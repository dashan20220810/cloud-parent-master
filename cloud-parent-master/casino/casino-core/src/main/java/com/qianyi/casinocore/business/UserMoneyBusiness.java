package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 用户钱包操作
 */
@Slf4j
@Service
public class UserMoneyBusiness {

    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private WashCodeChangeService washCodeChangeService;
    @Autowired
    private CodeNumChangeService codeNumChangeService;
    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private UserWashCodeConfigService userWashCodeConfigService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;
    @Autowired
    private GameRecordObdjService gameRecordObdjService;
    @Autowired
    private GameRecordObtyService gameRecordObtyService;
    @Autowired
    private RebateConfigurationService rebateConfigurationService;
    @Autowired
    private RebateDetailService rebateDetailService;
    @Autowired
    private PlatformConfigService platformConfigService;

    //默认最小清零打码量
    private static final BigDecimal DEFAULT_CLEAR = new BigDecimal("10");

    /***
     *
     * @param platformConfig
     * @param record
     * @return
     */
    @Transactional
    public void subCodeNum(String platform, PlatformConfig platformConfig, GameRecord record) {
        //已经处理过的不需要再次处理
        if (record.getCodeNumStatus() != null && record.getCodeNumStatus() == Constants.yes) {
            return;
        }
        log.info("开始打码,平台={}，注单ID={},注单明细={}", platform, record.getBetId(), record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        Long userId = record.getUserId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null || userMoney.getCodeNum() == null) {
            log.error("打码时，userMoney或者userMoney.getCodeNum()为null,userId={}", userId);
            return;
        }
        BigDecimal codeNum = userMoney.getCodeNum();
        //剩余打码量大于0
        if (codeNum.compareTo(BigDecimal.ZERO) == 1) {
            //有效投注额大于等于等于剩余打码量，最多只扣减剩余的
            validbet = validbet.compareTo(codeNum) > -1 ? codeNum : validbet;
            userMoneyService.subCodeNum(userId, validbet);
            BigDecimal codeNumAfter = userMoney.getCodeNum().subtract(validbet);
            CodeNumChange codeNumChange = CodeNumChange.setCodeNumChange(userId, record, validbet.negate(), userMoney.getCodeNum(), codeNumAfter);
            codeNumChange.setType(0);
            codeNumChange.setPlatform(platform);
            codeNumChangeService.save(codeNumChange);
            userMoney.setCodeNum(codeNumAfter);
            //检查最小清零打码量
            if (userMoney.getBalance().compareTo(BigDecimal.ZERO) == 1) {
                checkClearCodeNum(platformConfig, userId, userMoney);
            }
        }
        if (Constants.PLATFORM_WM.equals(platform)) {
            gameRecordService.updateCodeNumStatus(record.getId(), Constants.yes);
        } else if (Constants.PLATFORM_PG.equals(platform) || Constants.PLATFORM_CQ9.equals(platform)|| Constants.PLATFORM_SABASPORT.equals(platform)) {
            gameRecordGoldenFService.updateCodeNumStatus(record.getId(), Constants.yes);
        } else if(Constants.PLATFORM_OBDJ.equals(platform)){
            gameRecordObdjService.updateCodeNumStatus(record.getId(), Constants.yes);
        } else if(Constants.PLATFORM_OBTY.equals(platform)){
            gameRecordObtyService.updateCodeNumStatus(record.getId(), Constants.yes);
        }
        log.info("打码结束,平台={},注单ID={}", platform, record.getBetId());
    }

    /**
     * 最小清0打码量检查
     *
     * @param platformConfig
     * @param userId
     * @param user
     * @return
     */
    public void checkClearCodeNum(PlatformConfig platformConfig, Long userId, UserMoney user) {
        //打码已经归0，实时余额直接归0
        if (user.getCodeNum().compareTo(BigDecimal.ZERO) == 0) {
            userMoneyService.subBalance(userId, user.getBalance());
            return;
        }
        BigDecimal minCodeNumVal = DEFAULT_CLEAR;
        if (platformConfig != null && platformConfig.getClearCodeNum() != null) {
            minCodeNumVal = platformConfig.getClearCodeNum();
        }
        //余额小于等于最小清零打码量时 直接清0
        if (user.getBalance().compareTo(minCodeNumVal) < 1) {
            //打码量和实时余额都清0
            userMoneyService.subCodeNum(userId, user.getCodeNum());
            userMoneyService.subBalance(userId, user.getBalance());
            CodeNumChange codeNumChange = CodeNumChange.setCodeNumChange(userId, null, null, user.getCodeNum(), BigDecimal.ZERO);
            codeNumChange.setType(1);
            codeNumChange.setClearCodeNum(minCodeNumVal);
            codeNumChangeService.save(codeNumChange);
            log.info("触发最小清零打码量，打码量清0,最小清0点={},UserId={}", minCodeNumVal, userId);
        }
    }

    @Transactional
    public void washCode(String platform, GameRecord gameRecord) {
        //已经处理过的不需要再次处理
        if (gameRecord.getWashCodeStatus() != null && gameRecord.getWashCodeStatus() == Constants.yes) {
            return;
        }
        BigDecimal validbet = new BigDecimal(gameRecord.getValidbet());
        Long userId = gameRecord.getUserId();
        String washGameId = null;
        //WM的洗码是按里面游戏配置的，其他是按大类配置
        if (Constants.PLATFORM_WM.equals(platform)) {
            washGameId = gameRecord.getGid().toString();
        } else  {
            washGameId = platform;
        }
        log.info("开始洗码,平台={},注单ID={},注单明细={}", platform, gameRecord.getBetId(), gameRecord.toString());
        WashCodeConfig config = userWashCodeConfigService.getWashCodeConfigByUserIdAndGameId(platform, userId, washGameId);
        if (config != null && config.getRate() != null && config.getRate().compareTo(BigDecimal.ZERO) == 1) {
            log.info("游戏洗码配置={}", config.toString());
            //数据库存的10是代表百分之10
            BigDecimal rate = config.getRate().divide(new BigDecimal(100));//转换百分比
            BigDecimal washCodeVal = validbet.multiply(rate);
            WashCodeChange washCodeChange = new WashCodeChange();
            washCodeChange.setUserId(userId);
            washCodeChange.setAmount(washCodeVal);
            washCodeChange.setPlatform(platform);
            if (Constants.PLATFORM_WM.equals(platform)) {
                washCodeChange.setGameId(gameRecord.getGid().toString());
            } else {
                washCodeChange.setGameId(gameRecord.getGameCode());
            }
            washCodeChange.setGameName(gameRecord.getGname());
            washCodeChange.setRate(config.getRate());
            washCodeChange.setValidbet(validbet);
            washCodeChange.setGameRecordId(gameRecord.getId());
            washCodeChangeService.save(washCodeChange);
            userMoneyService.findUserByUserIdUseLock(userId);
            userMoneyService.addWashCode(userId, washCodeVal);
        }
        if (Constants.PLATFORM_WM.equals(platform)) {
            gameRecordService.updateWashCodeStatus(gameRecord.getId(), Constants.yes);
        } else if (Constants.PLATFORM_PG.equals(platform) || Constants.PLATFORM_CQ9.equals(platform)|| Constants.PLATFORM_SABASPORT.equals(platform)) {
            gameRecordGoldenFService.updateWashCodeStatus(gameRecord.getId(), Constants.yes);
        } else if (Constants.PLATFORM_OBDJ.equals(platform)) {
            gameRecordObdjService.updateWashCodeStatus(gameRecord.getId(), Constants.yes);
        } else if (Constants.PLATFORM_OBTY.equals(platform)) {
            gameRecordObtyService.updateWashCodeStatus(gameRecord.getId(), Constants.yes);
        }
        log.info("洗码完成,平台={},注单ID={}", platform, gameRecord.getBetId());
    }

    /**
     * 三级分润
     *
     * @param record
     */
    @Transactional
    public void shareProfit(String platform, GameRecord record) {
        //已经处理过的不需要再次处理
        if (record.getShareProfitStatus() != null && record.getShareProfitStatus() >0) {
            return;
        }
        log.info("开始三级分润,平台={},注单ID={},注单明细={}", platform, record.getBetId(), record.toString());
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        Long userId = record.getUserId();
        ShareProfitMqVo shareProfitMqVo = new ShareProfitMqVo();
        shareProfitMqVo.setPlatform(platform);
        shareProfitMqVo.setUserId(userId);
        shareProfitMqVo.setValidbet(validbet);
        shareProfitMqVo.setGameRecordId(record.getId());
        shareProfitMqVo.setBetTime(record.getBetTime());
        rabbitTemplate.convertAndSend(RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.SHAREPROFIT_DIRECT, shareProfitMqVo, new CorrelationData(UUID.randomUUID().toString()));
        log.info("分润消息发送成功,平台={},注单ID={},消息明细={}", platform, record.getBetId(), shareProfitMqVo);
    }

    /**
     * 增加账号实时余额
     *
     * @param userId
     * @param balance
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addBalance(Long userId, BigDecimal balance) {
        UserMoney userMoney = userMoneyService.findUserByUserIdUse(userId);
        if (userMoney.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        //打码量清0或者balance已经归0后不再累加
        if (userMoney.getCodeNum().compareTo(BigDecimal.ZERO) == 0) {
            userMoneyService.subBalance(userId, userMoney.getBalance());
            return;
        }
        //打码量和balance清0后不再累加
        if (balance != null && balance.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyService.addBalance(userId, balance);
        }
    }

    /**
     * 扣减账号实时余额
     *
     * @param userId
     * @param balance
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void subBalance(Long userId, BigDecimal balance) {
        UserMoney userMoney = userMoneyService.findUserByUserIdUse(userId);
        if (userMoney.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        //打码量等于0时，balance也要清0
        if (userMoney.getCodeNum().compareTo(BigDecimal.ZERO) == 0) {
            userMoneyService.subBalance(userId, userMoney.getBalance());
            return;
        }
        if (balance != null && balance.compareTo(BigDecimal.ZERO) == 1) {
            //剩余的小于扣减的
            if (userMoney.getBalance().compareTo(balance) == -1) {
                balance = userMoney.getBalance();
            }
            if (balance.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyService.subBalance(userId, balance);
            }
        }
    }

    /**
     * 增加账号实时余额
     *
     * @param userId
     * @param balance
     */
    @Transactional
    public void addBalanceAdmin(Long userId, BigDecimal balance) {
        if (balance != null && balance.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyService.addBalance(userId, balance);
        }
    }

    /**
     * 扣减账号实时余额
     *
     * @param userId
     * @param balance
     */
    @Transactional
    public void subBalanceAdmin(Long userId, BigDecimal balance) {
        if (balance != null && balance.compareTo(BigDecimal.ZERO) == 1) {
            UserMoney userMoney = userMoneyService.findUserByUserIdUse(userId);
            //剩余的小于扣减的
            if (userMoney.getBalance().compareTo(balance) == -1) {
                balance = userMoney.getBalance();
            }
            if (balance.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyService.subBalance(userId, balance);
            }
        }
    }

    @Transactional
    public void rebate(String platform, GameRecord record) {
        //已经处理过的不需要再次处理
        if (record.getRebateStatus() != null && record.getRebateStatus() == Constants.yes) {
            return;
        }
        log.info("开始返利，record={}",record.toString());
        //先查询平台的返利比例
        RebateConfiguration rebateConfiguration = rebateConfigurationService.findByUserIdAndType(0L, 0);
        BigDecimal platformRate = getRate(platform, rebateConfiguration);
        if (platformRate == null) {
            platformRate = BigDecimal.ZERO;
        }
        platformRate = platformRate.divide(new BigDecimal(100));//转换百分比
        BigDecimal validbet = new BigDecimal(record.getValidbet());
        BigDecimal totalAmount = validbet.multiply(platformRate);
        //查询用户的分成比例
        RebateConfiguration userRebateConfiguration = rebateConfigurationService.findByUserIdAndType(record.getUserId(), 1);
        BigDecimal userDivideRate = getRate(platform, userRebateConfiguration);
        if (userDivideRate == null) {
            //查询用户归属代理的分成比例
            RebateConfiguration proxyRebateConfiguration = rebateConfigurationService.findByUserIdAndType(record.getThirdProxy(), 2);
            userDivideRate = getRate(platform, proxyRebateConfiguration);
        }
        if (userDivideRate == null) {
            userDivideRate = BigDecimal.ZERO;
        }
        userDivideRate = userDivideRate.divide(new BigDecimal(100));//转换百分比
        //用户分成比例
        BigDecimal userAmount = totalAmount.multiply(userDivideRate);
        //剩余的
        BigDecimal surplusAmount = totalAmount.subtract(userAmount);
        //保存明细数据
        RebateDetail rebateDetail = new RebateDetail();
        rebateDetail.setUserId(record.getUserId());
        rebateDetail.setGameRecordId(record.getId());
        rebateDetail.setPlatform(platform);
        rebateDetail.setValidbet(validbet);
        rebateDetail.setPlatformRebateRate(platformRate);
        rebateDetail.setUserDivideRate(userDivideRate);
        rebateDetail.setTotalAmount(totalAmount);
        rebateDetail.setUserAmount(userAmount);
        rebateDetail.setSurplusAmount(surplusAmount);
        rebateDetailService.save(rebateDetail);
        //把分到的钱加到userMoney表的洗码额上面
        if (userAmount.compareTo(BigDecimal.ZERO) == 1) {
            userMoneyService.addWashCode(record.getUserId(), userAmount);
        }
        //更新返利状态
        if (Constants.PLATFORM_WM.equals(platform)) {
            gameRecordService.updateRebateStatus(record.getId(), Constants.yes);
        } else if (Constants.PLATFORM_PG.equals(platform) || Constants.PLATFORM_CQ9.equals(platform)|| Constants.PLATFORM_SABASPORT.equals(platform)) {
            gameRecordGoldenFService.updateRebateStatus(record.getId(), Constants.yes);
        } else if (Constants.PLATFORM_OBDJ.equals(platform)) {
            gameRecordObdjService.updateRebateStatus(record.getId(), Constants.yes);
        } else if (Constants.PLATFORM_OBTY.equals(platform)) {
            gameRecordObtyService.updateRebateStatus(record.getId(), Constants.yes);
        }
        if (rebateDetail.getTotalAmount().compareTo(BigDecimal.ZERO)>0){
            //后台异步增减平台总余额
            platformConfigService.reception(CommonConst.NUMBER_0,rebateDetail.getTotalAmount());
        }
        log.info("返利完成，record={}",record.toString());
    }

    public BigDecimal getRate(String platform, RebateConfiguration rebateConfiguration) {
        if (rebateConfiguration == null) {
            return null;
        }
        BigDecimal rate = null;
        if (Constants.PLATFORM_WM.equals(platform)) {
            rate = rebateConfiguration.getWMRate();
        } else if (Constants.PLATFORM_PG.equals(platform)) {
            rate = rebateConfiguration.getPGRate();
        } else if (Constants.PLATFORM_CQ9.equals(platform)) {
            rate = rebateConfiguration.getCQ9Rate();
        } else if (Constants.PLATFORM_SABASPORT.equals(platform)) {
            rate = rebateConfiguration.getSABASPORTRate();
        } else if (Constants.PLATFORM_OBDJ.equals(platform)) {
            rate = rebateConfiguration.getOBDJRate();
        } else if (Constants.PLATFORM_OBTY.equals(platform)) {
            rate = rebateConfiguration.getOBTYRate();
        }
        return rate;
    }

    public void proxyGameRecordReport(String platform, GameRecord record) {
        log.info("开始推送后台proxyGameRecordReport MQ消息,平台={},注单ID={},注单明细={}", platform, record.getBetId(), record.toString());
        ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
        vo.setPlatform(platform);
        vo.setOrderId(record.getBetId());
        vo.setFirstProxy(record.getFirstProxy());
        vo.setSecondProxy(record.getSecondProxy());
        vo.setThirdProxy(record.getThirdProxy());
        vo.setOrderTimes(record.getBetTime());
        vo.setUserId(record.getUserId());
        if (!ObjectUtils.isEmpty(record.getValidbet())){
            vo.setValidAmount(new BigDecimal(record.getValidbet()));
        }
        if (!ObjectUtils.isEmpty(record.getWinLoss())){
            vo.setWinLoss(new BigDecimal(record.getWinLoss()));
        }
        vo.setGameRecordId(record.getId());
        if (!ObjectUtils.isEmpty(record.getBet())){
            vo.setBetAmount(new BigDecimal(record.getBet()));
        }
        rabbitTemplate.convertAndSend(RabbitMqConstants.PROXYG_AMERECORD_REPORT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.PROXYG_AMERECORD_REPORT_DIRECT, vo, new CorrelationData(UUID.randomUUID().toString()));
        log.info("proxyGameRecordReport MQ消息发送成功,平台={},注单ID={},消息明细={}", platform, record.getBetId(), vo);
    }
}

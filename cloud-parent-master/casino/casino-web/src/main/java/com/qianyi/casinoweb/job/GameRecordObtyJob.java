package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.vo.GameRecordObtyDataVo;
import com.qianyi.casinoweb.vo.GameRecordObtyVo;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 查询用户投注记录接口,查询最近一周的数据,每次最大100条
 */
@Component
@Slf4j
public class GameRecordObtyJob {

    @Autowired
    private PublicObtyApi obtyApi;
    @Autowired
    private GameRecordObtyService gameRecordObtyService;
    @Autowired
    private GameRecordObEndTimeService gameRecordObEndTimeService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private GameRecordObtyDetailService gameRecordObtyDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private UserMoneyBusiness userMoneyBusiness;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private PlatformGameService platformGameService;

    //每隔7分钟执行一次
    @Scheduled(cron = "0 0/7 * * * ?")
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_OB);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭OB平台,无需拉单,platformGame={}", platformGame);
            return;
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(Constants.PLATFORM_OB, Constants.PLATFORM_OBTY);
        if (adGame != null && adGame.getGamesStatus() == 2) {
            log.info("后台已关闭OB体育,无需拉单,adGame={}",adGame);
            return;
        }
        log.info("定时器开始拉取OB体育注单记录");
        GameRecordObEndTime gameRecordObEndTime = gameRecordObEndTimeService.findFirstByVendorCodeAndStatusOrderByEndTimeDesc(Constants.PLATFORM_OBTY, Constants.yes);
        Long time = gameRecordObEndTime == null ? null : gameRecordObEndTime.getEndTime();
        //获取查询游戏记录的时间范围
        StartTimeAndEndTime startTimeAndEndTime = getStartTimeAndEndTime(time);
        pullGameRecord(startTimeAndEndTime);
        log.info("定时器拉取完成OB体育注单记录");
    }

    public void pullGameRecord(StartTimeAndEndTime startTimeAndEndTime) {
        if (startTimeAndEndTime == null) {
            return;
        }
        Long startTime = startTimeAndEndTime.getStartTime();
        Long endTime = startTimeAndEndTime.getEndTime();
        log.info("开始拉取{}到{}的OB体育游戏记录", startTime, endTime);
        pullGameRecordByTime(startTime, endTime, 1);
        log.info("{}到{}的OB体育游戏记录拉取完成", startTime, endTime);
    }

    public void pullGameRecordByTime(Long startTime, Long endTime, Integer pageNum) {
        //每次最多拉100条
        Integer pageSize = 100;
        PublicObtyApi.ResponseEntity betResult = obtyApi.queryBetList(null, startTime, endTime, 1, pageNum, pageSize);
        if (betResult == null) {
            log.error("拉取OB体育注单时远程请求错误,startTime={},endTime={},pageNum={}", startTime, endTime, pageNum);
            saveGameRecordObEndTime(startTime, endTime, Constants.no);
            return;
        }
        if (!betResult.getStatus()) {
            log.error("拉取OB体育注单时远程请求错误,startTime={},endTime={},pageNum={},result={}", startTime, endTime, pageNum, betResult.toString());
            saveGameRecordObEndTime(startTime, endTime, Constants.no);
            return;
        }
        if (ObjectUtils.isEmpty(betResult.getData())) {
            return;
        }
        GameRecordObtyDataVo gameReocrdData = JSONObject.parseObject(betResult.getData(), GameRecordObtyDataVo.class);
        //当前页
        Integer currentPageNum = gameReocrdData.getPageNum();
        Integer totalCount = gameReocrdData.getTotalCount();
        //计算总页数
        int totalNum = (int) Math.ceil(totalCount.doubleValue() / pageSize);
        //保存数据
        saveAll(gameReocrdData.getList());
        //当前时间范围数据没拉取完继续拉取
        if (pageNum < totalNum) {
            pullGameRecordByTime(startTime, endTime, currentPageNum + 1);
        }
        //保存时间区间
        saveGameRecordObEndTime(startTime, endTime, Constants.yes);
    }

    /**
     * @param startTime
     * @return
     * @throws ParseException
     */
    public StartTimeAndEndTime getStartTimeAndEndTime(Long startTime) {
        Long endTime = System.currentTimeMillis();
        //第一次拉取数据取当前时间前10分钟为开始时间，之后以上次拉取数据的结束时间为开始时间
        if (ObjectUtils.isEmpty(startTime)) {
            startTime = endTime - 10 * 60 * 1000;
        }
        //时间范围重叠两分钟
        startTime = startTime - 2 * 60 * 1000;
        if (startTime >= endTime) {
            return null;
        }
        StartTimeAndEndTime startTimeAndEndTime = new StartTimeAndEndTime();
        startTimeAndEndTime.setStartTime(startTime);
        startTimeAndEndTime.setEndTime(endTime);
        return startTimeAndEndTime;
    }


    public void saveAll(List<GameRecordObtyVo> gameRecordList) {
        if (CollectionUtils.isEmpty(gameRecordList)) {
            return;
        }
        log.info("开始处理OB体育游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecordObtyVo gameRecordObtyVo : gameRecordList) {
            GameRecordObty gameRecord = null;
            try {
                gameRecord = save(gameRecordObtyVo);
                if (gameRecord == null) {
                    continue;
                }
                //业务处理
                business(Constants.PLATFORM_OBTY, gameRecord, platformConfig);
                //保存明细数据
                saveBatchDetail(gameRecordObtyVo.getDetailList());
            }  catch (Exception e) {
                e.printStackTrace();
                log.error("保存OB体育游戏记录时报错,message={}", e.getMessage());
            }
        }
    }

    public void business(String platform, GameRecordObty gameRecordObty, PlatformConfig platformConfig) {
        //计算用户账号实时余额
        changeUserBalance(gameRecordObty);
        //组装gameRecord
        GameRecord record = combineGameRecord(gameRecordObty);
        //发送注单消息到MQ后台要统计数据
        gameRecordAsyncOper.proxyGameRecordReport(platform, record);
        String validbet = record.getValidbet();
        if (ObjectUtils.isEmpty(validbet) || new BigDecimal(validbet).compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        //洗码
        gameRecordAsyncOper.washCode(platform, record);
        // 抽点
        gameRecordAsyncOper.extractPoints(platform, record);
        //扣减打码量
        gameRecordAsyncOper.subCodeNum(platform, platformConfig, record);
        //代理分润
        gameRecordAsyncOper.shareProfit(platform, record);
        //返利
        gameRecordAsyncOper.rebate(platform, record);
    }

    /**
     * 改变用户实时余额
     */
    private void changeUserBalance(GameRecordObty gameRecordObty) {
        try {
            BigDecimal betAmount = gameRecordObty.getOrderAmount();
            BigDecimal winAmount = gameRecordObty.getProfitAmount();
            if (betAmount == null || winAmount == null) {
                return;
            }
            Long userId = gameRecordObty.getUserId();
            //下注金额大于0，扣减
            if (betAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.subBalance(userId, betAmount);
            }
            //派彩金额大于0，增加
            if (winAmount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.addBalance(userId, winAmount);
            }
        }catch (Exception e){
            log.error("改变用户实时余额时报错，msg={}",e.getMessage());
        }
    }

    public GameRecordObty save(GameRecordObtyVo gameRecordObtyVo) {
        UserThird account = userThirdService.findByObtyAccount(gameRecordObtyVo.getUserName());
        if (account == null || account.getUserId() == null) {
            log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecordObtyVo.getUserName());
            return null;
        }
        gameRecordObtyVo.setBetTime(gameRecordObtyVo.getCreateTime());
        SimpleDateFormat format = DateUtil.getSimpleDateFormat();
        //投注时间转yyyy-MM-dd
        if (!ObjectUtils.isEmpty(gameRecordObtyVo.getBetTime()) && gameRecordObtyVo.getBetTime() != 0) {
            gameRecordObtyVo.setBetStrTime(format.format(gameRecordObtyVo.getBetTime()));
        }
        //结算时间转yyyy-MM-dd
        if (!ObjectUtils.isEmpty(gameRecordObtyVo.getSettleTime()) && gameRecordObtyVo.getSettleTime() != 0) {
            gameRecordObtyVo.setSettleStrTime(format.format(gameRecordObtyVo.getSettleTime()));
        }
        GameRecordObty gameRecord = new GameRecordObty();
        BeanUtils.copyProperties(gameRecordObtyVo, gameRecord);
        gameRecord.setUserId(account.getUserId());
        BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getOrderAmount()) ? BigDecimal.ZERO : gameRecord.getOrderAmount();
        //有效投注额为0不参与洗码,打码,分润,抽點
        if (validbet.compareTo(BigDecimal.ZERO) == 0) {
            gameRecord.setWashCodeStatus(Constants.yes);
            gameRecord.setCodeNumStatus(Constants.yes);
            gameRecord.setShareProfitStatus(Constants.yes);
            gameRecord.setExtractStatus(Constants.yes);
            gameRecord.setRebateStatus(Constants.yes);
        }
        //查询3级代理
        User user = userService.findById(gameRecord.getUserId());
        if (user != null) {
            gameRecord.setFirstProxy(user.getFirstProxy());
            gameRecord.setSecondProxy(user.getSecondProxy());
            gameRecord.setThirdProxy(user.getThirdProxy());
        }

        GameRecordObty record = gameRecordObtyService.save(gameRecord);
        return record;
    }

    private void saveBatchDetail(List<GameRecordObtyDetail> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        for (GameRecordObtyDetail detail : detailList) {
            try {
                gameRecordObtyDetailService.save(detail);
            } catch (DataIntegrityViolationException e) {
                log.info("保存OB体育游戏记录时报错GameRecordObtyDetail唯一索引异常，GameRecordObtyDetail={}",detail.toString());
            }catch (Exception e) {
                log.error("保存OB体育注单明细时出错，msg={},GameRecordObtyDetail={}", e.getMessage(), detail.toString());
            }
        }
    }

    private GameRecord combineGameRecord(GameRecordObty item) {
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getOrderNo());
        gameRecord.setValidbet(item.getOrderAmount().toString());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode(Constants.PLATFORM_OBTY);
        gameRecord.setGname("OB体育");
        gameRecord.setBetTime(item.getSettleStrTime());
        gameRecord.setId(item.getId());
        gameRecord.setFirstProxy(item.getFirstProxy());
        gameRecord.setSecondProxy(item.getSecondProxy());
        gameRecord.setThirdProxy(item.getThirdProxy());
        if (!ObjectUtils.isEmpty(item.getOrderAmount())) {
            gameRecord.setBet(item.getOrderAmount().toString());
        }
        if (item.getProfitAmount() != null) {
            gameRecord.setWinLoss(item.getProfitAmount().toString());
        }
        return gameRecord;
    }

    private void saveGameRecordObEndTime(Long startTime, Long endTime, Integer status) {
        GameRecordObEndTime gameRecordObEndTime = new GameRecordObEndTime();
        gameRecordObEndTime.setStartTime(startTime);
        gameRecordObEndTime.setEndTime(endTime);
        gameRecordObEndTime.setStatus(status);
        gameRecordObEndTime.setVendorCode(Constants.PLATFORM_OBTY);
        gameRecordObEndTimeService.save(gameRecordObEndTime);
    }

    @Data
    public static class StartTimeAndEndTime {
        private Long startTime;
        private Long endTime;
    }
}

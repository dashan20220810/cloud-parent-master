package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.model.GameRecordObdj;
import com.qianyi.casinocore.model.GameRecordObty;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.util.RedisLockUtil;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Date;

@Slf4j
@Service
public class ProxyGameRecordReportBusiness {

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private RedisLockUtil redisLockUtil;


    @Transactional
    public void saveOrUpdate(ProxyGameRecordReportVo proxyGameRecordReportVo) {
        if (proxyGameRecordReportVo==null|| proxyGameRecordReportVo.getPlatform() == null || proxyGameRecordReportVo.getGameRecordId() == null){
            log.error("代理报表异步处理平台编码为空{}",proxyGameRecordReportVo);
            return;
        }
        String value = proxyGameRecordReportVo.getPlatform() + proxyGameRecordReportVo.getGameRecordId().toString();
        String key = MessageFormat.format(RedisLockUtil.PROXY_GAME_RECORD_REPORT_BUSINESS,value);
        Boolean lock = false;
        try {
            lock = redisLockUtil.getLock(key, value);
            if (lock){
                log.info("取到redis锁{}",key);
                if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_WM)){
                    GameRecord gameRecord = gameRecordService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecord == null || gameRecord.getGameRecordStatus() == Constants.yes){
                        log.error("wm注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBDJ)){
                    GameRecordObdj gameRecordById = gameRecordObdjService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null || gameRecordById.getGameRecordStatus() == Constants.yes){
                        log.error("OB电竞注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBTY)){
                    GameRecordObty gameRecordById =
                        gameRecordObtyService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null || gameRecordById.getGameRecordStatus() == Constants.yes){
                        log.error("OB体育注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }else {
                    GameRecordGoldenF gameRecordById = gameRecordGoldenFService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null || gameRecordById.getGameRecordStatus() == Constants.yes){
                        log.error("电子注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }
                Date date = DateUtil.getSimpleDateFormat().parse(proxyGameRecordReportVo.getOrderTimes());
                Date americaDate = cn.hutool.core.date.DateUtil.offsetHour(date, -12);//转为美东时间保存,代理报表全部用美东时间
                String orderTimes = DateUtil.dateToPatten1(americaDate);
                Long proxyGameRecordReportId = CommonUtil.toHash(orderTimes+proxyGameRecordReportVo.getUserId().toString());
                if (proxyGameRecordReportVo.getThirdProxy() == null){
                    proxyGameRecordReportService.updateKey(proxyGameRecordReportId,proxyGameRecordReportVo.getUserId(),
                        orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),0L,
                        0L,0L,proxyGameRecordReportVo.getBetAmount());
                }else {
                    proxyGameRecordReportService.updateKey(proxyGameRecordReportId,proxyGameRecordReportVo.getUserId(),
                        orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getFirstProxy(),
                        proxyGameRecordReportVo.getSecondProxy(),proxyGameRecordReportVo.getThirdProxy(),proxyGameRecordReportVo.getBetAmount());
                }

                Long userGameRecordReportId = CommonUtil.toHash(orderTimes+proxyGameRecordReportVo.getUserId().toString()+proxyGameRecordReportVo.getPlatform());
                //            if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_WM)){//会员报表单单wm使用美东时间
                //                userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),orderTimes,
                //                    proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());
                //            }else {
                //                orderTimes = DateUtil.dateToPatten1(date);
                //                userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),orderTimes,
                //                    proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());
                //            }

                orderTimes = DateUtil.dateToPatten1(date);
                userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),orderTimes,
                    proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());

                //更新注单状态
                if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_WM)){
                    gameRecordService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBDJ)){
                    gameRecordObdjService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBTY)){
                    gameRecordObtyService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else {
                    gameRecordGoldenFService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }
            }
        }catch (Exception ex){
            log.error("代理报表异步处理异常需要人工补单,注单标识{}={}",proxyGameRecordReportVo.getPlatform(),proxyGameRecordReportVo.getGameRecordId());
        }finally {
            if (lock){
                log.info("释放redis锁{}",key);
                redisLockUtil.releaseLock(key, value);
            }
        }
    }
}

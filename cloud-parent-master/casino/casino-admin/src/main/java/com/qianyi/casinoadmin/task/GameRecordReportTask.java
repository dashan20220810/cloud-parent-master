package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.business.ProxyGameRecordReportBusiness;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.model.GameRecordObdj;
import com.qianyi.casinocore.model.GameRecordObty;
import com.qianyi.casinocore.service.GameRecordGoldenFService;
import com.qianyi.casinocore.service.GameRecordObdjService;
import com.qianyi.casinocore.service.GameRecordObtyService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 会员报表补单定时任务，主要处理mq异步订单没有处理的注单
 *
 */
@Slf4j
@Component
public class GameRecordReportTask {

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;
    
    public static final List<Integer> betStatus = new ArrayList<>();

    static {
        betStatus.add(5);
        betStatus.add(6);
        betStatus.add(8);
        betStatus.add(9);
        betStatus.add(10);
    }
    
    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    @Autowired
    private ProxyGameRecordReportBusiness proxyGameRecordReportBusiness;

    @Scheduled(cron = TaskConst.GAME_RECORD_REPORT_TASK)
    public void begin(){
        log.info("每日会员报表补单定时任务开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        String today = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        nowTime.add(Calendar.DATE, -1);
        String yesterday = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String startTime = yesterday + start;
        String endTime = today + end;
        log.info("每日会员报表补单定时startTime:{}-endTime:{}",startTime,endTime);
        List<ProxyGameRecordReportVo> proxyGameRecordReportVos = new ArrayList<>();
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameRecordStatus(0);
        List<GameRecord> gameRecords = gameRecordService.findGameRecord(gameRecord, startTime, endTime);
        if(gameRecords != null && gameRecords.size()>= 1){
            proxyGameRecordReportVos = assemblyGameRecord(proxyGameRecordReportVos,gameRecords);
        }
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setGameRecordStatus(0);
        List<GameRecordGoldenF> gameRecordGoldenFs = gameRecordGoldenFService.findGameRecord(gameRecordGoldenF, startTime, endTime);
        if(gameRecordGoldenFs != null && gameRecordGoldenFs.size()>= 1){
            proxyGameRecordReportVos = assemblyGameRecordGoldenF(proxyGameRecordReportVos,gameRecordGoldenFs);
        }

        GameRecordObdj gameRecordObdj = new GameRecordObdj();
        gameRecordObdj.setGameRecordStatus(0);
        List<GameRecordObdj> gameRecordObdjs = gameRecordObdjService.findGameRecord(gameRecordObdj, startTime, endTime, betStatus);
        if(gameRecordObdjs != null && gameRecordObdjs.size()>= 1){
            proxyGameRecordReportVos = assemblyGameRecordObdj(proxyGameRecordReportVos,gameRecordObdjs);
        }
        GameRecordObty gameRecordObty = new GameRecordObty();
        gameRecordObty.setGameRecordStatus(0);
        List<GameRecordObty> gameRecordObtys = gameRecordObtyService.findGameRecord(gameRecordObty, startTime, endTime);
        if(gameRecordObtys != null && gameRecordObtys.size()>= 1){
            proxyGameRecordReportVos = assemblyGameRecordObty(proxyGameRecordReportVos,gameRecordObtys);
        }

        if (proxyGameRecordReportVos.size() >= 1){
            log.info("统计到未处理的注单{}==========================================>",proxyGameRecordReportVos.size());
            for (ProxyGameRecordReportVo proxyGameRecordReportVo:proxyGameRecordReportVos){
                proxyGameRecordReportBusiness.saveOrUpdate(proxyGameRecordReportVo);
            }
            log.info("处理结束，任务结束end==========================================>");
        }else {
            log.info("没有未处理的注单，任务结束end==========================================>");
        }
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecord(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,List<GameRecord> gameRecords){
        try {
            for (GameRecord gameRecord:gameRecords){
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getBetId());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getBetTime());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(new BigDecimal(gameRecord.getValidbet()));
                vo.setWinLoss(new BigDecimal(gameRecord.getWinLoss()));
                vo.setBetAmount(new BigDecimal(gameRecord.getBet()));
                vo.setPlatform(Constants.PLATFORM_WM);
                proxyGameRecordReportVos.add(vo);
            }
        }catch (Exception ex){
            log.error("组装wm注单异常{}",ex);
        }
        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordGoldenF(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,List<GameRecordGoldenF> gameRecordGoldenFS){
        try {
            for (GameRecordGoldenF gameRecord:gameRecordGoldenFS){
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getTraceId());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getCreateAtStr());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(gameRecord.getBetAmount());
                vo.setWinLoss(gameRecord.getWinAmount().subtract(gameRecord.getBetAmount()));
                vo.setBetAmount(gameRecord.getBetAmount());
                vo.setPlatform(gameRecord.getVendorCode());
                proxyGameRecordReportVos.add(vo);
            }
        }catch (Exception ex){
            log.error("组装PG注单异常{}",ex);
        }

        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordObdj(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,List<GameRecordObdj> gameRecords){
        try {
            for (GameRecordObdj gameRecord:gameRecords){
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getBetId().toString());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getSetStrTime());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(gameRecord.getBetAmount());
                vo.setWinLoss(BigDecimal.ZERO);
                if (gameRecord.getWinAmount() != null && gameRecord.getBetAmount() != null) {
                    BigDecimal winLoss = gameRecord.getWinAmount().subtract(gameRecord.getBetAmount());
                    vo.setWinLoss(winLoss);
                }
                vo.setBetAmount(gameRecord.getBetAmount());
                vo.setPlatform(Constants.PLATFORM_OBDJ);
                proxyGameRecordReportVos.add(vo);
            }
        }catch (Exception ex){
            log.error("组装OBDJ注单异常{}",ex);
        }

        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordObty(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,List<GameRecordObty> gameRecords){
        try {
            for (GameRecordObty gameRecord:gameRecords){
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getOrderNo());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getSettleStrTime());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(gameRecord.getOrderAmount());
                vo.setWinLoss(BigDecimal.ZERO);
                if (gameRecord.getProfitAmount() != null) {
                    vo.setWinLoss(gameRecord.getProfitAmount());
                }
                vo.setBetAmount(gameRecord.getOrderAmount());
                vo.setPlatform(Constants.PLATFORM_OBTY);
                proxyGameRecordReportVos.add(vo);
            }
        }catch (Exception ex){
            log.error("组装OBTY注单异常{}",ex);
        }

        return proxyGameRecordReportVos;
    }
}

//package com.qianyi.casinoadmin.task;
//
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.GameRecordReport;
//import com.qianyi.casinocore.service.GameRecordReportService;
//import com.qianyi.casinocore.service.GameRecordService;
//import com.qianyi.casinocore.util.CommonConst;
//import com.qianyi.casinocore.util.TaskConst;
//import com.qianyi.modulecommon.util.DateUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class GameRecordTask {
//    public final static String start = ":00:00";
//
//    public final static String end = ":59:59";
//
//    @Autowired
//    private GameRecordReportService gameRecordReportService;
//
//    @Autowired
//    private GameRecordService gameRecordService;
//
//    @Scheduled(cron = TaskConst.GAMERECORD_TASK)
//    public void create() throws ParseException {
//        log.info("每小时报表统计开始start=============================================》");
//        Calendar nowTime = Calendar.getInstance();
//        nowTime.add(Calendar.HOUR, -1);
//        String format = DateUtil.getSimpleDateFormat2().format(nowTime.getTime());
//        List<GameRecordReport> byStaticsTimes = gameRecordReportService.findByStaticsTimes(format);
//        if (!LoginUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0) {
//            byStaticsTimes.clear();
//            return;
//        }
//        String startTime = format + start;
//        String endTime = format + end;
//        List<Map<String, Object>> records = gameRecordService.queryGameRecords(startTime, endTime);
//        this.create(format,records);
//        log.info("每小时报表统计结束end=============================================》");
//    }
//    private void create(String format,List<Map<String, Object>> reportResult){
//        try {
//            if (LoginUtil.checkNull(reportResult) || reportResult.size() == CommonConst.NUMBER_0){
//                return;
//            }
//            if (LoginUtil.checkNull(reportResult.get(0).get("num")) || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0){
//                return;
//            }
//            for (Map<String, Object> map:reportResult){
//                GameRecordReport gameRecordReport = new GameRecordReport();
//                gameRecordReport.setStaticsTimes(format);
//                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
//                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
//                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
//                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
//                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
//                gameRecordReport.setGid(Integer.parseInt(map.get("gid").toString()));
//                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
//                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
//                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
//                gameRecordReportService.save(gameRecordReport);
//            }
//        }catch (Exception ex){
//            log.error("每小时报表统计失败",ex);
//        }
//    }
//}

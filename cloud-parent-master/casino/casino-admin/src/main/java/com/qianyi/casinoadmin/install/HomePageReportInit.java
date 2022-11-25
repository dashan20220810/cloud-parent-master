//package com.qianyi.casinoadmin.install;
//
//import com.qianyi.casinoadmin.model.HomePageReport;
//import com.qianyi.casinoadmin.service.HomePageReportService;
//import com.qianyi.casinoadmin.task.HomePageReportTask;
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.util.CommonConst;
//import com.qianyi.casinocore.util.CommonUtil;
//import com.qianyi.modulecommon.util.DateUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.text.ParseException;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//@Component
//@Slf4j
//@Order(value = 6)
//public class HomePageReportInit implements CommandLineRunner {
//
//    public final static String startTime = "2021-11-01";
////    public final static String endTime = "2022-03-01";
//    @Autowired
//    private HomePageReportTask homePageReportTask;
//
//    @Autowired
//    private HomePageReportService homePageReportService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("初始化首页报表开始============================================》");
//        List<HomePageReport> all = homePageReportService.findAll();
//        if (!LoginUtil.checkNull(all) && all.size() >= CommonConst.NUMBER_1){
//            return;
//        }
//        new Thread(()->begin()).start();
//    }
//
//    private void begin(){
//        try {
//            Date startDate = DateUtil.getSimpleDateFormat1().parse(startTime);
//            Calendar nowTime = Calendar.getInstance();
//            int hour = nowTime.get(Calendar.HOUR_OF_DAY);
//            String endTime;
//            if (hour >= CommonConst.NUMBER_12){
//                endTime = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
//            }else {
//                nowTime.add(Calendar.DATE, -1);
//                endTime = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
//            }
//            Date endDate = DateUtil.getSimpleDateFormat1().parse(endTime);
//            Map<Integer,String> mapDate = CommonUtil.findDates("D", startDate, endDate);
//            mapDate.forEach((k,yesterday)->{
//                Calendar calendar = Calendar.getInstance();
//                Date  yesterdayDate = null;
//                try {
//                    yesterdayDate = DateUtil.getSimpleDateFormat1().parse(yesterday);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                calendar.setTime(yesterdayDate);
//                calendar.add(Calendar.DATE, 1);
//                String today = DateUtil.dateToPatten1(calendar.getTime());
////                System.out.println("昨日"+yesterday+"===========今日"+today);
//                homePageReportTask.begin(yesterday,today);
//            });
//        }catch (ParseException ex){
//            log.error("严重的异常，初始化首页数据失败");
//        }
//
//    }
//}

//package com.qianyi.casinoproxy.task;
//
//import com.qianyi.casinocore.model.*;
//import com.qianyi.casinocore.service.*;
//import com.qianyi.casinocore.util.CommonConst;
//import com.qianyi.casinocore.util.TaskConst;
//import com.qianyi.casinocore.model.ProxyHomePageReport;
//import com.qianyi.casinocore.service.ProxyHomePageReportService;
//import com.qianyi.casinoproxy.util.CasinoProxyUtil;
//import com.qianyi.modulecommon.util.DateUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.Calendar;
//import java.util.Date;
//import java.util.LinkedList;
//import java.util.List;
//
//@Slf4j
//@Component
//public class ProxyHomePageReportTask {
//    public final static String start = " 00:00:00";
//
//    public final static String end = " 23:59:59";
//
//    @Autowired
//    private ProxyUserService proxyUserService;
//
//    @Autowired
//    private ProxyHomePageReportService proxyHomePageReportService;
//    @Scheduled(cron = TaskConst.PROXY_HOME_PAGE_REPORT)
//    public void create(){
//        log.info("每日代理首页报表统计开始start=============================================》");
//        Calendar nowTime = Calendar.getInstance();
//        nowTime.add(Calendar.DATE, -1);
//        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
//            List<ProxyHomePageReport> byStaticsTimes = proxyHomePageReportService.findByStaticsTimes(format);
//            if (!CasinoProxyUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0)
//                return;
//            try {
//                String startTime = format + start;
//                String endTime = format + end;
//                Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
//                Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
//                ProxyUser proxyUser = new ProxyUser();
//                proxyUser.setIsDelete(CommonConst.NUMBER_1);
////                proxyUser.setUserFlag(CommonConst.NUMBER_1);
//                List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
//                if (CasinoProxyUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
//                    return;
//                }
//                proxyUserList.forEach(proxy -> {
//                    new Thread(()->this.create(proxy,startTime,endTime,startDate,endDate,format)).start();
//                });
//                log.info("每日代理首页报表统计结束end=============================================》");
//            }catch (Exception ex){
//                log.error("代理首页报表统计失败",ex);
//            }
//    }
//    public void create(ProxyUser proxyUser,String startTime,String endTime,Date startDate,Date endDate,String format){
//        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
//        proxyHomePageReport.setStaticsTimes(format);
//        proxyHomePageReport.setStaticsYear(format.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_4));
//        proxyHomePageReport.setStaticsMonth(format.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_7));
//        proxyHomePageReportService.chargeOrder(proxyUser,startDate,endDate,proxyHomePageReport);
//        proxyHomePageReportService.withdrawOrder(proxyUser,startDate,endDate,proxyHomePageReport);
//        proxyHomePageReportService.gameRecord(proxyUser,startTime,endTime,proxyHomePageReport);
//        proxyHomePageReportService.getNewUsers(proxyUser,startDate,endDate,proxyHomePageReport);
//        proxyHomePageReport.setProxyUserId(proxyUser.getId());
//        proxyHomePageReport.setProxyRole(proxyUser.getProxyRole());
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_3){
//            proxyHomePageReport.setFirstProxy(proxyUser.getFirstProxy());
//            proxyHomePageReport.setSecondProxy(proxyUser.getSecondProxy());
//        }
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_2){
//            proxyHomePageReport.setFirstProxy(proxyUser.getFirstProxy());
//            proxyHomePageReport.setSecondProxy(proxyUser.getId());
//            proxyHomePageReportService.getNewThirdProxys(proxyUser,startDate,endDate,proxyHomePageReport);
//        }
//        if (proxyUser.getProxyRole() == CommonConst.NUMBER_1){
//            proxyHomePageReport.setFirstProxy(proxyUser.getId());
//            proxyHomePageReport.setSecondProxy(CommonConst.LONG_0);
//            proxyHomePageReportService.getNewThirdProxys(proxyUser,startDate,endDate,proxyHomePageReport);
//            proxyHomePageReportService.getNewSecondProxys(proxyUser,startDate,endDate,proxyHomePageReport);
//        }
//        proxyHomePageReportService.save(proxyHomePageReport);
//    }
//}

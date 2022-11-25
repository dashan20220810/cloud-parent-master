package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Component public class UserRunningWaterTask {
    //    public final static String start = ":00:00";
    //
    //    public final static String end = ":59:59";

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    //    public final static Integer startHour = 0;
    //
    //    public final static Integer endHour = 23;

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @Scheduled(cron = TaskConst.USER_RUNNING_WATER) public void create() {
        log.info("每日会员流水报表统计开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.DATE, -1);
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        List<UserRunningWater> byStaticsTimes = userRunningWaterService.findByStaticsTimes(format);
        if (!LoginUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0)
            return;
        try {
            //            for (int i = startHour; i <= endHour; i++) {
            //                String s = i < CommonConst.NUMBER_10 ? " 0" + i : " " + i;
            //                String startTime = format + s + start;
            //                String endTime = format + s + end;
            //                    Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
            //                    Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
            //                this.gameRecord(startTime, endTime, format);
            //                this.findSumBetAmount(startTime, endTime, format);
            //                this.shareProfitChange(format, startTime, endTime);
            //            }
            String startTime = format + start;
            String endTime = format + end;
            //            this.gameRecord(startTime, endTime, format);
            //            this.findSumBetAmount(startTime, endTime, format);
            this.sumUserRunningWater(format);
            this.shareProfitChange(format, startTime, endTime);
            log.info("每日会员流水报表统计结束end=============================================》");
        } catch (Exception ex) {
            log.error("每日会员流水报表统计失败", ex);
        }
    }

    public void sumUserRunningWater(String format){
        try {
            List<Map<String, Object>> gameRecords = userGameRecordReportService.sumUserRunningWater(format, format);
            if (gameRecords == null || gameRecords.size() == CommonConst.NUMBER_0) {
                return;
            }
            gameRecords.stream().forEach(item -> {
                Long userId = Long.valueOf(item.get("userId").toString());
                BigDecimal validbet = new BigDecimal(item.get("validbet").toString());
                User user = userService.findById(userId);
                if (LoginUtil.checkNull(user) || LoginUtil.checkNull(user.getFirstProxy())) {
                    userRunningWaterService
                        .updateKey(userId, format, validbet, BigDecimal.ZERO, CommonConst.LONG_0, CommonConst.LONG_0,
                            CommonConst.LONG_0);
                } else {
                    userRunningWaterService.updateKey(userId, format, validbet, BigDecimal.ZERO, user.getFirstProxy(),
                        user.getSecondProxy(), user.getThirdProxy());
                }
            });
            gameRecords.clear();
        }catch (Exception ex){
            log.error("用户流水统计失败{}", ex);
        }
    }

    //    public void gameRecord(String startTime, String endTime, String format) {
    //        try {
    //            List<Map<String, Object>> gameRecords = gameRecordService.findGameRecords(startTime, endTime);
    //            if (gameRecords == null || gameRecords.size() == CommonConst.NUMBER_0) {
    //                return;
    //            }
    //            gameRecords.stream().forEach(item -> {
    //                Long userId = Long.valueOf(item.get("userId").toString());
    //                BigDecimal validbet = new BigDecimal(item.get("validbet").toString());
    //                User user = userService.findById(userId);
    //                if (LoginUtil.checkNull(user) || LoginUtil.checkNull(user.getFirstProxy())) {
    //                    userRunningWaterService
    //                        .updateKey(userId, format, validbet, BigDecimal.ZERO, CommonConst.LONG_0, CommonConst.LONG_0,
    //                            CommonConst.LONG_0);
    //                } else {
    //                    userRunningWaterService.updateKey(userId, format, validbet, BigDecimal.ZERO, user.getFirstProxy(),
    //                        user.getSecondProxy(), user.getThirdProxy());
    //                }
    //            });
    //            gameRecords.clear();
    //        } catch (Exception ex) {
    //            log.error("用户流水统计WM三方游戏注单失败{}", ex);
    //        }
    //    }
    //
    //    public void findSumBetAmount(String startTime, String endTime, String format) {
    //        try {
    //            List<Map<String, Object>> sumBetAmount = gameRecordGoldenFService.findSumBetAmount(startTime, endTime);
    //            if (sumBetAmount == null || sumBetAmount.size() == CommonConst.NUMBER_0) {
    //                return;
    //            }
    //            sumBetAmount.stream().forEach(item -> {
    //                Long userId = Long.valueOf(item.get("userId").toString());
    //                BigDecimal betAmount = new BigDecimal(item.get("betAmount").toString());
    //                User user = userService.findById(userId);
    //                if (LoginUtil.checkNull(user) || LoginUtil.checkNull(user.getFirstProxy())) {
    //                    userRunningWaterService
    //                        .updateKey(userId, format, betAmount, BigDecimal.ZERO, CommonConst.LONG_0, CommonConst.LONG_0,
    //                            CommonConst.LONG_0);
    //                } else {
    //                    userRunningWaterService.updateKey(userId, format, betAmount, BigDecimal.ZERO, user.getFirstProxy(),
    //                        user.getSecondProxy(), user.getThirdProxy());
    //                }
    //            });
    //            sumBetAmount.clear();
    //        } catch (Exception ex) {
    //            log.error("用户流水统计PG/CQ9三方游戏注单失败{}", ex);
    //        }
    //    }

    public void shareProfitChange(String format, String startDate, String endDate) {
        try {
            List<Map<String, Object>> sumAmount = shareProfitChangeService.findSumAmount(startDate, endDate);
            if (sumAmount == null || sumAmount.size() == CommonConst.NUMBER_0) {
                return;
            }
            sumAmount.stream().forEach(item -> {
                Long userId = Long.valueOf(item.get("fromUserId").toString());
                BigDecimal amount = new BigDecimal(item.get("amount").toString());
                User user = userService.findById(userId);
                if (LoginUtil.checkNull(user) || LoginUtil.checkNull(user.getFirstProxy())) {
                    userRunningWaterService
                        .updateKey(userId, format, BigDecimal.ZERO, amount, CommonConst.LONG_0, CommonConst.LONG_0,
                            CommonConst.LONG_0);
                } else {
                    userRunningWaterService
                        .updateKey(userId, format, BigDecimal.ZERO, amount, user.getFirstProxy(), user.getSecondProxy(),
                            user.getThirdProxy());
                }
            });
            sumAmount.clear();
        } catch (Exception ex) {
            log.error("用户流水统计人人代佣金失败{}", ex);
        }
    }
}

package com.qianyi.casinoadmin.task;

import com.qianyi.casinoadmin.model.HomePageReport;
import com.qianyi.casinoadmin.service.HomePageReportService;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.co.charge.ChargeOrderCo;
import com.qianyi.casinocore.co.withdrwa.WithdrawOrderCo;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class HomePageReportTask {
    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private HomePageReportService homePageReportService;

    @Autowired
    private WashCodeChangeService washCodeChangeService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private ExtractPointsChangeService extractPointsChangeService;

    @Scheduled(cron = TaskConst.HOME_PAGE_REPORT_NEW)
    public void create(){
        log.info("每日首页报表统计开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        String today = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        nowTime.add(Calendar.DATE, -1);
        String yesterday = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        List<HomePageReport> byStaticsTimes = homePageReportService.findByStaticsTimes(yesterday);
        if (!LoginUtil.checkNull(byStaticsTimes) && byStaticsTimes.size() > CommonConst.NUMBER_0)
            return;
        this.begin(yesterday,today);
    }
    public void begin(String yesterday,String today){
        try {
            String startTime = yesterday + start;
            String endTime = today + end;
            Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
            Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
            HomePageReport homePageReport = new HomePageReport();
            homePageReport.setStaticsTimes(yesterday);
            homePageReport.setStaticsMonth(yesterday.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_7));
            homePageReport.setStaticsYear(yesterday.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_4));
            this.chargeOrder(startDate,endDate,homePageReport);
            this.withdrawOrder(startDate,endDate,homePageReport);
            this.shareProfitChange(startDate,endDate,homePageReport);
            this.getNewUsers(startDate,endDate,homePageReport);
            this.bonusAmount(startDate,endDate,homePageReport);
            this.washCodeAmount(startDate,endDate,homePageReport);
            this.extractPointsAmount(startDate, endDate, homePageReport);
            homePageReportService.save(homePageReport);
            log.info("每日首页报表统计结束end=============================================》");
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
        }
    }
    public void chargeOrder(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            ChargeOrderCo co = new ChargeOrderCo();
            co.setStartDate(startDate);
            co.setEndDate(endDate);
            List<ChargeOrder> chargeOrders = chargeOrderService.findSuccessedListByUpdate(co);
            if (LoginUtil.checkNull(chargeOrders) || chargeOrders.size() == CommonConst.NUMBER_0){
                homePageReport.setChargeAmount(BigDecimal.ZERO);
                homePageReport.setChargeNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal chargeAmount = chargeOrders.stream().map(ChargeOrder::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setChargeAmount(chargeAmount);
            homePageReport.setChargeNums(chargeOrders.size());
            chargeOrders.clear();
        }catch (Exception ex){
            log.error("统计充值订单失败",ex);
        }
    }
    public void withdrawOrder(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            WithdrawOrderCo co = new WithdrawOrderCo();
            co.setStartDate(startDate);
            co.setEndDate(endDate);
            List<WithdrawOrder> withdrawOrders = withdrawOrderService.findSuccessedListByUpdate(co);
            if (LoginUtil.checkNull(withdrawOrders) || withdrawOrders.size() == CommonConst.NUMBER_0){
                homePageReport.setWithdrawMoney(BigDecimal.ZERO);
                homePageReport.setWithdrawNums(CommonConst.NUMBER_0);
                return;
            }
            BigDecimal withdrawMoney = withdrawOrders.stream().map(WithdrawOrder::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal serviceCharge = withdrawOrders.stream().map(WithdrawOrder::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setWithdrawMoney(withdrawMoney);
            homePageReport.setWithdrawNums(withdrawOrders.size());
            homePageReport.setServiceCharge(serviceCharge.add(homePageReport.getServiceCharge()));
            withdrawOrders.clear();
        }catch (Exception ex){
            log.error("统计提现订单失败",ex);
        }
    }
    //    public void gameRecord(String startTime,String endTime,HomePageReport homePageReport){
    //        try {
    //            Map<String, Object> gameRecordSum = gameRecordService.findSumBetAndWinLoss(startTime, endTime);
    //            BigDecimal gameRecordValidbet = gameRecordSum.get("validbet") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("validbet").toString());
    //            BigDecimal gameRecordWinLoss = gameRecordSum.get("winLoss") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("winLoss").toString());
    //            Set<Long> gameRecordUser = gameRecordService.findGroupByUser(startTime, endTime);
    //            Map<String, Object> gameRecordGoldenFSum = gameRecordGoldenFService.findSumBetAndWinLoss(startTime, endTime);
    //            BigDecimal gameRecordGoldenFValidbet = gameRecordGoldenFSum.get("betAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("betAmount").toString());
    //            BigDecimal gameRecordGoldenFWinLoss = gameRecordGoldenFSum.get("winAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("winAmount").toString());
    //            Set<Long> gameRecordGoldenFUser = gameRecordGoldenFService.findGroupByUser(startTime,endTime);
    //            homePageReport.setValidbetAmount(gameRecordValidbet.add(gameRecordGoldenFValidbet));
    //            homePageReport.setWinLossAmount(gameRecordWinLoss.add(gameRecordGoldenFWinLoss));
    //            gameRecordUser.addAll(gameRecordGoldenFUser);
    //            homePageReport.setActiveUsers(gameRecordUser.size());
    //        }catch (Exception ex){
    //            log.error("统计三方游戏注单失败",ex);
    //        }
    //    }
    public void shareProfitChange(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(null,null, startDate, endDate);
            if (LoginUtil.checkNull(shareProfitChanges) || shareProfitChanges.size() == CommonConst.NUMBER_0){
                homePageReport.setShareAmount(BigDecimal.ZERO);
                return;
            }
            BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setShareAmount(contribution);
            shareProfitChanges.clear();
        }catch (Exception ex){
            log.error("统计人人代佣金失败",ex);
        }
    }
    public void getNewUsers(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            User user = new User();
            Long userCount = userService.findUserCount(user, startDate, endDate);
            homePageReport.setNewUsers(Math.toIntExact(userCount));
        }catch (Exception ex){
            log.error("统计新增用户失败",ex);
        }
    }
    public void bonusAmount(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            homePageReport.setBonusAmount(BigDecimal.ZERO);
        }catch (Exception ex){
            log.error("统计红利失败",ex);
        }
    }
    public void washCodeAmount(Date startDate,Date endDate,HomePageReport homePageReport){
        try {
            List<WashCodeChange> washCodeChanges = washCodeChangeService.findUserList(startDate, endDate);
            if (LoginUtil.checkNull(washCodeChanges) || washCodeChanges.size() == CommonConst.NUMBER_0){
                homePageReport.setWashCodeAmount(BigDecimal.ZERO);
                return;
            }
            BigDecimal amount = washCodeChanges.stream().map(WashCodeChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            homePageReport.setWashCodeAmount(amount);
            washCodeChanges.clear();
        }catch (Exception ex){
            log.error("统计洗码金额失败",ex);
        }
    }

    /**
     * 统计抽点金额
     *
     * @param startDate      入参释义
     * @param endDate        入参释义
     * @param homePageReport 入参释义
     * @author lance
     * @since 2022 -02-22 19:29:31
     */
    public void extractPointsAmount(Date startDate, Date endDate, HomePageReport homePageReport) {
        try {
            List<ExtractPointsChange> changes = extractPointsChangeService.findBetween(startDate, endDate);
            if (changes.isEmpty()) {
                // 设置抽点总额
                homePageReport.setExtractPointsAmount(BigDecimal.ZERO);
                return ;
            }
            BigDecimal amount = changes.stream().map(ExtractPointsChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 设置抽点总额
            homePageReport.setExtractPointsAmount(amount);
            changes.clear();
        } catch (Exception ex) {
            log.error("统计抽点金额失败", ex);
        }
    }
}

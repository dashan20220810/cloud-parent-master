package com.qianyi.casinoadmin.controller;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinoadmin.service.HomePageReportService;
import com.qianyi.casinoadmin.task.HomePageReportTask;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.HomePageReportVo;
import com.qianyi.casinoadmin.model.HomePageReport;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "首页报表")
@RestController
@Slf4j
@RequestMapping("homePageReport")
public class HomePageReportController {
    @Autowired
    private HomePageReportService homePageReportService;

    @Autowired
    private TotalPlatformQuotaRecordService totalPlatformQuotaRecordService;

    @Autowired
    private HomePageReportTask homePageReportTask;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    public final static String startMonth = "-01";

    public final static String endMonth = "-12";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private ExtractPointsChangeService extractPointsChangeService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private WashCodeChangeService washCodeChangeService;

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;
    @ApiOperation("查询首页报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tag", value = "1:今日;2:昨日;3本月:;4:总计", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    @NoAuthorization
    public ResponseEntity<HomePageReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,Integer tag){
        HomePageReportVo homePageReportVo = null;
        if (LoginUtil.checkNull(tag)){
            tag = CommonConst.NUMBER_1;
        }
        try {
            //统计充值，提款，洗码等
            homePageReportVo = this.findHomePageReportVo(tag);

            //统计有效投注，输赢，活跃人数
            if (LoginUtil.checkNull(startDate,endDate)){//不传时间，统计所有
                homePageReportVo = gameRecord(homePageReportVo);
            }else {//传时间，统计时间往后推12小时
                String startTime = DateUtil.getSimpleDateFormat1().format(startDate);
                String endTime = DateUtil.getSimpleDateFormat1().format(endDate);

                Calendar nowTime = Calendar.getInstance();
                nowTime.setTime(startDate);
                nowTime.add(Calendar.HOUR, 12);
                startDate = nowTime.getTime();
                String startTimeStr = DateUtil.dateToPatten(startDate);
                nowTime.setTime(endDate);
                nowTime.add(Calendar.HOUR, 12);
                endDate = nowTime.getTime();
                String endTimeStr = DateUtil.dateToPatten(endDate);
                homePageReportVo = gameRecord(startTimeStr,endTimeStr,homePageReportVo,startTime,endTime);
                if (tag == CommonConst.NUMBER_1 || tag == CommonConst.NUMBER_2){
                    return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
                }
            }

            // yyyy-MM-dd
            String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
            String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);

            // home_page_report
            List<HomePageReport> homePageReports = homePageReportService.findHomePageReports(startTime,endTime);
            if (LoginUtil.checkNull(homePageReports) || homePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
            }

            // 充值金额
            BigDecimal chargeAmount = homePageReports.stream().map(HomePageReport::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 充值单数
            Integer chargeNums = homePageReports.stream().mapToInt(HomePageReport::getChargeNums).sum();
            // 提款金额
            BigDecimal withdrawMoney = homePageReports.stream().map(HomePageReport::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 提款单数
            Integer withdrawNums = homePageReports.stream().mapToInt(HomePageReport::getWithdrawNums).sum();
            // 派发洗码
            //            BigDecimal washCodeAmount = homePageReports.stream().map(HomePageReport::getWashCodeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 人人代佣金
            //            BigDecimal shareAmount = homePageReports.stream().map(HomePageReport::getShareAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal bonusAmount = homePageReports.stream().map(HomePageReport::getBonusAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 抽取提款手续费
            BigDecimal serviceCharge = homePageReports.stream().map(HomePageReport::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 抽点总额
            //            BigDecimal extractPointsAmount = homePageReports.stream().map(HomePageReport::getExtractPointsAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Integer newUsers = homePageReports.stream().mapToInt(HomePageReport::getNewUsers).sum();
            homePageReportVo.setChargeAmount(homePageReportVo.getChargeAmount().add(chargeAmount));
            homePageReportVo.setWithdrawMoney(homePageReportVo.getWithdrawMoney().add(withdrawMoney));
            //            homePageReportVo.setWashCodeAmount(homePageReportVo.getWashCodeAmount().add(washCodeAmount));
            //            homePageReportVo.setShareAmount(homePageReportVo.getShareAmount().add(shareAmount));
            homePageReportVo.setBonusAmount(homePageReportVo.getBonusAmount().add(bonusAmount));
            homePageReportVo.setServiceCharge(homePageReportVo.getServiceCharge().add(serviceCharge));
            homePageReportVo.setChargeNums(chargeNums + homePageReportVo.getChargeNums());
            homePageReportVo.setWithdrawNums(withdrawNums + homePageReportVo.getWithdrawNums());
            homePageReportVo.setNewUsers(newUsers + homePageReportVo.getNewUsers());
            // 设置抽点总额
            //            homePageReportVo.setExtractPointsAmount(extractPointsAmount.add(homePageReportVo.getExtractPointsAmount()));

            this.findCompanyProxyDetails(new CompanyProxyMonth(),startTime,endTime,homePageReportVo);
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(this.getHomePageReportVo(homePageReportVo));
    }
    @ApiOperation("查找走势图")
    @GetMapping("/findTrendChart")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tag", value = "1:每日 2:每月 3:每年", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    @NoAuthorization
    public ResponseEntity<HomePageReportVo> findTrendChart(Integer tag,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate) {
        if (LoginUtil.checkNull(startDate, endDate)) {
            ResponseUtil.custom("参数必填");
        }
        List<HomePageReportVo> list = new LinkedList<>();
        try {
            if ((DateUtil.isEffectiveDate(new Date(),startDate,endDate))){
                HomePageReportVo homePageReportVo = this.findHomePageReportVo();
                list.add(homePageReportVo);
            }
            Sort sort=Sort.by("id").descending();
            String formatStart = DateUtil.getSimpleDateFormat1().format(startDate);
            String formatEnd = DateUtil.getSimpleDateFormat1().format(endDate);
            List<HomePageReport> homePageReports = homePageReportService.findHomePageReports(sort,formatStart, formatEnd);
            if (LoginUtil.checkNull(homePageReports) || homePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(list);
            }
            homePageReports.forEach(homePageReport1 -> {
                HomePageReportVo vo = new HomePageReportVo();
                BeanUtils.copyProperties(homePageReport1, vo);
                list.add(vo);
            });
            if (LoginUtil.checkNull(tag) || tag == CommonConst.NUMBER_1){
                // 向后偏移12小时
                startDate = cn.hutool.core.date.DateUtil.offsetHour(startDate, 12);
                endDate = cn.hutool.core.date.DateUtil.offsetHour(endDate, 12);
                String startTimeStr = cn.hutool.core.date.DateUtil.formatDateTime(startDate);
                String endTimeStr = cn.hutool.core.date.DateUtil.formatDateTime(endDate);
                Map<String, Map<String, BigDecimal>> betMap = this.getBetMap(formatStart, formatEnd);
                Map<String, BigDecimal> pointsChangeMap = this.getPointsChangeMap(startTimeStr, endTimeStr);
                Map<String, BigDecimal> shareProfitMap = this.getShareProfitMap(startTimeStr, endTimeStr);
                Map<String, BigDecimal> washCodeMap = this.getWashCodeMap(startTimeStr, endTimeStr);
                list.forEach(vo -> {
                    String staticsTimes = vo.getStaticsTimes();
                    Map<String, BigDecimal> stringBigDecimalMap = betMap.get(staticsTimes);
                    if (CollUtil.isNotEmpty(stringBigDecimalMap)){
                        vo.setValidbetAmount(stringBigDecimalMap.get("validAmount"));
                        vo.setWinLossAmount(stringBigDecimalMap.get("winLoss"));
                    }
                    vo.setExtractPointsAmount(pointsChangeMap.get(staticsTimes)==null?BigDecimal.ZERO:pointsChangeMap.get(staticsTimes));
                    vo.setShareAmount(shareProfitMap.get(staticsTimes)==null?BigDecimal.ZERO:shareProfitMap.get(staticsTimes));
                    vo.setWashCodeAmount(washCodeMap.get(staticsTimes)==null?BigDecimal.ZERO:washCodeMap.get(staticsTimes));
                    this.getHomePageReportVo(vo);
                    if (vo.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || vo.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
                        vo.setOddsRatio(vo.getChargeAmount());
                    }else {
                        BigDecimal divide = vo.getChargeAmount().divide(vo.getValidbetAmount(), 4, RoundingMode.HALF_UP);
                        vo.setOddsRatio(divide);
                    }
                    vo.setTime(vo.getStaticsTimes());
                });

                //                list.forEach(vo -> {
                //                    Calendar calendar = Calendar.getInstance();
                //                    Date date = null;
                //                    try {
                //                        date = DateUtil.getSimpleDateFormat1().parse(vo.getStaticsTimes());
                //                    } catch (ParseException e) {
                //                        e.printStackTrace();
                //                    }
                //                    calendar.setTime(date);
                //                    String firstDay = DateUtil.dateToPatten1(calendar.getTime()) + start;
                //                    calendar.add(Calendar.DATE, 1);
                //                    String lastDay = DateUtil.dateToPatten1(calendar.getTime()) + end;
                //                    this.gameRecordMoney(firstDay,lastDay,vo,vo.getStaticsTimes(),vo.getStaticsTimes());
                //                    this.getHomePageReportVo(vo);
                //                    if (vo.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || vo.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
                //                        vo.setOddsRatio(vo.getChargeAmount());
                //                    }else {
                //                        BigDecimal divide = vo.getChargeAmount().divide(vo.getValidbetAmount(), 4, RoundingMode.HALF_UP);
                //                        vo.setOddsRatio(divide);
                //                    }
                //                    vo.setTime(vo.getStaticsTimes());
                //                });
                Collections.reverse(list);
                return ResponseUtil.success(list);
            }else if (tag == CommonConst.NUMBER_2){
                Map<String, List<HomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(HomePageReportVo::getStaticsMonth));
                Map<String,List<HomePageReportVo>> result = new TreeMap<>();
                map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x->result.put(x.getKey(),x.getValue()));
                list.clear();
                result.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(value,key,CommonConst.NUMBER_2));
                });
            }else {
                Map<String, List<HomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(HomePageReportVo::getStaticsYear));
                list.clear();
                map.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(value,key,CommonConst.NUMBER_3));
                });
                Collections.reverse(list);
            }
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(list);
    }

    private HomePageReportVo findHomePageReportVo(Integer tag)
        throws ParseException {
        HomePageReportVo homePageReportVo = null;
        Calendar nowTime = Calendar.getInstance();
        int hour = nowTime.get(Calendar.HOUR_OF_DAY);
        String today = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        if (tag == CommonConst.NUMBER_1 || (hour >= CommonConst.NUMBER_12 && tag != CommonConst.NUMBER_2)){
            nowTime.add(Calendar.DATE, 1);
            String tomorrow = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
            homePageReportVo = this.assemble(today,tomorrow);
        }else {
            nowTime.add(Calendar.DATE, -1);
            String yesterday = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
            homePageReportVo = this.assemble(yesterday,today);
        }
        return homePageReportVo;
    }

    private HomePageReportVo findHomePageReportVo()
        throws ParseException {
        HomePageReportVo homePageReportVo = null;
        Calendar nowTime = Calendar.getInstance();
        int hour = nowTime.get(Calendar.HOUR_OF_DAY);
        String today = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        if (hour >= CommonConst.NUMBER_12){
            nowTime.add(Calendar.DATE, 1);
            String tomorrow = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
            homePageReportVo = this.assemble(today,tomorrow);
        }else {
            nowTime.add(Calendar.DATE, -1);
            String yesterday = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
            homePageReportVo = this.assemble(yesterday,today);
        }
        return homePageReportVo;
    }

    @ApiOperation("查找在线人数")
    @GetMapping("/findOnlineUser")
    @NoAuthorization
    public ResponseEntity findOnlineUser(){
        Set<String> keys = redisTemplate.keys("token::casino-web::*");
        Integer count = 0;
        try {
            for (String s: keys){
                JjwtUtil.Token refreshJwtToken = (JjwtUtil.Token)redisTemplate.opsForValue().get(s);
                if (LoginUtil.checkNull(refreshJwtToken)){
                    continue;
                }
                if (JjwtUtil.check(LoginUtil.checkNull(refreshJwtToken.getNewToken())?refreshJwtToken.getOldToken():refreshJwtToken.getNewToken(), Constants.CASINO_WEB)) {
                    count++;
                }
            }
            keys.clear();
        }catch (Exception ex){
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(count);
    }

    private HomePageReportVo getHomePageReportVo(List<HomePageReportVo> list,String time,Integer tag){
        HomePageReportVo vo = new HomePageReportVo();
        try {
            if (tag == CommonConst.NUMBER_2){
                Calendar calendar = Calendar.getInstance();
                Date date = DateUtil.getSimpleDateFormatMonth().parse(time);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_MONTH,1);
                String startTimeStr = DateUtil.dateToPatten1(calendar.getTime());
                String firstDay = startTimeStr + start;
                calendar.add(Calendar.MONTH, 1);
                String lastDay = DateUtil.dateToPatten1(calendar.getTime()) + end;

                calendar.add(Calendar.DATE, -1);
                this.gameRecordMoney(firstDay,lastDay,vo,startTimeStr,DateUtil.dateToPatten1(calendar.getTime()));
                this.findCompanyProxyDetails(new CompanyProxyMonth(),time,time,vo);
            }else if (tag == CommonConst.NUMBER_3){
                Calendar calendar = Calendar.getInstance();
                Date date = DateUtil.getSimpleDateFormatYear().parse(time);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_YEAR,1);
                String startTimeStr = DateUtil.dateToPatten1(calendar.getTime());
                String firstDay = startTimeStr + start;
                calendar.add(Calendar.YEAR, 1);
                String lastDay = DateUtil.dateToPatten1(calendar.getTime()) + end;

                calendar.add(Calendar.DATE, -1);
                this.gameRecordMoney(firstDay,lastDay,vo,startTimeStr,DateUtil.dateToPatten1(calendar.getTime()));
                this.findCompanyProxyDetails(new CompanyProxyMonth(),time + startMonth,time + endMonth,vo);
            }
        }catch (ParseException ex){
            log.error("admin首页报表统计失败{}",ex);
        }

        BigDecimal chargeAmount = list.stream().map(HomePageReportVo::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setChargeAmount(chargeAmount);

        BigDecimal validbetAmount = vo.getValidbetAmount();

        //        BigDecimal shareAmount = list.stream().map(HomePageReportVo::getShareAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //        vo.setShareAmount(shareAmount);

        BigDecimal bonusAmount = list.stream().map(HomePageReportVo::getBonusAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setBonusAmount(bonusAmount);

        BigDecimal serviceCharge = list.stream().map(HomePageReportVo::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setServiceCharge(serviceCharge);

        BigDecimal proxyProfit = list.stream().map(HomePageReportVo::getProxyProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setProxyProfit(proxyProfit);

        //        BigDecimal washCodeAmount = list.stream().map(HomePageReportVo::getWashCodeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //        vo.setWashCodeAmount(washCodeAmount);

        //        BigDecimal extractPointsAmount = list.stream().map(HomePageReportVo::getExtractPointsAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //        vo.setExtractPointsAmount(extractPointsAmount);

        vo = this.getHomePageReportVo(vo);

        if (validbetAmount.compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || chargeAmount.compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            vo.setOddsRatio(chargeAmount);
        }else {
            BigDecimal divide = chargeAmount.divide(validbetAmount, 4, RoundingMode.HALF_UP);
            vo.setOddsRatio(divide);
        }
        vo.setTime(time);
        return vo;
    }

    private void findCompanyProxyDetails(CompanyProxyMonth companyProxyMonth, String startTime, String endTime, HomePageReportVo homePageReportVo){
        List<CompanyProxyMonth> companyProxyMonths = companyProxyMonthService.findCompanyProxyMonths(companyProxyMonth, startTime, endTime);
        if (LoginUtil.checkNull(companyProxyMonths) || companyProxyMonths.size() == CommonConst.NUMBER_0){
            homePageReportVo.setProxyProfit(BigDecimal.ZERO);
            return;
        }
        companyProxyMonths = companyProxyMonths.stream().filter(companyProxy -> !companyProxy.getStaticsTimes().equals(homePageReportVo.getStaticsMonth())).collect(Collectors.toList());
        BigDecimal profitAmount = companyProxyMonths.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        homePageReportVo.setProxyProfit(profitAmount);
    }

    private HomePageReportVo assemble(String startStr,String endStr) throws ParseException {
        String startTime = startStr + start;
        String endTime = endStr + end;
        Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
        Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
        HomePageReport homePageReport = new HomePageReport();
        homePageReport.setStaticsTimes(startStr);
        homePageReport.setStaticsYear(startStr.substring(CommonConst.NUMBER_0, CommonConst.NUMBER_4));
        homePageReport.setStaticsMonth(startStr.substring(CommonConst.NUMBER_0, CommonConst.NUMBER_7));
        homePageReportTask.chargeOrder(startDate, endDate, homePageReport);
        homePageReportTask.withdrawOrder(startDate, endDate, homePageReport);
        //        homePageReportTask.shareProfitChange(startDate, endDate, homePageReport);
        homePageReportTask.getNewUsers(startDate, endDate, homePageReport);
        homePageReportTask.bonusAmount(startDate, endDate, homePageReport);
        //        homePageReportTask.washCodeAmount(startDate, endDate, homePageReport);
        //        homePageReportTask.extractPointsAmount(startDate, endDate, homePageReport);
        HomePageReportVo homePageReportVo = new HomePageReportVo();
        BeanUtils.copyProperties(homePageReport, homePageReportVo);
        return homePageReportVo;
    }
    private HomePageReportVo  gameRecordMoney(String startTime, String endTime, HomePageReportVo homePageReportVo,String startTimeStr,String endTimeStr){
        try {
            //            Map<String, Object> gameRecordSum = gameRecordService.findSumBetAndWinLoss(startTime, endTime);
            //            BigDecimal gameRecordValidbet = gameRecordSum.get("validbet") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("validbet").toString());
            //            BigDecimal gameRecordWinLoss = gameRecordSum.get("winLoss") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("winLoss").toString());
            //
            //            Map<String, Object> gameRecordGoldenFSum = gameRecordGoldenFService.findSumBetAndWinLoss(startTime, endTime);
            //            BigDecimal gameRecordGoldenFValidbet = gameRecordGoldenFSum.get("betAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("betAmount").toString());
            //            BigDecimal gameRecordGoldenFWinLoss = gameRecordGoldenFSum.get("winAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("winAmount").toString());
            //
            //            homePageReportVo.setValidbetAmount(gameRecordValidbet.add(gameRecordGoldenFValidbet));
            //            homePageReportVo.setWinLossAmount(BigDecimal.ZERO.subtract(gameRecordWinLoss).subtract(gameRecordGoldenFWinLoss));

            Map<String, Object> gameRecordMap = proxyGameRecordReportService.findSumBetAndWinLoss(startTimeStr,endTimeStr);
            //有效投注
            homePageReportVo.setValidbetAmount(new BigDecimal(gameRecordMap.get("validAmount").toString()));
            //输赢，以平台维度取反
            homePageReportVo.setWinLossAmount(BigDecimal.ZERO.subtract(new BigDecimal(gameRecordMap.get("winLoss").toString())));

            BigDecimal extractPointsAmount = extractPointsChangeService.sumAmount(startTime, endTime);
            homePageReportVo.setExtractPointsAmount(extractPointsAmount);

            BigDecimal shareAmount = shareProfitChangeService.sumAmount(startTime, endTime);
            homePageReportVo.setShareAmount(shareAmount);

            BigDecimal washCodeAmount = washCodeChangeService.sumAmount(startTime, endTime);
            homePageReportVo.setWashCodeAmount(washCodeAmount);
            return homePageReportVo;
        }catch (Exception ex){
            log.error("统计三方游戏注单失败",ex);
        }
        return homePageReportVo;
    }

    private Map<String, Map<String, BigDecimal>>  getBetMap(String startTimeStr,String endTimeStr){
        Map<String, Map<String, BigDecimal>> map = new HashMap<>();
        try {
            List<Map<String, Object>> betAndWinLoss = proxyGameRecordReportService.findBetAndWinLoss(startTimeStr, endTimeStr);
            if (CollUtil.isNotEmpty(betAndWinLoss)) {
                betAndWinLoss.forEach(list->{
                    Map<String, BigDecimal> betMap = new HashMap<>();
                    betMap.put("validAmount",new BigDecimal(list.get("validAmount").toString()));
                    betMap.put("winLoss",BigDecimal.ZERO.subtract(new BigDecimal(list.get("winLoss").toString())));
                    map.put(list.get("orderTimes").toString(),betMap);
                });
            }
            //有效投注
            //            homePageReportVo.setValidbetAmount(new BigDecimal(gameRecordMap.get("validAmount").toString()));
            //            //输赢，以平台维度取反
            //            homePageReportVo.setWinLossAmount(BigDecimal.ZERO.subtract(new BigDecimal(gameRecordMap.get("winLoss").toString())));
            return map;
        }catch (Exception ex){
            log.error("走势图统计三方游戏注单失败",ex);
        }
        return map;
    }

    private Map<String, BigDecimal>  getPointsChangeMap(String startTime,String endTime){
        Map<String, BigDecimal> map = new HashMap<>();
        try {
            List<Map<String, Object>> mapSumAmount = extractPointsChangeService.getMapSumAmount(startTime, endTime);
            if (CollUtil.isNotEmpty(mapSumAmount)) {
                mapSumAmount.forEach(list->{
                    map.put(list.get("orderTimes").toString(),new BigDecimal(list.get("amount").toString()));
                });
            }
            return map;
        }catch (Exception ex){
            log.error("走势图统计抽点失败",ex);
        }
        return map;
    }

    private Map<String, BigDecimal>  getShareProfitMap(String startTime,String endTime){
        Map<String, BigDecimal> map = new HashMap<>();
        try {
            List<Map<String, Object>> mapSumAmount = shareProfitChangeService.getMapSumAmount(startTime, endTime);
            if (CollUtil.isNotEmpty(mapSumAmount)) {
                mapSumAmount.forEach(list->{
                    map.put(list.get("orderTimes").toString(),new BigDecimal(list.get("amount").toString()));
                });
            }
            return map;
        }catch (Exception ex){
            log.error("走势图统计人人代失败",ex);
        }
        return map;
    }

    private Map<String, BigDecimal>  getWashCodeMap(String startTime,String endTime){
        Map<String, BigDecimal> map = new HashMap<>();
        try {
            List<Map<String, Object>> mapSumAmount = washCodeChangeService.getMapSumAmount(startTime, endTime);
            if (CollUtil.isNotEmpty(mapSumAmount)) {
                mapSumAmount.forEach(list->{
                    map.put(list.get("orderTimes").toString(),new BigDecimal(list.get("amount").toString()));
                });
            }
            return map;
        }catch (Exception ex){
            log.error("走势图统计洗码失败",ex);
        }
        return map;
    }


    private HomePageReportVo  gameRecord( HomePageReportVo homePageReportVo){
        try {
            //            Map<String, Object> gameRecordSum = gameRecordService.findSumBetAndWinLoss();
            //            BigDecimal gameRecordValidbet = gameRecordSum.get("validbet") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("validbet").toString());
            //            BigDecimal gameRecordWinLoss = gameRecordSum.get("winLoss") == null?BigDecimal.ZERO:new BigDecimal(gameRecordSum.get("winLoss").toString());
            //
            //            Map<String, Object> gameRecordGoldenFSum = gameRecordGoldenFService.findSumBetAndWinLoss();
            //            BigDecimal gameRecordGoldenFValidbet = gameRecordGoldenFSum.get("betAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("betAmount").toString());
            //            BigDecimal gameRecordGoldenFWinLoss = gameRecordGoldenFSum.get("winAmount") == null?BigDecimal.ZERO:new BigDecimal(gameRecordGoldenFSum.get("winAmount").toString());

            Map<String, Object> gameRecordMap = proxyGameRecordReportService.findSumBetAndWinLoss();
            //活跃人数
            homePageReportVo.setActiveUsers(Integer.parseInt(gameRecordMap.get("num").toString()));
            //有效投注
            homePageReportVo.setValidbetAmount(new BigDecimal(gameRecordMap.get("validAmount").toString()));
            //输赢，以平台维度取反
            homePageReportVo.setWinLossAmount(BigDecimal.ZERO.subtract(new BigDecimal(gameRecordMap.get("winLoss").toString())));

            if (homePageReportVo.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || homePageReportVo.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
                homePageReportVo.setOddsRatio(homePageReportVo.getChargeAmount());
            }else {
                homePageReportVo.setOddsRatio(homePageReportVo.getChargeAmount().divide(homePageReportVo.getValidbetAmount(), 2, RoundingMode.HALF_UP));
            }
            BigDecimal extractPointsAmount = extractPointsChangeService.sumAmount();
            homePageReportVo.setExtractPointsAmount(extractPointsAmount);

            BigDecimal shareAmount = shareProfitChangeService.sumAmount();
            homePageReportVo.setShareAmount(shareAmount);

            BigDecimal washCodeAmount = washCodeChangeService.sumAmount();
            homePageReportVo.setWashCodeAmount(washCodeAmount);

            //            Set<Long> gameRecordGoldenFUser = gameRecordGoldenFService.findGroupByUser();
            //            Set<Long> gameRecordUser = gameRecordService.findGroupByUser();
            //
            //            gameRecordGoldenFUser.addAll(gameRecordUser);
            //            homePageReportVo.setActiveUsers(gameRecordGoldenFUser.size());
            //            gameRecordGoldenFUser.clear();
            return homePageReportVo;
        }catch (Exception ex){
            log.error("统计三方游戏注单失败",ex);
        }
        return homePageReportVo;
    }

    private HomePageReportVo  gameRecord(String startTime, String endTime, HomePageReportVo homePageReportVo,String start, String end){
        try {
            Map<String, Object> gameRecordMap = proxyGameRecordReportService.findSumBetAndWinLoss(start,end);
            //活跃人数
            homePageReportVo.setActiveUsers(Integer.parseInt(gameRecordMap.get("num").toString()));
            //有效投注
            homePageReportVo.setValidbetAmount(new BigDecimal(gameRecordMap.get("validAmount").toString()));
            //输赢，以平台维度取反
            homePageReportVo.setWinLossAmount(BigDecimal.ZERO.subtract(new BigDecimal(gameRecordMap.get("winLoss").toString())));

            if (homePageReportVo.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || homePageReportVo.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
                homePageReportVo.setOddsRatio(homePageReportVo.getChargeAmount());
            }else {
                homePageReportVo.setOddsRatio(homePageReportVo.getChargeAmount().divide(homePageReportVo.getValidbetAmount(), 2, RoundingMode.HALF_UP));
            }

            BigDecimal extractPointsAmount = extractPointsChangeService.sumAmount(startTime, endTime);
            homePageReportVo.setExtractPointsAmount(extractPointsAmount);

            BigDecimal shareAmount = shareProfitChangeService.sumAmount(startTime, endTime);
            homePageReportVo.setShareAmount(shareAmount);

            BigDecimal washCodeAmount = washCodeChangeService.sumAmount(startTime, endTime);
            homePageReportVo.setWashCodeAmount(washCodeAmount);
            return homePageReportVo;
        }catch (Exception ex){
            log.error("统计三方游戏注单失败",ex);
        }
        return homePageReportVo;
    }

    // 计算毛利
    private HomePageReportVo getHomePageReportVo(HomePageReportVo homePageReportVo){
        // 毛利1需要修改:
        // 毛利1 = 平台输赢金额 - 洗码 - 代理抽点
        homePageReportVo.setGrossMargin1(homePageReportVo.getWinLossAmount().subtract(homePageReportVo.getWashCodeAmount()).subtract(homePageReportVo.getExtractPointsAmount()));
        homePageReportVo.setGrossMargin2(homePageReportVo.getGrossMargin1().subtract(homePageReportVo.getShareAmount()).subtract(homePageReportVo.getBonusAmount()).add(homePageReportVo.getServiceCharge()));
        homePageReportVo.setGrossMargin3(homePageReportVo.getGrossMargin2().subtract(homePageReportVo.getProxyProfit()));
        return homePageReportVo;
    }

    @ApiOperation("平台总额度明细")
    @GetMapping("/findCommission")
    @NoAuthorization
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<TotalPlatformQuotaRecord> queryTotalPlatformQuotaRecordList(Integer pageSize, Integer pageCode,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<TotalPlatformQuotaRecord> totalPlatformQuotaRecordPage = totalPlatformQuotaRecordService.findTotalPlatformQuotaRecordPage(pageable, startDate, endDate);
        List<TotalPlatformQuotaRecord> content = totalPlatformQuotaRecordPage.getContent();
        PageResultVO<TotalPlatformQuotaRecord> pageResultVO =new PageResultVO(totalPlatformQuotaRecordPage);
        pageResultVO.setContent(content);
        return ResponseUtil.success(pageResultVO);
    }
}

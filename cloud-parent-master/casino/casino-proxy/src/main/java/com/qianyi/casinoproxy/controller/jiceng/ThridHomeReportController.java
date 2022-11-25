package com.qianyi.casinoproxy.controller.jiceng;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.casinoproxy.vo.ProxyHomePageReportVo;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "基层代理首页报表")
@RestController
@Slf4j
@RequestMapping("thridHomeReport/jiceng")
public class ThridHomeReportController {
    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;


    @ApiOperation("查询基层代理首页报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<ProxyHomePageReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ProxyHomePageReportVo proxyHomePageReportVo = null;
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        try {
            ProxyHomePageReport proxyHome = new ProxyHomePageReport();
            //统计投注、充值提现
            if (CasinoProxyUtil.checkNull(startDate) || CasinoProxyUtil.checkNull(endDate)){
                proxyHomePageReportVo = this.assembleData(byId,proxyHome);
            }else {
                String startTime = DateUtil.getSimpleDateFormat1().format(startDate);
                String endTime = DateUtil.getSimpleDateFormat1().format(endDate);

                //偏移12小时
                Date start = cn.hutool.core.date.DateUtil.offsetHour(startDate, 12);
                Date end = cn.hutool.core.date.DateUtil.offsetHour(endDate, 12);
                String startTimeStr = DateUtil.dateToPatten(start);
                String endTimeStr = DateUtil.dateToPatten(end);
                proxyHomePageReportVo = this.assembleData(byId,proxyHome,startTimeStr,endTimeStr,startTime,endTime);
            }
            String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
            String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);

            CompanyProxyMonth companyProxyMonth = new CompanyProxyMonth();
            companyProxyMonth.setUserId(CasinoProxyUtil.getAuthId());
            this.findCompanyProxyDetails(companyProxyMonth,startTime,endTime,proxyHomePageReportVo);
        }catch (Exception ex){
            log.error("首页报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(proxyHomePageReportVo);
    }
    @ApiOperation("查找走势图")
    @GetMapping("/findTrendChart")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tag", value = "1:每日 2:每月 3:每年", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyHomePageReportVo> findTrendChart(Integer tag,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate) {
        if (CasinoProxyUtil.checkNull(startDate, endDate)) {
            return ResponseUtil.custom("参数必填");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        List<ProxyHomePageReportVo> list = new LinkedList<>();

        Map<Integer,String> mapDate = CommonUtil.findDates("D", startDate, endDate);
        if (CasinoProxyUtil.checkNull(mapDate) || mapDate.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success();
        }
        mapDate.forEach((k,date)->{
            ProxyHomePageReportVo vo = new ProxyHomePageReportVo();
            vo.setStaticsTimes(date);
            vo.setStaticsYear(date.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_4));
            vo.setStaticsMonth(date.substring(CommonConst.NUMBER_0,CommonConst.NUMBER_7));
            list.add(vo);
        });
        try {
            if (CasinoProxyUtil.checkNull(tag) || tag == CommonConst.NUMBER_1){
                List<ProxyHomePageReportVo> newList = new LinkedList<>();
                list.forEach(vo -> {
                    String staticsTimes = vo.getStaticsTimes();
                    Calendar calendar = Calendar.getInstance();
                    Date date = null;
                    try {
                        date = DateUtil.getSimpleDateFormat1().parse(staticsTimes);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    calendar.setTime(date);
                    String startTime = DateUtil.dateToPatten1(calendar.getTime()) + start;
                    calendar.add(Calendar.DATE, 1);
                    String endTime = DateUtil.dateToPatten1(calendar.getTime()) + end;
                    ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
                    try {
                        vo = this.trendChartData(byId,proxyHomePageReport,startTime,endTime,staticsTimes,staticsTimes);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    vo.setTime(vo.getStaticsTimes());
                    if (vo.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || vo.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
                        BigDecimal oddsRatio = vo.getChargeAmount();
                        vo.setOddsRatio(oddsRatio);
                    }else {
                        BigDecimal oddsRatio = vo.getChargeAmount().divide(vo.getValidbetAmount(),2, RoundingMode.HALF_UP);
                        vo.setOddsRatio(oddsRatio);
                    }
                    vo.setStaticsTimes(staticsTimes);
                    vo.setTime(staticsTimes);
                    newList.add(vo);
                });
                return ResponseUtil.success(newList);
            }else if (tag == CommonConst.NUMBER_2){
                Map<String, List<ProxyHomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(ProxyHomePageReportVo::getStaticsMonth));
                Map<String,List<ProxyHomePageReportVo>> result = new TreeMap<>();
                map.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(x->result.put(x.getKey(),x.getValue()));
                list.clear();
                result.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(byId,key,tag));
                });
            }else {
                Map<String, List<ProxyHomePageReportVo>> map = list.stream().collect(Collectors.groupingBy(ProxyHomePageReportVo::getStaticsYear));
                list.clear();
                map.forEach((key,value)->{
                    list.add(this.getHomePageReportVo(byId,key,tag));
                });
                Collections.reverse(list);
            }
        } catch (Exception ex) {
            log.error("基层代理首页报表查找走势图失败", ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(list);
    }
    private ProxyHomePageReportVo getHomePageReportVo(ProxyUser byId,String time,Integer tag){
        ProxyHomePageReportVo vo = new ProxyHomePageReportVo();
        try {
            if (tag == CommonConst.NUMBER_2){
                //                Date date = DateUtil.getSimpleDateFormatMonth().parse(time);
                //                Calendar calendar = Calendar.getInstance();
                //                calendar.setTime(date);
                //                calendar.set(Calendar.DAY_OF_MONTH, 1);
                //                String startTime = DateUtil.dateToPatten1(calendar.getTime()) + start;
                //
                //                calendar.add(Calendar.MONTH,1);//月增加1天
                //                calendar.add(Calendar.DAY_OF_MONTH,-1);//日期倒数一日,既得到本月最后一天
                //                String endTime = DateUtil.dateToPatten1(calendar.getTime()) + end;

                Calendar calendar = Calendar.getInstance();
                Date date = DateUtil.getSimpleDateFormatMonth().parse(time);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_MONTH,1);
                String startTimeStr = DateUtil.dateToPatten1(calendar.getTime());
                String startTime = startTimeStr + start;
                calendar.add(Calendar.MONTH, 1);
                String endTime = DateUtil.dateToPatten1(calendar.getTime()) + end;

                calendar.add(Calendar.DATE, -1);
                ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
                vo = this.trendChartData(byId,proxyHomePageReport,startTime,endTime,startTimeStr,DateUtil.dateToPatten1(calendar.getTime()));
            }else {
                //                Date date = DateUtil.getSimpleDateFormatYear().parse(time);
                //                Calendar calendar = Calendar.getInstance();
                //                calendar.setTime(date);
                //                calendar.set(Calendar.DAY_OF_YEAR,1);
                //
                //                String startTime = DateUtil.dateToPatten1(calendar.getTime()) + start;
                //
                //                int year = calendar.get(Calendar.YEAR);
                //                calendar.set(Calendar.YEAR, year);
                //                calendar.roll(Calendar.DAY_OF_YEAR, -1);
                //
                //                String endTime = DateUtil.dateToPatten1(calendar.getTime()) + end;

                Calendar calendar = Calendar.getInstance();
                Date date = DateUtil.getSimpleDateFormatYear().parse(time);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_YEAR,1);
                String startTimeStr = DateUtil.dateToPatten1(calendar.getTime());
                String startTime = startTimeStr + start;
                calendar.add(Calendar.YEAR, 1);
                String endTime = DateUtil.dateToPatten1(calendar.getTime()) + end;

                calendar.add(Calendar.DATE, -1);
                ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
                vo = this.trendChartData(byId,proxyHomePageReport,startTime,endTime,startTimeStr,DateUtil.dateToPatten1(calendar.getTime()));
            }
        }catch (ParseException px){
            log.error("基层代理首页报表统计失败{}",px);
            px.printStackTrace();
        }

        if (vo.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || vo.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            vo.setOddsRatio(vo.getChargeAmount());
        }else {
            vo.setOddsRatio(vo.getChargeAmount().divide(vo.getValidbetAmount(),2, RoundingMode.HALF_UP));
        }
        vo.setTime(time);
        return vo;
    }

    private void findCompanyProxyDetails(CompanyProxyMonth companyProxyMonth, String startTime, String endTime,ProxyHomePageReportVo proxyHomePageReportVo){
        List<CompanyProxyMonth> companyProxyMonths = companyProxyMonthService.findCompanyProxyMonths(companyProxyMonth, startTime, endTime);
        if (CasinoProxyUtil.checkNull(companyProxyMonths) || companyProxyMonths.size() == CommonConst.NUMBER_0){
            proxyHomePageReportVo.setGroupTotalProfit(BigDecimal.ZERO);
            proxyHomePageReportVo.setTotalProfit(BigDecimal.ZERO);
            return;
        }
        companyProxyMonths = companyProxyMonths.stream().filter(companyProxy -> !companyProxy.getStaticsTimes().equals(proxyHomePageReportVo.getStaticsMonth())).collect(Collectors.toList());
        BigDecimal groupTotalprofit = companyProxyMonths.stream().map(CompanyProxyMonth::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = companyProxyMonths.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        proxyHomePageReportVo.setGroupTotalProfit(groupTotalprofit);
        proxyHomePageReportVo.setTotalProfit(profitAmount);
    }

    private ProxyHomePageReportVo trendChartData(ProxyUser byId,ProxyHomePageReport proxyHomePageReport,String startTime, String endTime,String startTimeStr, String endTimeStr)throws ParseException{
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        proxyHomePageReportService.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.findGameRecord(byId, startTimeStr, endTimeStr, proxyHomePageReport);
        ProxyHomePageReportVo proxyHomePageReportVo = new ProxyHomePageReportVo();
        BeanUtils.copyProperties(proxyHomePageReport, proxyHomePageReportVo);
        return proxyHomePageReportVo;
    }

    private ProxyHomePageReportVo assembleData(ProxyUser byId,ProxyHomePageReport proxyHomePageReport,String startTime, String endTime,String startTimeStr, String endTimeStr)
        throws ParseException {
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        proxyHomePageReportService.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.withdrawOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.findGameRecord(byId, startTimeStr, endTimeStr, proxyHomePageReport);
        ProxyHomePageReportVo proxyHomePageReportVo = new ProxyHomePageReportVo();
        BeanUtils.copyProperties(proxyHomePageReport, proxyHomePageReportVo);
        return proxyHomePageReportVo;
    }

    private ProxyHomePageReportVo assembleData(ProxyUser byId,ProxyHomePageReport proxyHomePageReport){
        proxyHomePageReportService.chargeOrder(byId, null, null, proxyHomePageReport);
        proxyHomePageReportService.withdrawOrder(byId, null, null, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, null, null, proxyHomePageReport);

        proxyHomePageReportService.gameRecord(byId,proxyHomePageReport);
        ProxyHomePageReportVo proxyHomePageReportVo = new ProxyHomePageReportVo();
        BeanUtils.copyProperties(proxyHomePageReport, proxyHomePageReportVo);
        return proxyHomePageReportVo;
    }
}

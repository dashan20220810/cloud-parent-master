package com.qianyi.casinoproxy.controller.jiceng;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "基层代理代理中心")
@RestController
@Slf4j
@RequestMapping("companyProxyDetail/jiceng")
public class ThridCompanyProxyDetailController {

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String startStr = " 12:00:00";

    public final static String endStr = " 11:59:59";
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser proxy = proxyUserService.findById(authId);
        if (CasinoProxyUtil.checkNull(proxy)){
            return ResponseUtil.success();
        }
        List<CompanyProxyReportVo> list = new LinkedList<>();

        String startTime = DateUtil.getSimpleDateFormat1().format(startDate);
        String endTime = DateUtil.getSimpleDateFormat1().format(endDate);

        //偏移12小时
        Date start = cn.hutool.core.date.DateUtil.offsetHour(startDate, 12);
        Date end = cn.hutool.core.date.DateUtil.offsetHour(endDate, 12);
        try {
            CompanyProxyReportVo companyProxyReportVo = this.assembleData(proxy,start,end,startTime,endTime);
            list.add(companyProxyReportVo);
            return ResponseUtil.success(this.getCompanyProxyReportVos(list));
        }catch (Exception ex){
            log.error("代理报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
    }

    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyReportVo> findDailyDetails(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate,endDate) ){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        Long authId = CasinoProxyUtil.getAuthId();
        proxyHomeReport.setProxyUserId(authId);
        ProxyUser proxyUser = proxyUserService.findById(authId);
        if (CasinoProxyUtil.checkNull(proxyUser)){
            return ResponseUtil.success();
        }
        ProxyUser proxy = proxyUser;
        List<CompanyProxyReportVo> list = new LinkedList<>();
        try {
            Map<Integer,String> mapDate = CommonUtil.findDates("D", startDate, endDate);
            mapDate.forEach((k,today)->{
                Calendar calendar = Calendar.getInstance();
                Date  todayDate = null;
                try {
                    todayDate = DateUtil.getSimpleDateFormat1().parse(today);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.setTime(todayDate);
                calendar.add(Calendar.DATE, 1);
                String tomorrow = DateUtil.dateToPatten1(calendar.getTime());
                try {
                    Date start = DateUtil.getSimpleDateFormat().parse(today+startStr);
                    Date end = DateUtil.getSimpleDateFormat().parse(tomorrow+endStr);
                    CompanyProxyReportVo companyProxyReportVo = this.assembleData(proxy,start,end,today,today);
                    companyProxyReportVo.setStaticsTimes(today);
                    list.add(companyProxyReportVo);
                }catch (ParseException px){
                    px.printStackTrace();
                }
            });
        }catch (Exception ex){
            log.error("proxy基层代理报表每日明细查询失败",ex);
            return ResponseUtil.custom("查询失败");
        }

        return ResponseUtil.success(list);
    }
    private CompanyProxyReportVo assembleData(ProxyUser byId,Date start,Date end,String startTime,String endTime){
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();

        proxyHomePageReportService.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.withdrawOrder(byId, start, end, proxyHomePageReport);
        //        proxyHomePageReportService.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.findGameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, start, end, proxyHomePageReport);
        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
        companyProxyReportVo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
        companyProxyReportVo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
        BeanUtils.copyProperties(proxyHomePageReport, companyProxyReportVo);
        return this.assemble(companyProxyReportVo,byId);
    }

    private CompanyProxyReportVo assemble(CompanyProxyReportVo companyProxyReportVo,ProxyUser byId){
        companyProxyReportVo.setId(byId.getId());
        companyProxyReportVo.setParentId(byId.getSecondProxy());
        companyProxyReportVo.setUserName(byId.getUserName());
        companyProxyReportVo.setNickName(byId.getNickName());
        companyProxyReportVo.setProxyRole(byId.getProxyRole());
        return companyProxyReportVo;
    }

    private List<CompanyProxyReportVo> getCompanyProxyReportVos(List<CompanyProxyReportVo> list){
        if (list.size() > CommonConst.NUMBER_0){
            List<Long> collect = list.stream().map(CompanyProxyReportVo::getParentId).collect(Collectors.toList());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(collect);
            list.stream().forEach(companyProxyReportVo -> {
                proxyUsers.forEach(proxyUser -> {
                    if (proxyUser.getId().equals(companyProxyReportVo.getParentId())){
                        companyProxyReportVo.setSuperiorProxyAccount(proxyUser.getUserName());
                    }
                });
            });
            collect.clear();
            proxyUsers.clear();
        }
        return list;
    }
}

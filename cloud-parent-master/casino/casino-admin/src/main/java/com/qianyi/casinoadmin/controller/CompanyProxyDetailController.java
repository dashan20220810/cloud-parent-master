package com.qianyi.casinoadmin.controller;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@Slf4j
@RestController
@RequestMapping("companyProxyDetail")
public class CompanyProxyDetailController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    public final static String startStr = " 12:00:00";

    public final static String endStr = " 11:59:59";
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
        @ApiImplicitParam(name = "sort", value = "1 正序 2 倒序", required = false),
        @ApiImplicitParam(name = "content", value = "1 团队充值 2 团队提款", required = false),
    })
    public ResponseEntity<CompanyProxyReportVo> find(Integer pageSize, Integer pageCode,Integer proxyRole, Integer tag, String userName,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate,Integer content,Integer sort){
        if (LoginUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser proxyUser = new ProxyUser();
        Sort sorts=Sort.by("proxyRole").ascending();
        sorts = sorts.and(Sort.by("id").descending());
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sorts);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        if (!LoginUtil.checkNull(userName)){
            if (LoginUtil.checkNull(tag)){
                return ResponseUtil.custom("参数不合法");
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_0){
                proxyUser.setUserName(userName);
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                }else if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    proxyUser.setFirstProxy(byUserName.getId());
                }else {
                    proxyUser.setId(byUserName.getId());
                }
            }
        }

        String startTime = DateUtil.getSimpleDateFormat1().format(startDate);
        String endTime = DateUtil.getSimpleDateFormat1().format(endDate);

        //偏移12小时
        Date start = cn.hutool.core.date.DateUtil.offsetHour(startDate, 12);
        Date end = cn.hutool.core.date.DateUtil.offsetHour(endDate, 12);

        if (!LoginUtil.checkNull(content)){
            return this.getComparatorResult(proxyUser,content,sort,startDate,endDate,pageSize,pageCode);
        }
        //分页查询代理，按照分页查出来得代理，及时统计代理数据
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable,proxyUser,null,null);
        if (LoginUtil.checkNull(proxyUserPage) || proxyUserPage.getContent().size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(new PageResultVO());
        }
        PageResultVO<CompanyProxyReportVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<CompanyProxyReportVo> list = new LinkedList<>();
        try {
            proxyUserPage.getContent().forEach(proxyUser1 -> {
                CompanyProxyReportVo companyProxyReportVo = null;
                companyProxyReportVo = this.assembleData(proxyUser1,start,end,startTime,endTime);
                list.add(companyProxyReportVo);
            });
            this.getCompanyProxyReportVos(list);
            pageResultVO.setContent(list);
            return ResponseUtil.success(pageResultVO);
        }catch (Exception ex){
            log.error("admin代理报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
    }

    private ResponseEntity<CompanyProxyReportVo> getComparatorResult(ProxyUser proxyUser,Integer content,Integer sort,
        Date startDate,Date endDate,Integer pageSize, Integer pageCode){
        try {
            List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
            if (LoginUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(new PageResultVO());
            }
            if (LoginUtil.checkNull(sort)){
                sort = CommonConst.NUMBER_2;
            }
            List<CompanyProxyReportVo> list = new LinkedList<>();
            if (content == CommonConst.NUMBER_1){
                proxyUserList.forEach(proxy->{
                    ChargeOrder chargeOrder = new ChargeOrder();
                    if (CommonUtil.setParameter(chargeOrder,proxy)){
                        return;
                    }
                    chargeOrder.setStatus(CommonConst.NUMBER_1);
                    ChargeOrder pay = chargeOrderService.sumChargeOrder(chargeOrder,startDate,endDate);
                    chargeOrder.setStatus(CommonConst.NUMBER_4);
                    ChargeOrder bonusPoint  = chargeOrderService.sumChargeOrder(chargeOrder,startDate,endDate);
                    CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
                    companyProxyReportVo.setChargeAmount(pay.getChargeAmount().add(bonusPoint.getChargeAmount()));
                    companyProxyReportVo.setProxyUser(proxy);
                    list.add(companyProxyReportVo);
                });
                if (sort == CommonConst.NUMBER_2){
                    this.chargeAmountDesc(list);
                }else {
                    this.chargeAmountAsc(list);
                }

            }else {
                proxyUserList.forEach(proxy->{
                    WithdrawOrder withdrawOrder = new WithdrawOrder();
                    if (CommonUtil.setParameter(withdrawOrder,proxy)){
                        return;
                    }
                    withdrawOrder.setStatus(CommonConst.NUMBER_1);
                    WithdrawOrder drawings = withdrawOrderService.sumWithdrawOrder(withdrawOrder, startDate, endDate);
                    withdrawOrder.setStatus(CommonConst.NUMBER_4);
                    WithdrawOrder withdraw = withdrawOrderService.sumWithdrawOrder(withdrawOrder, startDate, endDate);
                    CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
                    companyProxyReportVo.setWithdrawMoney(drawings.getWithdrawMoney().add(withdraw.getWithdrawMoney()));
                    companyProxyReportVo.setProxyUser(proxy);
                    list.add(companyProxyReportVo);
                });
                if (sort == CommonConst.NUMBER_2){
                    this.withdrawMoneyDesc(list);
                }else {
                    this.withdrawMoneyAsc(list);
                }
            }
            PageVo pageVO = new PageVo(pageCode,pageSize);
            PageResultVO<CompanyProxyReportVo> pageResultVO = (PageResultVO<CompanyProxyReportVo>) CommonUtil.handlePageResult(list, pageVO);
            List<CompanyProxyReportVo> content1 = (List<CompanyProxyReportVo>)pageResultVO.getContent();
            List<CompanyProxyReportVo> newList = new LinkedList<>();
            content1.forEach(companyProxyReportVo -> {
                ProxyUser byId = companyProxyReportVo.getProxyUser();
                ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
                String startTime = cn.hutool.core.date.DateUtil.formatDateTime(startDate);
                String endTime = cn.hutool.core.date.DateUtil.formatDateTime(endDate);
                if (content == CommonConst.NUMBER_1){
                    proxyHomePageReportService.withdrawOrder(byId, startDate, endDate, proxyHomePageReport);
                    proxyHomePageReport.setChargeAmount(companyProxyReportVo.getChargeAmount());
                }else {
                    proxyHomePageReportService.chargeOrder(byId, startDate, endDate, proxyHomePageReport);
                    proxyHomePageReport.setWithdrawMoney(companyProxyReportVo.getWithdrawMoney());
                }
                proxyHomePageReportService.gameRecord(byId, startTime, endTime, proxyHomePageReport);
                proxyHomePageReportService.getNewUsers(byId, startDate, endDate, proxyHomePageReport);
                if (byId.getProxyRole() == CommonConst.NUMBER_1){
                    proxyHomePageReportService.getNewSecondProxys(byId,startDate,endDate,proxyHomePageReport);
                }
                if (byId.getProxyRole() != CommonConst.NUMBER_3){
                    proxyHomePageReportService.getNewThirdProxys(byId,startDate,endDate,proxyHomePageReport);
                }
                CompanyProxyReportVo vo = new CompanyProxyReportVo();
                BeanUtils.copyProperties(proxyHomePageReport, vo);
                vo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
                vo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
                vo.setGroupNewProxyUsers(proxyHomePageReport.getNewSecondProxys() + proxyHomePageReport.getNewThirdProxys());
                vo = setNameAndId(vo,byId);
                newList.add(vo);
            });
            list.clear();
            proxyUserList.clear();
            this.getCompanyProxyReportVos(newList);
            pageResultVO.setContent(newList);
            return ResponseUtil.success(pageResultVO);
        }catch (Exception ex){
            log.error("admin代理报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
    }

    //降序排序
    public List<CompanyProxyReportVo> withdrawMoneyDesc(List<CompanyProxyReportVo> companyProxyReportVos){
        Collections.sort(companyProxyReportVos, (o1, o2) -> -o1.getWithdrawMoney().compareTo(o2.getWithdrawMoney()));
        return companyProxyReportVos;
    }

    //升序排列
    public List<CompanyProxyReportVo> withdrawMoneyAsc(List<CompanyProxyReportVo> companyProxyReportVos){
        Collections.sort(companyProxyReportVos, (o1, o2) -> o1.getWithdrawMoney().compareTo(o2.getWithdrawMoney()));
        return companyProxyReportVos;
    }

    //降序排序
    public List<CompanyProxyReportVo> chargeAmountDesc(List<CompanyProxyReportVo> companyProxyReportVos){
        Collections.sort(companyProxyReportVos, (o1, o2) -> -o1.getChargeAmount().compareTo(o2.getChargeAmount()));
        return companyProxyReportVos;
    }

    //升序排列
    public List<CompanyProxyReportVo> chargeAmountAsc(List<CompanyProxyReportVo> companyProxyReportVos){
        Collections.sort(companyProxyReportVos, (o1, o2) -> o1.getChargeAmount().compareTo(o2.getChargeAmount()));
        return companyProxyReportVos;
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "当前id", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyReportVo> findDailyDetails(Long id,String userName,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(id) && LoginUtil.checkNull(userName)  ){
            return ResponseUtil.custom("参数不合法");
        }
        if (LoginUtil.checkNull(startDate,endDate) ){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser proxyUser = null;
        if (!LoginUtil.checkNull(id)){
            proxyUser = proxyUserService.findById(id);
        }
        if (!LoginUtil.checkNull(userName)){
            proxyUser = proxyUserService.findByUserName(userName);
        }
        if (LoginUtil.checkNull(proxyUser)){
            return ResponseUtil.success();
        }
        ProxyUser proxy = proxyUser;
        List<CompanyProxyReportVo> list = new LinkedList<>();
        Map<Integer,String> mapDate = CommonUtil.findDates("D", startDate, endDate);
        try {
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
                //                System.out.println("昨日"+yesterday+"===========今日"+today);
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
            log.error("admin代理报表每日明细查询失败",ex);
            return ResponseUtil.custom("查询失败");
        }

        //        Sort sort=Sort.by("staticsTimes").descending();
        //
        //        String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
        //        String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
        //        List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,startTime,endTime,sort);
        //        if (!LoginUtil.checkNull(companyProxyReportVo)){
        //            list.add(companyProxyReportVo);
        //        }
        //        if (!LoginUtil.checkNull(proxyHomePageReports) && proxyHomePageReports.size() > CommonConst.NUMBER_0){
        //            proxyHomePageReports.forEach(proxyHomePageReport -> {
        //                list.add(this.assemble(proxyHomePageReport));
        //            });
        //            proxyHomePageReports.clear();
        //        }
        //        Collections.reverse(list);
        return ResponseUtil.success(list);
    }
    private CompanyProxyReportVo assembleData(ProxyUser byId,Date startDate,Date endDate,String startTime,String endTime){
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();

        long start = System.currentTimeMillis();
        proxyHomePageReportService.chargeOrder(byId, startDate, endDate, proxyHomePageReport);
        log.info("综合报表统计充值耗时{}",System.currentTimeMillis()-start);
        start = System.currentTimeMillis();
        proxyHomePageReportService.withdrawOrder(byId, startDate, endDate, proxyHomePageReport);
        log.info("综合报表统计提现耗时{}",System.currentTimeMillis()-start);
        start = System.currentTimeMillis();
        //        proxyHomePageReportService.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        //        proxyHomePageReportService.sumGameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.findGameRecord(byId, startTime, endTime, proxyHomePageReport);
        log.info("综合报表统计注单耗时{}",System.currentTimeMillis()-start);
        start = System.currentTimeMillis();
        proxyHomePageReportService.getNewUsers(byId, startDate, endDate, proxyHomePageReport);
        log.info("综合报表统计用户耗时{}",System.currentTimeMillis()-start);
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyHomePageReportService.getNewSecondProxys(byId,startDate,endDate,proxyHomePageReport);
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            proxyHomePageReportService.getNewThirdProxys(byId,startDate,endDate,proxyHomePageReport);
        }
        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
        BeanUtils.copyProperties(proxyHomePageReport, companyProxyReportVo);
        companyProxyReportVo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
        companyProxyReportVo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
        companyProxyReportVo.setGroupNewProxyUsers(proxyHomePageReport.getNewSecondProxys() + proxyHomePageReport.getNewThirdProxys());
        return this.setNameAndId(companyProxyReportVo,byId);
    }

    private CompanyProxyReportVo setNameAndId(CompanyProxyReportVo companyProxyReportVo,ProxyUser byId){
        companyProxyReportVo.setId(byId.getId());
        companyProxyReportVo.setParentId(CommonConst.LONG_0);
        if (byId.getProxyRole() == CommonConst.NUMBER_3){
            companyProxyReportVo.setParentId(byId.getSecondProxy());
        }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
            companyProxyReportVo.setParentId(byId.getFirstProxy());
        }
        companyProxyReportVo.setUserName(byId.getUserName());
        companyProxyReportVo.setNickName(byId.getNickName());
        companyProxyReportVo.setProxyRole(byId.getProxyRole());
        return companyProxyReportVo;
    }
    //    private CompanyProxyReportVo assemble(ProxyHomePageReport proxyHomePageReport,Set<Long> userIdSet){
    //        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
    //        companyProxyReportVo.setChargeAmount(proxyHomePageReport.getChargeAmount());
    //        companyProxyReportVo.setWithdrawMoney(proxyHomePageReport.getWithdrawMoney());
    //        companyProxyReportVo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
    //        companyProxyReportVo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
    //        companyProxyReportVo.setGroupNewProxyUsers(proxyHomePageReport.getNewSecondProxys() + proxyHomePageReport.getNewThirdProxys());
    //        companyProxyReportVo.setUserIdSet(userIdSet);
    //        companyProxyReportVo.setActiveUsers(userIdSet == null ? CommonConst.NUMBER_0 : userIdSet.size());
    //        companyProxyReportVo.setStaticsTimes(proxyHomePageReport.getStaticsTimes());
    //        return companyProxyReportVo;
    //    }
    private CompanyProxyReportVo assemble(ProxyHomePageReport proxyHomePageReport){
        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
        companyProxyReportVo.setChargeAmount(proxyHomePageReport.getChargeAmount());
        companyProxyReportVo.setWithdrawMoney(proxyHomePageReport.getWithdrawMoney());
        companyProxyReportVo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
        companyProxyReportVo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
        companyProxyReportVo.setGroupNewProxyUsers(proxyHomePageReport.getNewSecondProxys() + proxyHomePageReport.getNewThirdProxys());
        companyProxyReportVo.setActiveUsers(proxyHomePageReport.getActiveUsers());
        companyProxyReportVo.setStaticsTimes(proxyHomePageReport.getStaticsTimes());
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

package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyLevelVo;
import com.qianyi.casinocore.vo.CompanyProxyMonthVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@Slf4j
@RequestMapping("companyProxyMonth")
public class CompanyProxyMonthController {
    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private SysUserService sysUserService;



    @Autowired
    private MessageUtil messageUtil;


    @ApiOperation("查询代理月结表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = false),
    })
    public ResponseEntity<CompanyProxyMonthVo> find(Integer pageSize, Integer pageCode, Integer proxyRole, Integer tag, String userName,
                                                    String startDate,String endDate){
        if (LoginUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        ProxyUser proxyUser = new ProxyUser();
        Sort sort=Sort.by("proxyRole").ascending();
        sort = sort.and(Sort.by("id").descending());
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        proxyUser.setProxyRole(proxyRole);
        CompanyProxyMonth companyProxyMonth = new  CompanyProxyMonth();
        companyProxyMonth.setProxyRole(proxyRole);
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
                companyProxyMonth.setUserId(byUserName.getId());
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                    companyProxyMonth.setSecondProxy(byUserName.getId());
                }else if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    proxyUser.setFirstProxy(byUserName.getId());
                    companyProxyMonth.setFirstProxy(byUserName.getId());
                }else {
                    proxyUser.setId(byUserName.getId());
                    companyProxyMonth.setUserId(byUserName.getId());
                }
            }
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable,proxyUser,null,null);
        if (LoginUtil.checkNull(proxyUserPage) || proxyUserPage.getContent().size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(new PageResultVO());
        }
        PageResultVO<CompanyProxyMonthVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<CompanyProxyMonthVo> list = new LinkedList<>();
        List<Long> proxyUserId = proxyUserPage.getContent().stream().map(ProxyUser::getId).collect(Collectors.toList());
        List<CompanyProxyMonth> companyProxyMonths = companyProxyMonthService.findCompanyProxyMonths(proxyUserId, companyProxyMonth, startDate, endDate);
//        if (LoginUtil.checkNull(companyProxyMonths) || companyProxyMonths.size() == CommonConst.NUMBER_0){
//            pageResultVO.setContent(list);
//            return ResponseUtil.success(pageResultVO);
//        }
        Map<Long, List<CompanyProxyMonth>> firstMap = companyProxyMonths.stream().collect(Collectors.groupingBy(CompanyProxyMonth::getUserId));
        companyProxyMonths.clear();
        if (startDate.equals(endDate)){
            proxyUserPage.getContent().forEach(proxy -> {
                List<CompanyProxyMonth> proxyHomes = firstMap.get(proxy.getId());
                CompanyProxyMonthVo companyProxyMonthVo = new CompanyProxyMonthVo();
                companyProxyMonthVo.setUserName(proxy.getUserName());
                companyProxyMonthVo.setNickName(proxy.getNickName());
                companyProxyMonthVo.setProxyUserId(proxy.getId());
                companyProxyMonthVo.setProxyRole(proxy.getProxyRole());
                companyProxyMonthVo.setId(CommonConst.LONG_0);
         /*       try {
                    companyProxyMonthVo.setUpdateTime(DateUtil.getSimpleDateFormatMonth().parse(startDate));
                } catch (ParseException e) {
                    log.error("代理佣金查询时间转换出错");
                }*/
                if (!LoginUtil.checkNull(proxyHomes) && proxyHomes.size() > CommonConst.NUMBER_0){
                    companyProxyMonthVo.setPlayerNum(proxyHomes.stream().mapToInt(CompanyProxyMonth::getPlayerNum).sum());
                    companyProxyMonthVo.setGroupBetAmount(proxyHomes.stream().map(CompanyProxyMonth::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    companyProxyMonthVo.setGroupTotalprofit(proxyHomes.stream().map(CompanyProxyMonth::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add));
                    companyProxyMonthVo.setProfitAmount(proxyHomes.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    // 抽点金额
                    if (proxy.getProxyRole().equals(CommonConst.NUMBER_3)) {
                        companyProxyMonthVo.setExtractPointsAmount(proxyHomes.stream()
                                .map(CompanyProxyMonth::getExtractPointsAmount)
                                // 过滤空值
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                    }

                    List<Integer> collect = proxyHomes.stream().map(CompanyProxyMonth::getSettleStatus).collect(Collectors.toList());
                    companyProxyMonthVo.setSettleStatus(Collections.min(collect));
                    CompanyProxyMonth proxyDetail = proxyHomes.get(CommonConst.NUMBER_0);
                    //全线返佣比列
                 /*   if (companyProxyMonthVo.getProxyRole() == CommonConst.NUMBER_3){
                        String profitRate="--";
                        if (!proxyDetail.getProfitRate().equals(CommonConst.STRING_0)){
                            profitRate=messageUtil.get("每")+" "+proxyDetail.getProfitAmountLine()==null?messageUtil.get("万"):proxyDetail.getProfitAmountLine()+" "+messageUtil.get("返")+" "+ Double.valueOf(proxyDetail.getProfitRate()).intValue()+messageUtil.get(CommonConst.COMPANY);
                        }
                        companyProxyMonthVo.setProfitRate(profitRate);
                        //返佣级别:根据返佣金额查询当前返佣级别
                        String profitLevel = proxyDetail.getProfitLevelNumber();
                        companyProxyMonthVo.setProfitLevel(profitLevel);
                    }*/
                    companyProxyMonthVo.setBenefitRate(proxyDetail.getBenefitRate());
                    companyProxyMonthVo.setId(proxyDetail.getId());
                    if (!LoginUtil.checkNull(proxyDetail.getUpdateBy())){
                        SysUser byId = sysUserService.findById(Long.parseLong(proxyDetail.getUpdateBy()));
                        companyProxyMonthVo.setUpdateBy(byId==null?"":byId.getUserName());
                    }
                    companyProxyMonthVo.setUpdateTime(companyProxyMonthVo.getSettleStatus()==CommonConst.NUMBER_0 ? null:proxyDetail.getUpdateTime());
                }else {
                    if (companyProxyMonthVo.getProxyRole() == CommonConst.NUMBER_3){
                        companyProxyMonthVo.setProfitRate("--");
                        companyProxyMonthVo.setProfitLevel(messageUtil.get(CommonConst.REBATE_LEVEL));
                    }
                }
                list.add(companyProxyMonthVo);
            });
        }else {
            proxyUserPage.getContent().forEach(proxy -> {
                List<CompanyProxyMonth> proxyHomes = firstMap.get(proxy.getId());
                CompanyProxyMonthVo companyProxyMonthVo = new CompanyProxyMonthVo();
                companyProxyMonthVo.setUserName(proxy.getUserName());
                companyProxyMonthVo.setNickName(proxy.getNickName());
                companyProxyMonthVo.setProxyUserId(proxy.getId());
                companyProxyMonthVo.setProxyRole(proxy.getProxyRole());
                companyProxyMonthVo.setUpdateTime(proxy.getUpdateTime());
                if (!LoginUtil.checkNull(proxyHomes) && proxyHomes.size() > CommonConst.NUMBER_0){
                    companyProxyMonthVo.setPlayerNum(proxyHomes.stream().mapToInt(CompanyProxyMonth::getPlayerNum).sum());
                    companyProxyMonthVo.setGroupBetAmount(proxyHomes.stream().map(CompanyProxyMonth::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    companyProxyMonthVo.setGroupTotalprofit(proxyHomes.stream().map(CompanyProxyMonth::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add));
                    companyProxyMonthVo.setProfitAmount(proxyHomes.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    // 抽点金额
                    if (proxy.getProxyRole().equals(CommonConst.NUMBER_3)) {
                        companyProxyMonthVo.setExtractPointsAmount(proxyHomes.stream()
                                .map(CompanyProxyMonth::getExtractPointsAmount)
                                // 过滤空值
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                    }
                    List<Integer> collect = proxyHomes.stream().map(CompanyProxyMonth::getSettleStatus).collect(Collectors.toList());
                    companyProxyMonthVo.setSettleStatus(Collections.min(collect));
                }
                list.add(companyProxyMonthVo);
            });
        }
        firstMap.clear();
        pageResultVO.setContent(list);
        return ResponseUtil.success(pageResultVO);
    }

//    @ApiOperation("统计代理月结表")
//    @GetMapping("/findSum")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
//            @ApiImplicitParam(name = "userName", value = "账号", required = false),
//            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
//            @ApiImplicitParam(name = "staticsTimes", value = "统计时段", required = true),
//    })
//    @NoAuthorization
//    public ResponseEntity<CompanyProxyMonth> findSum(Integer proxyRole, Integer tag, String userName,String staticsTimes){
//        CompanyProxyMonth companyProxyMonth = new CompanyProxyMonth();
//        companyProxyMonth.setProxyRole(proxyRole);
//        companyProxyMonth.setStaticsTimes(staticsTimes);
//        List<CompanyProxyMonth> companyProxyMonths = new ArrayList<>();
//        if (this.findCompanyProxyMonths(tag,userName,companyProxyMonth,null,companyProxyMonths)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        this.assemble(companyProxyMonths,companyProxyMonth);
//        return ResponseUtil.success(companyProxyMonth);
//    }
    @ApiOperation("结清佣金")
    @GetMapping("/update")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
    })
    @NoAuthorization
    public ResponseEntity update(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数必填");
        }
        return this.updateStatus(id);
    }
    private synchronized ResponseEntity updateStatus(Long id){
        CompanyProxyMonth byId = companyProxyMonthService.findById(id);
        if (LoginUtil.checkNull(byId)){
            return ResponseUtil.custom("无佣金可结算");
        }
        if (byId.getSettleStatus() != CommonConst.NUMBER_0){
            return ResponseUtil.custom("该数据已经被处理");
        }
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormatMonth().format(nowTime.getTime());
        if (byId.getStaticsTimes().equals(format)){
            return ResponseUtil.custom("不能结清当月");
        }
        byId.setSettleStatus(CommonConst.NUMBER_1);
        companyProxyMonthService.save(byId);
        return ResponseUtil.success();
    }

//    private void assemble(List<CompanyProxyMonth> list,CompanyProxyMonth vo){
//        if (LoginUtil.checkNull(list) && list.size() == CommonConst.NUMBER_0)
//            return;
//        BigDecimal groupBetAmount = list.stream().map(CompanyProxyMonth::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal groupTotalprofit = list.stream().map(CompanyProxyMonth::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal profitAmount = list.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//        Integer playerNum = list.stream().mapToInt(CompanyProxyMonth::getPlayerNum).sum();
//        vo.setPlayerNum(playerNum);
//        vo.setGroupBetAmount(groupBetAmount);
//        vo.setGroupTotalprofit(groupTotalprofit);
//        vo.setProfitAmount(profitAmount);
//    }
//
//    private Boolean findCompanyProxyMonths(Integer tag, String userName, CompanyProxyMonth companyProxyMonth, Sort sort, List<CompanyProxyMonth> companyProxyMonthList){
//        List<CompanyProxyMonth> companyProxyMonths;
//        if (!LoginUtil.checkNull(userName)){
//            if (LoginUtil.checkNull(tag)){
//                return true;
//            }
//            ProxyUser byUserName = proxyUserService.findByUserName(userName);
//            if (LoginUtil.checkNull(byUserName)){
//                return false;
//            }
//            if (tag == CommonConst.NUMBER_1){//包含下级
//                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
//                    companyProxyMonth.setFirstProxy(byUserName.getId());
//                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
//                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
//                    companyProxyMonth.setSecondProxy(byUserName.getId());
//                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
//                    companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
//                }else {
//                    companyProxyMonth.setThirdProxy(byUserName.getId());
//                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
//                    companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
//                }
//            }else {
//                companyProxyMonth.setUserId(byUserName.getId());
//                companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
//            }
//        }else {
//            companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
//
//        }
//        companyProxyMonthList.addAll(companyProxyMonths);
//        return false;
//    }
}

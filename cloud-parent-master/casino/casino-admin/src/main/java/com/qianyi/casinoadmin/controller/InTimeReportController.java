package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecordReportNew;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.GameRecordReportTotalVo;
import com.qianyi.casinocore.vo.GameRecordReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理实时报表")
@Slf4j
@RestController
@RequestMapping("inTimeReport")
public class InTimeReportController {

    @Autowired
    private GameRecordReportNewService gameRecordReportNewService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private MessageUtil messageUtil;

    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "代理账号", required = false),
        @ApiImplicitParam(name = "agentMark", value = "代理标识", required = false),
        @ApiImplicitParam(name = "agentId", value = "总代id", required = false),
        //            @ApiImplicitParam(name = "gid", value = "游戏类别编号 百家乐:101 龙虎:102 轮盘:103 骰宝:104 牛牛:105 番摊:107 色碟:108 鱼虾蟹:110 炸金花:111 安达巴哈:128", required = false),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<GameRecordReportVo> find(Integer pageSize, Integer pageCode, String userName,Boolean agentMark,Integer agentId, String platform, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(startDate,endDate,agentMark)){
            return ResponseUtil.custom("参数不合法");
        }
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        if (agentMark){
            pageable = PageRequest.of(0, 1000,sort);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 12);
        startDate = calendar.getTime();
        String startTime = DateUtil.dateToPatten2(startDate);
        calendar.setTime(endDate);
        calendar.add(Calendar.HOUR, 12);
        endDate = calendar.getTime();
        String endTime = DateUtil.dateToPatten2(endDate);
        GameRecordReportNew gameRecordReport = new GameRecordReportNew();
        gameRecordReport.setPlatform(platform);
        Long proxyId = null;
        Integer proxyRole = null;
        Boolean mark = true;
        if (!LoginUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            proxyId = byUserName.getId();
            proxyRole = byUserName.getProxyRole();
            if( (agentMark && proxyRole== CommonConst.NUMBER_2) || proxyRole== CommonConst.NUMBER_3){
                mark=false;
            }
        }
        Page<GameRecordReportNew> gameRecordReportPage = gameRecordReportNewService.findGameRecordReportPage(pageable, gameRecordReport, startTime, endTime,proxyId,proxyRole,agentMark,agentId);
        PageResultVO<GameRecordReportVo> pageResultVO = new PageResultVO(gameRecordReportPage);
        List<GameRecordReportNew> gameRecordReports = gameRecordReportPage.getContent();
        if(!LoginUtil.checkNull(gameRecordReports) && gameRecordReports.size() > 0){
            List<GameRecordReportVo> gameRecordReportVos = new LinkedList();
            List<Long> firsts = gameRecordReports.stream().map(GameRecordReportNew::getFirstProxy).collect(Collectors.toList());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(firsts);
            Boolean finalMark = mark;
            gameRecordReports.stream().forEach(gameRecordReport1 -> {
                GameRecordReportVo vo = new GameRecordReportVo();
                vo.setHasChildren(finalMark);
                BeanUtils.copyProperties(gameRecordReport1,vo);
                vo.setAmount(vo.getAmount().setScale(2, RoundingMode.HALF_UP));
                //                if (gameRecordReport1.getFirstProxy().equals(CommonConst.LONG_0)){
                //                    vo.setAccount(messageUtil.get("公司"));
                //                }
                if (vo.getUserAmount()==null){
                    vo.setUserAmount(BigDecimal.ZERO);
                }
                if (vo.getSurplusAmount()==null){
                    vo.setSurplusAmount(BigDecimal.ZERO);
                }
                vo.setTotalWinLossAmount(vo.getWinLossAmount().add(vo.getAmount()).add(vo.getUserAmount()).add(vo.getSurplusAmount()));
                vo.setAccountId(gameRecordReport1.getId());
                proxyUsers.stream().forEach(proxyUser -> {
                    if (gameRecordReport1.getFirstProxy().equals(proxyUser.getId())){
                        vo.setAccount(proxyUser.getUserName());
                        vo.setAccountId(proxyUser.getId());
                    }
                });
                gameRecordReportVos.add(vo);
            });
            pageResultVO.setContent(gameRecordReportVos);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("查询代理报表统计")
    @GetMapping("/findSum")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userName", value = "代理账号", required = false),
        //            @ApiImplicitParam(name = "gid", value = "游戏类别编号 百家乐:101 龙虎:102 轮盘:103 骰宝:104 牛牛:105 番摊:107 色碟:108 鱼虾蟹:110 炸金花:111 安达巴哈:128", required = false),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    @NoAuthentication
    public ResponseEntity findSum( String userName,String platform,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(startDate) ||  LoginUtil.checkNull(endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 12);
        startDate = calendar.getTime();
        String startTime = DateUtil.dateToPatten2(startDate);
        calendar.setTime(endDate);
        calendar.add(Calendar.HOUR, 12);
        endDate = calendar.getTime();
        String endTime = DateUtil.dateToPatten2(endDate);
        GameRecordReportNew gameRecordReport = new GameRecordReportNew();
        gameRecordReport.setPlatform(platform);
        Long proxyId = null;
        Integer proxyRole = null;
        if (!LoginUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            proxyId = byUserName.getId();
            proxyRole = byUserName.getProxyRole();
        }
        GameRecordReportNew recordRecordSum = gameRecordReportNewService.findRecordRecordSum(gameRecordReport, startTime, endTime, proxyId, proxyRole);
        if (!LoginUtil.checkNull(recordRecordSum)){
            recordRecordSum.setAmount(recordRecordSum.getAmount() != null? recordRecordSum.getAmount().setScale(2, RoundingMode.HALF_UP):null);
        }
        GameRecordReportTotalVo gameRecordReportTotalVo=new GameRecordReportTotalVo();
        gameRecordReportTotalVo.setBetAmount(recordRecordSum.getBetAmount() != null? recordRecordSum.getBetAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setBettingNumber(recordRecordSum.getBettingNumber()!= null? recordRecordSum.getBettingNumber() : CommonConst.NUMBER_0);
        gameRecordReportTotalVo.setValidAmount(recordRecordSum.getValidAmount()!= null? recordRecordSum.getValidAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setWinLossAmount(recordRecordSum.getWinLossAmount()!= null? recordRecordSum.getWinLossAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setAmount(recordRecordSum.getAmount()!= null? recordRecordSum.getAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setSurplusAmount(recordRecordSum.getSurplusAmount()!=null?recordRecordSum.getSurplusAmount():BigDecimal.ZERO);
        gameRecordReportTotalVo.setUserAmount(recordRecordSum.getUserAmount()!=null?recordRecordSum.getUserAmount():BigDecimal.ZERO);
        gameRecordReportTotalVo.setTotalWinLossAmount(gameRecordReportTotalVo.getWinLossAmount().add(gameRecordReportTotalVo.getAmount()).add(gameRecordReportTotalVo.getUserAmount()).add(gameRecordReportTotalVo.getSurplusAmount()));
        return ResponseUtil.success(gameRecordReportTotalVo);

    }
}

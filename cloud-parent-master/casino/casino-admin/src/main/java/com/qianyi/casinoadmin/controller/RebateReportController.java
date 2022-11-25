package com.qianyi.casinoadmin.controller;

import cn.hutool.core.date.DateUtil;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.vo.*;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Api(tags = "报表中心")
@Slf4j
@RestController
@RequestMapping("rebateReport")
public class RebateReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    //    @NoAuthorization
    @ApiOperation("查询个人返利报表")
    @GetMapping("/queryPersonReport")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = true),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9、OBDJ ", required = false),
        @ApiImplicitParam(name = "sort", value = "1 正序 2 倒序", required = false),
        @ApiImplicitParam(name = "time", value = "1 北京时间 2 美东时间", required = false),
        @ApiImplicitParam(name = "tag", value = "1：投注笔数 2：投注金额 3：有效投注 4：返利总额 5：用户输赢金额", required = false),
    })
    public ResponseEntity<PersonReportVo> queryPersonReport(
        Integer pageSize,
        Integer pageCode,
        String userName,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
        String platform,
        Integer sort,
        Integer time,
        Integer tag){
        if (LoginUtil.checkNull(startTime, endTime, pageSize, pageCode)){
            return ResponseUtil.custom("参数不合法");
        }

        String orderTimeStart = "'"+DateUtil.formatDate(startTime)+"'";
        String orderTimeEnd = "'"+DateUtil.formatDate(endTime)+"'";

        if (LoginUtil.checkNull(time)){
            // 向后偏移12小时
            if (LoginUtil.checkNull(platform) || platform.equals("WM")){
                startTime = DateUtil.offsetHour(startTime, 12);
                endTime = DateUtil.offsetHour(endTime, 12);
            }
        }else {
            if (time == CommonConst.NUMBER_2){
                startTime = DateUtil.offsetHour(startTime, 12);
                endTime = DateUtil.offsetHour(endTime, 12);
            }
        }

        String startTimeStr = DateUtil.formatDateTime(startTime);
        String endTimeStr = DateUtil.formatDateTime(endTime);

        if(StringUtils.hasLength(userName)){
            User user = userService.findByAccount(userName);
            if(user != null){
                List<RebateReportVo> reportResult = userService.findRebateMap(platform,startTimeStr,endTimeStr,user.getId(),orderTimeStart,orderTimeEnd,"");
                PageResultVO<RebateReportVo> mapPageResultVO = combinePage(reportResult, 1, pageCode, pageSize);
                return ResponseUtil.success(getMap(mapPageResultVO));
            }
            List<RebateReportVo> emptyResult = new ArrayList<>();
            return ResponseUtil.success(combinePage(emptyResult, 0, pageCode, pageSize));
        }

        int page = (pageCode-1)*pageSize;
        List<RebateReportVo> reportResult = null;

        try {
            String statement = getOrderByStatement(tag, sort);
            reportResult = userService.findRebateMap(platform, startTimeStr, endTimeStr, page, pageSize, statement,orderTimeStart,orderTimeEnd,"");
        } catch (Exception e) {
            return ResponseUtil.custom("查询失败");
        }

        int totalElement = reportService.queryTotalElement(startTimeStr, endTimeStr);
        PageResultVO<RebateReportVo> mapPageResultVO = combinePage(reportResult, totalElement, pageCode, pageSize);
        return ResponseUtil.success(getMap(mapPageResultVO));
    }

    // 获取 order by 语句
    private String getOrderByStatement(Integer tag, Integer sort){
        if (LoginUtil.checkNull(tag)) {
            return "";
        }
        String str = "ORDER BY {0} ";
        switch (tag) {
            case 1:
                str = MessageFormat.format(str,"num");
                break;
            case 2:
                str = MessageFormat.format(str,"bet_amount");
                break;
            case 3:
                str = MessageFormat.format(str,"validbet");
                break;
            case 4:
                str = MessageFormat.format(str,"total_rebate");
                break;
            case 5:
                str = MessageFormat.format(str,"win_loss");
                break;
            default:
                throw new BusinessException("参数不合法");
        }
        if (LoginUtil.checkNull(sort) || sort == CommonConst.NUMBER_1){
            str = str + "ASC";
        }else {
            str = str + "DESC";
        }
        return str;
    }

    private PageResultVO<RebateReportVo> getMap(PageResultVO<RebateReportVo> mapPageResultVO){
        List<RebateReportVo> content = (List<RebateReportVo>) mapPageResultVO.getContent();

        if (!LoginUtil.checkNull(content) && content.size() > CommonConst.NUMBER_0){
            for (RebateReportVo item: content){
                if (item.getThirdProxy() == null){
                    continue;
                }
                ProxyUser third_proxy = proxyUserService.findById(Long.parseLong(item.getThirdProxy()));
                item.setThirdProxyName(third_proxy==null?"":third_proxy.getUserName());
            }
        }
        return mapPageResultVO;
    }

    private PageResultVO<RebateReportVo> combinePage(List<RebateReportVo> reportResult, int totalElement, int page, int num){
        PageResultVO<RebateReportVo> pageResult = new PageResultVO<>(page, num, Long.parseLong(totalElement+""), reportResult);
        return pageResult;
    }

    //    @NoAuthorization
    @ApiOperation("查询个人返利报表总计")
    @GetMapping("/queryTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "time", value = "1 北京时间 2 美东时间", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<RebateReportTotalVo> queryTotal(
        String userName,
        String platform,
        Integer time,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }

        String orderTimeStart = "'"+DateUtil.formatDate(startDate)+"'";
        String orderTimeEnd = "'"+DateUtil.formatDate(endDate)+"'";

        if (LoginUtil.checkNull(time)){
            // 向后偏移12小时
            if (LoginUtil.checkNull(platform) || platform.equals("WM")){
                startDate = DateUtil.offsetHour(startDate, 12);
                endDate = DateUtil.offsetHour(endDate, 12);
            }
        }else {
            if (time == CommonConst.NUMBER_2){
                startDate = DateUtil.offsetHour(startDate, 12);
                endDate = DateUtil.offsetHour(endDate, 12);
            }
        }

        String startTime = DateUtil.formatDateTime(startDate);
        String endTime = DateUtil.formatDateTime(endDate);

        Long userId=null;
        RebateReportTotalVo itemObject = null;
        if(StringUtils.hasLength(userName)){
            User user = userService.findByAccount(userName);
            if(user != null){
                userId=user.getId();
                List<RebateReportVo> maps = userService.findRebateMap(platform, startTime, endTime, userId,orderTimeStart,orderTimeEnd,"");
                itemObject = DTOUtil.toDTO(maps.get(0), RebateReportTotalVo.class);
            }
        }else {
            Map<String,Object> result = userService.findRebateMap(platform,startTime,endTime,orderTimeStart,orderTimeEnd,"");
            itemObject = DTOUtil.toDTO(result, RebateReportTotalVo.class);
        }
        return ResponseUtil.success(itemObject);
    }

    /*private HistoryTotal getHistoryItem(Map<String,Object> result){
        HistoryTotal historyTotal = new HistoryTotal();
        historyTotal.setAll_profit_amount(new BigDecimal(result.get("all_profit_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setService_charge(new BigDecimal(result.get("service_charge").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setTotal_amount(new BigDecimal(result.get("total_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setNum(Integer.parseInt(result.get("num").toString()));
        historyTotal.setBet_amount(new BigDecimal(result.get("bet_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWin_loss(new BigDecimal(result.get("win_loss").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setValidbet(new BigDecimal(result.get("validbet").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWash_amount(new BigDecimal(result.get("wash_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setAvg_benefit(new BigDecimal(result.get("avg_benefit").toString()).setScale(2, RoundingMode.HALF_UP));
        return historyTotal;
    }*/
}

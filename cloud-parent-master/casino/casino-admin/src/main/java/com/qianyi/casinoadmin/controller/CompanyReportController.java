package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.GameRecordReportNew;
import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.vo.GameRecordReportVo;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

@Api(tags = "公司报表")
@Slf4j
@RestController
@RequestMapping("companyReport")
public class CompanyReportController {

    @Autowired
    private GameRecordReportNewService gameRecordReportNewService;

    @ApiOperation("查询公司报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        //            @ApiImplicitParam(name = "gid", value = "游戏类别编号 百家乐:101 龙虎:102 轮盘:103 骰宝:104 牛牛:105 番摊:107 色碟:108 鱼虾蟹:110 炸金花:111 安达巴哈:128", required = false),
            @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<GameRecordReportVo> find(String platform, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
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
        GameRecordReportNew recordRecordSum = gameRecordReportNewService.findRecordRecordSum(gameRecordReport, startTime, endTime);
        if (!LoginUtil.checkNull(recordRecordSum)){
            recordRecordSum.setAmount(recordRecordSum.getAmount() != null? recordRecordSum.getAmount().setScale(2, RoundingMode.HALF_UP):null);
        }
        return ResponseUtil.success(recordRecordSum);
    }
}

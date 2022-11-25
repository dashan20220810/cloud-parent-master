package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserThirdService;
import com.qianyi.casinoweb.job.GameRecordAsyncOper;
import com.qianyi.casinoweb.job.GameRecordGoldenFJob;
import com.qianyi.casinoweb.job.GameRecordJob;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.GoldenFTimeVO;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Api(tags = "手动补单")
@RestController
@RequestMapping("/supplement")
@Slf4j
public class SupplementController {

    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private GameRecordJob gameRecordJob;
    @Autowired
    private GameRecordGoldenFJob gameRecordGoldenFJob;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;

    @GetMapping("/supplementByPlatform")
    @ApiOperation("后台根据平台手动补单")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间,格式为:yyyy-MM-dd HH:mm:ss", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间,格式为:yyyy-MM-dd HH:mm:ss", required = true),
            @ApiImplicitParam(name = "platform", value = "平台：WM,PG,CQ9,SABASPORT", required = true),
    })
    public ResponseEntity supplementByPlatform(String platform, String startTime, String endTime) {
        log.info("后台开始补单,platform={},startTime={},endTime={}",platform,startTime,endTime);
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        boolean checkNull = CasinoWebUtil.checkNull(platform, startTime, endTime);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setLenient(false);//表示严格验证
        Date startDateTime = null;
        Date endDateTime = null;
        try {
            startDateTime = df.parse(startTime);
            endDateTime = df.parse(endTime);
            long startTimeNum = startDateTime.getTime();
            long endTimeNum = endDateTime.getTime();
            if (endTimeNum < startTimeNum) {
                return ResponseUtil.custom("开始时间不能大于结束时间");
            }
            long diff = endTimeNum - startTimeNum;
            //后台只补单时间区间不能超过1天
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 1) {
                return ResponseUtil.custom("补单时间范围不能超过1天");
            }
        } catch (ParseException e) {
            return ResponseUtil.custom("startTime或endTime时间格式填写错误,,格式为:yyyy-MM-dd HH:mm:ss");
        }
        if (Constants.PLATFORM_WM_BIG.equals(platform)) {
            SimpleDateFormat wm = new SimpleDateFormat("yyyyMMddHHmmss");
            String wmStartTime = wm.format(startDateTime);
            String wmEndTime = wm.format(endDateTime);
            ResponseEntity response = wmSupplement(Constants.CASINO_WEB, wmStartTime, wmEndTime);
            return response;
        } else if (Constants.PLATFORM_PG.equals(platform) || Constants.PLATFORM_CQ9.equals(platform)|| Constants.PLATFORM_SABASPORT.equals(platform)) {
            Long goldenfStartTime = startDateTime.getTime();
            Long goldenfEndTime = endDateTime.getTime();
            ResponseEntity response = goldenFSupplement(Constants.CASINO_WEB, platform, goldenfStartTime, goldenfEndTime);
            return response;
        }else {
            return ResponseUtil.custom(platform+"平台不支持补单");
        }
    }

    @GetMapping("/wm")
    @ApiOperation("WM平台手动补单")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间,格式为:yyyyMMddHHmmss", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间,格式为:yyyyMMddHHmmss", required = true),
            @ApiImplicitParam(name = "secretkey", value = "秘钥", required = true),
    })
    public ResponseEntity wmSupplement(String secretkey, String startTime, String endTime) {
        boolean checkNull = CasinoWebUtil.checkNull(secretkey,startTime, endTime);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!Constants.CASINO_WEB.equals(secretkey)) {
            return ResponseUtil.custom("秘钥错误");
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        df.setLenient(false);//表示严格验证
        try {
            long startTimeNum = df.parse(startTime).getTime();
            long endTimeNum = df.parse(endTime).getTime();
            if (endTimeNum < startTimeNum) {
                return ResponseUtil.custom("开始时间不能大于结束时间");
            }
            long diff = new Date().getTime() - startTimeNum;
            //WM报表资料只保留60天。
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 60) {
                return ResponseUtil.custom("仅允许补单60天内的数据");
            }
        } catch (ParseException e) {
            return ResponseUtil.custom("startTime或endTime时间格式填写错误,,格式为:yyyyMMddHHmmss");
        }
        List<String> list = new ArrayList<>();
        //第三方要求拉取数据时间范围不能大于1天，大于1天，取开始时间的后一天为结束时间
        getDateTimeReport(list, startTime, endTime, df);
        log.info("WM补单完成,timeList={}", JSON.toJSONString(list));
        return ResponseUtil.success(list);
    }

    private List<String> getDateTimeReport(List<String> list, String startTime, String endTime, SimpleDateFormat df) {
        String timeMsg = null;
        String endTimeNew = null;
        try {
            Date startDate = df.parse(startTime);
            Date endDate = df.parse(endTime);
            long startTimeNum = startDate.getTime();
            long endTimeNum = endDate.getTime();
            //重叠时间区间 -2代表重叠2分钟
            int overlap = -2;
            if ((endTimeNum - startTimeNum) > 60 * 60 * 24 * 1000) {
                Calendar after = Calendar.getInstance();
                after.setTime(startDate);
                after.add(Calendar.DAY_OF_MONTH, 1);
                Date afterDate = after.getTime();
                endTimeNew = df.format(afterDate);
                //下面开始时间前移2分钟，结束时间也要前移2分钟
                startTime = getBeforeDateTime(df,startTime,overlap);
                endTimeNew = getBeforeDateTime(df,endTimeNew,overlap);
            } else {
                endTimeNew = endTime;
            }

            timeMsg = startTime + "到" + endTimeNew;
            log.info("开始拉取{}的注单记录", timeMsg);
            String result = wmApi.getDateTimeReport(null, startTime, endTimeNew, 0, 1, 2, null, null);
            //远程请求异常
            if (ObjectUtils.isEmpty(result)) {
                log.error("{}游戏记录拉取失败,远程请求异常", timeMsg);
                list.add(timeMsg + "游戏记录拉取失败,远程请求异常");
            } else if ("notData".equals(result)) { //查询结果无记录
                log.info("{}时间范围无记录", timeMsg);
                list.add(timeMsg + "时间范围无记录");
            } else {
                List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
                if (!CollectionUtils.isEmpty(gameRecords)) {
                    gameRecordJob.saveAll(gameRecords);
                    log.info("{}游戏记录补单完成", timeMsg);
                    list.add(timeMsg + "游戏记录补单完成");
                }
            }
        } catch (Exception e) {
            log.error("{}游戏记录拉取异常,msg={}", timeMsg,e.getMessage());
            list.add(timeMsg + "游戏记录拉取异常");
            e.printStackTrace();
        }
        if (!endTime.equals(endTimeNew)) {
            try {
                //报表查询需间隔30秒，未搜寻到数据需间隔10秒。
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getDateTimeReport(list, endTimeNew, endTime, df);
        }
        return list;
    }

    private String getBeforeDateTime(SimpleDateFormat format,String currentTime,int before) throws ParseException {
        Date date = format.parse(currentTime);
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.add(Calendar.MINUTE, before);
        Date afterFiveMin = now.getTime();
        String dateTime = format.format(afterFiveMin);
        return dateTime;
    }

    @GetMapping("/goldenF")
    @ApiOperation("goldenF平台手动补单")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "secretkey", value = "秘钥", required = true),
            @ApiImplicitParam(name = "startTime", value = "开始时间,13位时间戳", required = true),
            @ApiImplicitParam(name = "endTime", value = "结束时间,13位时间戳", required = true),
            @ApiImplicitParam(name = "vendorCode", value = "产品代码：PG/CQ9/SABASPORT", required = true),
    })
    public ResponseEntity goldenFSupplement(String secretkey, String vendorCode, Long startTime, Long endTime) {
        boolean checkNull = CasinoWebUtil.checkNull(secretkey,vendorCode,startTime, endTime);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!Constants.CASINO_WEB.equals(secretkey)) {
            return ResponseUtil.custom("秘钥错误");
        }
        if (!Constants.PLATFORM_PG.equals(vendorCode) && !Constants.PLATFORM_CQ9.equals(vendorCode)&& !Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
            return ResponseUtil.custom("产品代码错误");
        }
        if (startTime.toString().length() != 13 || endTime.toString().length() != 13) {
            return ResponseUtil.custom("时间格式错误");
        }
        List<GoldenFTimeVO> timeVOS = new ArrayList<>();
        log.info("开始时间:{},结束时间:{}", startTime, endTime);
        Long range = endTime - startTime;
        if (range <= 0) {
            return ResponseUtil.custom("结束时间大于开始时间");
        }
        long diff = new Date().getTime() - startTime;
        //WM报表资料只保留60天。
        long days = diff / (1000 * 60 * 60 * 24);
        if (days > 60) {
            return ResponseUtil.custom("仅允许补单60天内的数据");
        }
        log.info("{}", range);
        Long num = range / (4 * 60 * 1000);
        log.info("num is {}", num);
        for (int i = 0; i <= num; i++) {
            startTime = startTime - 60 * 1000;//每次拉取重叠一分钟
            GoldenFTimeVO goldenFTimeVO = new GoldenFTimeVO();
            Long tempEndTime = startTime + (5 * 60 * 1000);
            goldenFTimeVO.setStartTime(startTime);
            goldenFTimeVO.setEndTime(tempEndTime > endTime ? endTime : tempEndTime);
            timeVOS.add(goldenFTimeVO);
            startTime = tempEndTime;
        }
        log.info("{}", timeVOS);
        gameRecordGoldenFJob.supplementPullGameRecord(vendorCode,timeVOS);
        log.info("{}补单完成,timeList={}", vendorCode,JSON.toJSONString(timeVOS));
        return ResponseUtil.success(timeVOS);
    }
}

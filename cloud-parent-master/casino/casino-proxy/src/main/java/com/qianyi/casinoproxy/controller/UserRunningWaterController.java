package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Api(tags = "客户中心")
@Slf4j
@RestController
@RequestMapping("userRunningWater")
public class UserRunningWaterController {

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @ApiOperation("查询会员流水报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userId", value = "会员id", required = true),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<UserRunningWater> find(Integer pageSize, Integer pageCode, Long userId,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(userId)){
            return ResponseUtil.custom("参数必填");
        }
        UserRunningWater userRunningWater = new UserRunningWater();
        userRunningWater.setUserId(userId);
        UserRunningWater runningWater = null;
        if ((CasinoProxyUtil.checkNull(startDate,endDate)) || DateUtil.isEffectiveDate(new Date(),startDate,endDate)){
            try {
                runningWater = this.assemble(runningWater,userId);
            }catch (Exception ex){
                return ResponseUtil.custom("查询失败");
            }
        }
        Sort sort=Sort.by("id").descending();
        String startTime = startDate == null?null:DateUtil.dateToPatten1(startDate);
        String endTime = endDate == null?null:DateUtil.dateToPatten1(endDate);
        if (CasinoProxyUtil.checkNull(runningWater)){
            Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
            Page<UserRunningWater> userPage = userRunningWaterService.findUserPage(pageable, userRunningWater,startTime,endTime);
            return ResponseUtil.success(userPage);
        }else {
            List<UserRunningWater> list = new LinkedList<>();
            list.add(runningWater);
            List<UserRunningWater> userRunningWaters = userRunningWaterService.findUserRunningWaters(sort,userRunningWater, startTime, endTime);
            if (!CasinoProxyUtil.checkNull(userRunningWaters) && userRunningWaters.size() > CommonConst.NUMBER_0){
                list.addAll(userRunningWaters);
            }
            PageVo pageVO = new PageVo(pageCode,pageSize);
            PageResultVO<UserRunningWater> pageResultVO = (PageResultVO<UserRunningWater>) CommonUtil.handlePageResult(list, pageVO);
            userRunningWaters.clear();
            return ResponseUtil.success(pageResultVO);
        }
    }

    private UserRunningWater assemble(UserRunningWater runningWater,Long userId) throws ParseException {
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String startTime = format + start;
        String endTime = format + end;
        Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
        Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
        BigDecimal validbet = userGameRecordReportService.sumUserRunningWaterByUserId(format, format, userId);

        if (validbet.compareTo(BigDecimal.ZERO) != CommonConst.NUMBER_0){
            runningWater = new UserRunningWater();
            runningWater.setAmount(validbet);
            runningWater.setStaticsTimes(format);
            runningWater.setCommission(BigDecimal.ZERO);
        }
        List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(userId,null, startDate, endDate);
        if (!CasinoProxyUtil.checkNull(shareProfitChanges) && shareProfitChanges.size() > CommonConst.NUMBER_0){
            BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (CasinoProxyUtil.checkNull(runningWater)){
                runningWater = new UserRunningWater();
                runningWater.setCommission(contribution);
                runningWater.setStaticsTimes(format);
                runningWater.setAmount(BigDecimal.ZERO);
            }else {
                runningWater.setCommission(contribution);
            }
        }
        return runningWater;
    }
}

package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.RechargeTurnover;
import com.qianyi.casinocore.service.RechargeTurnoverService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


@RestController
@RequestMapping("/rechargeTurnover")
@Api(tags = "资金中心")
public class RechargeTurnoverController {

    @Autowired
    private RechargeTurnoverService rechargeTurnoverService;

    @ApiOperation("充值订单流水详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "date", value = "时间：全部：不传值，0：今天，1：昨天，2：一个月内", required = false)
    })
    @GetMapping("/findPage")
    public ResponseEntity<Page<RechargeTurnover>> findPage(Integer pageSize, Integer pageCode, String date) {
        Long userId = CasinoWebUtil.getAuthId();
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoWebUtil.setPageable(pageCode, pageSize, sort);
        String startTime = null;
        String endTime = null;
        if ("0".equals(date)) {
            startTime = DateUtil.getStartTime(0);
            endTime = DateUtil.getEndTime(0);
        } else if ("1".equals(date)) {
            startTime = DateUtil.getStartTime(-1);
            endTime = DateUtil.getEndTime(-1);
        } else if ("2".equals(date)) {
            startTime = DateUtil.getMonthAgoStartTime(-1);
            endTime = DateUtil.getEndTime(0);
        }
        Page<RechargeTurnover> rechargeTurnoverPage = rechargeTurnoverService.findUserPage(pageable, userId, startTime, endTime);
        return ResponseUtil.success(rechargeTurnoverPage);
    }

    private static Date getStartTime(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,num);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date getEndTime(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,num);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }


    private static Date getMonthAgoStartTime(int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, num);//得到前一个月
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(getMonthAgoStartTime(-1)));
        System.out.println(sdf.format(getEndTime(-1)));
    }

}

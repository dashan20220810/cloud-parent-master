package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.WithdrawOrderService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 提现记录表
 */
@Slf4j
@RestController
@RequestMapping("/withdraw")
@Api(tags = "资金中心")
public class WithdrawOrderController {

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @ApiOperation("提现列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "订单状态：全部：不传值，0: 正在出款 1：成功出款，2：退回出款，4.总控下分 5.代理下分", required = false),
            @ApiImplicitParam(name = "date", value = "时间：全部：不传值，0：今天，1：昨天，2：一个月内", required = false)
    })
    @GetMapping("/withdrawList")
    public ResponseEntity<Page<WithdrawOrder>> withdrawList(Integer pageSize, Integer pageCode, Integer status, String date) {
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
        //合并状态
        List<Integer> statusList = new ArrayList();
        if (status != null) {
            if (status == 0) {
                statusList.add(0);
                statusList.add(6);
                statusList.add(7);
            } else if (status == 1) {
                statusList.add(1);
            } else if (status == 2) {
                statusList.add(2);
                statusList.add(8);
            }
        }
        Page<WithdrawOrder> withdrawOrderPage = withdrawOrderService.findUserPage(pageable, userId,statusList,startTime,endTime);
        List<WithdrawOrder> content = withdrawOrderPage.getContent();
        if (!CollectionUtils.isEmpty(content)) {
            for (WithdrawOrder withdrawOrder : content) {
                Integer orderStatus = withdrawOrder.getStatus();
                //总控和代理的操作为人工操作
                if (orderStatus != null ){
                    if (orderStatus == 4 || orderStatus == 5) {
                        withdrawOrder.setRemitType(4);
                    }
                    //合并状态
                    if (orderStatus == 6 || orderStatus == 7) {
                        withdrawOrder.setStatus(0);
                    } else if (orderStatus == 8) {
                        withdrawOrder.setStatus(2);
                    }
                }
            }
        }
        return ResponseUtil.success(withdrawOrderPage);
    }
}

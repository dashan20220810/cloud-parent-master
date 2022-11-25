package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.business.DepositSendActivityBusiness;
import com.qianyi.casinocore.model.DepositSendActivity;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.DepositSendActivityService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activity")
@Api(tags = "存送活动")
public class ActivityController {

    @Autowired
    private DepositSendActivityService depositSendActivityService;

    @Autowired
    private DepositSendActivityBusiness depositSendActivityBusiness;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/activitylist")
    @ApiOperation("活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "actName", value = "活动名称", required = false),
    })
    public ResponseEntity<DepositSendActivity> bankList(String actName) {
        List<DepositSendActivity> depositSendActivityList = depositSendActivityService.findAllAct(actName);
        if(depositSendActivityList != null && depositSendActivityList.size() > 0){
            List<String> createBy = depositSendActivityList.stream().map(DepositSendActivity::getCreateBy).collect(Collectors.toList());
            List<SysUser> sysUsers = sysUserService.findAll(createBy);
            depositSendActivityList.stream().forEach(depositSendActivity -> {
                sysUsers.stream().forEach(sysUser -> {
                    if (sysUser.getId().toString().equals(depositSendActivity.getCreateBy() == null ? "" : depositSendActivity.getCreateBy())) {
                        depositSendActivity.setCreateBy(sysUser.getUserName());
                    }
                });
            });
        }
        return ResponseUtil.success(depositSendActivityList);
    }

    @ApiOperation("新增活动")
    @PostMapping(value = "/saveActivity",name = "新增活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "actName", value = "活动名称", required = true),
            @ApiImplicitParam(name = "actType", value = "活动类型", required = true),
            @ApiImplicitParam(name = "depositAmount", value = "存金额", required = true),
            @ApiImplicitParam(name = "sendAmount", value = "送金额", required = true),
            @ApiImplicitParam(name = "amountTimes", value = "流水倍数", required = true)})
    public ResponseEntity saveActivity(String actName,String actType,String depositAmount,String sendAmount, String amountTimes){
        if (LoginUtil.checkNull(actName)){
            ResponseUtil.custom("参数不合法");
        }

        Integer actTypeInt = Integer.parseInt(actType);
        BigDecimal depAmountDec = new BigDecimal(depositAmount);
        BigDecimal sendAmountDec = new BigDecimal(sendAmount);
        Integer amountTimesInt = Integer.parseInt(amountTimes);

        return ResponseUtil.success(depositSendActivityBusiness.save(actName,actTypeInt,depAmountDec,sendAmountDec,amountTimesInt));
    }

    @ApiOperation("编辑活动")
    @PostMapping(value = "/updateActivity",name = "更新活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动Id", required = true),
            @ApiImplicitParam(name = "actName", value = "活动名称", required = true),
            @ApiImplicitParam(name = "depositAmount", value = "存金额", required = true),
            @ApiImplicitParam(name = "sendAmount", value = "送金额", required = true),
            @ApiImplicitParam(name = "amountTimes", value = "流水倍数")})
    public ResponseEntity updateActivity(String id,String actName, String actType,String depositAmount,String sendAmount, String amountTimes){
        if (LoginUtil.checkNull(actName)){
            ResponseUtil.custom("参数不合法");
        }

        Long actId = Long.parseLong(id);
        Integer actTypeInt = Integer.parseInt(actType);
        BigDecimal depAmountDec = new BigDecimal(depositAmount);
        BigDecimal sendAmountDec = new BigDecimal(sendAmount);
        Integer amountTimesInt = Integer.parseInt(amountTimes);

        return ResponseUtil.success(depositSendActivityBusiness.updateActivity(actId,actName,actTypeInt,depAmountDec,sendAmountDec,amountTimesInt));
    }


    @ApiOperation("启动活动")
    @PostMapping(value = "/startActivity",name = "开启活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动Id", required = true)})
    public ResponseEntity startActivity(String id){
        if (LoginUtil.checkNull(id)){
            ResponseUtil.custom("参数不合法");
        }

        Long actId = Long.parseLong(id);
        depositSendActivityService.startActivity(actId);
        return ResponseUtil.success();
    }

    @ApiOperation("停止活动")
    @PostMapping(value = "/stopActivity",name = "停止活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动Id", required = true)})
    public ResponseEntity stopActivity(String id){
        if (LoginUtil.checkNull(id)){
            ResponseUtil.custom("参数不合法");
        }

        Long actId = Long.parseLong(id);
        depositSendActivityService.stopActivity(actId);
        return ResponseUtil.success();
    }

    @ApiOperation("删除活动")
    @PostMapping(value = "/delActivity",name = "删除活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "活动Id", required = true)})
    public ResponseEntity delActivity(String id){
        if (LoginUtil.checkNull(id)){
            ResponseUtil.custom("参数不合法");
        }

        Long actId = Long.parseLong(id);
        depositSendActivityService.deleteAct(actId);
        return ResponseUtil.success();
    }
}

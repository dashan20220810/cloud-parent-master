package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.AmountConfigVo;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@RestController
@RequestMapping("/chargeConfig")
@Api(tags = "运营中心")
public class ChargeConfigController {
    @Autowired
    private PlatformConfigService platformConfigService;
    /**
     * 充值设置列表
     * @return
     */
    @ApiOperation("充值设置列表")
    @GetMapping("/findChargeConfig")
    public ResponseEntity<AmountConfigVo> findChargeConfig(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        AmountConfigVo amountConfigVo = new AmountConfigVo();
        if (LoginUtil.checkNull(platformConfig)){
            return ResponseUtil.success(amountConfigVo);
        }
        amountConfigVo.setFixedAmount(platformConfig.getChargeServiceMoney());
        amountConfigVo.setPercentage(platformConfig.getChargeRate().multiply(CommonConst.BIGDECIMAL_100));
        amountConfigVo.setMaxMoney(platformConfig.getChargeMaxMoney());
        amountConfigVo.setMinMoney(platformConfig.getChargeMinMoney());
        return ResponseUtil.success(amountConfigVo);
    }

    /**
     * 充值设置修改
     * @param fixedAmount 固定金额
     * @param percentage 百分比金额
     * @param maxMoney 最大金额
     * @param minMoney 最小金额
     * @return
     */
    @ApiOperation("充值设置修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fixedAmount", value = "固定金额", required = false),
            @ApiImplicitParam(name = "percentage", value = "百分比金额只能传0到100之间", required = false),
            @ApiImplicitParam(name = "maxMoney", value = "最大金额", required = true),
            @ApiImplicitParam(name = "minMoney", value = "最小金额", required = true),
    })
    @PostMapping("/saveChargeConfig")
    public ResponseEntity saveChargeConfig(BigDecimal fixedAmount, Float percentage, BigDecimal maxMoney, BigDecimal minMoney){
        if (fixedAmount != null && percentage != null){
            return ResponseUtil.custom("参数错误");
        }
        if (LoginUtil.checkNull(maxMoney,minMoney)){
            return ResponseUtil.custom("参数不合法");
        }
        if (maxMoney.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
            return ResponseUtil.custom("金额不能大于99999999");
        }
        if (minMoney.compareTo(maxMoney) >= CommonConst.NUMBER_0){
            return ResponseUtil.custom("最小金额不能大于或者等于最大金额");
        }
        if (percentage != null && (percentage > CommonConst.FLOAT_100 || percentage <= CommonConst.FLOAT_0)){
            return ResponseUtil.custom("百分比金额0%-100%区间");
        }
        if (fixedAmount != null && minMoney != null){
            if (fixedAmount.compareTo(minMoney) > CommonConst.NUMBER_0){
                return ResponseUtil.custom("手续费不能大于最小金额");
            }
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        platformConfig.setChargeMaxMoney(maxMoney);
        platformConfig.setChargeMinMoney(minMoney);
        if (LoginUtil.checkNull(percentage)){
            platformConfig.setChargeRate(BigDecimal.ZERO);
        }else {
            percentage = percentage/CommonConst.FLOAT_100;
            platformConfig.setChargeRate(BigDecimal.valueOf(percentage));
        }
        if (LoginUtil.checkNull(fixedAmount)){
            platformConfig.setChargeServiceMoney(BigDecimal.ZERO);
        }else {
            platformConfig.setChargeServiceMoney(fixedAmount);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }
}

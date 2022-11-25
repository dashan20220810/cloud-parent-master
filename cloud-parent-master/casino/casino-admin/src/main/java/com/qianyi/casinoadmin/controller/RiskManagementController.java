package com.qianyi.casinoadmin.controller;


import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
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

@Slf4j
@RestController
@RequestMapping("/managementRisk")
@Api(tags = "运营中心")
public class RiskManagementController {
    @Autowired
    private PlatformConfigService platformConfigService;
    /**
     * 客户风险配置查询
     * @return
     */
    @ApiOperation("客户风险配置查询")
    @GetMapping("/findRiskConfig")
    public ResponseEntity<PlatformConfig> findSysConfig(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        return ResponseUtil.success(platformConfig);
    }

    /**
     * 客户风险配置修改
     * @param ipMaxNum 注册ip限制
     * @param wmMoneyWarning WM余额警戒线
     * @return
     */
    @ApiOperation("客户风险配置修改")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ipMaxNum", value = "注册ip限制", required = false),
            @ApiImplicitParam(name = "wmMoneyWarning", value = "WM余额警戒线", required = false),
            @ApiImplicitParam(name = "electronicsMoneyWarning", value = "PG/CQ9余额警戒线", required = false),
    })
    @PostMapping("/saveRiskConfig")
    public ResponseEntity saveSysConfig(Integer ipMaxNum, BigDecimal wmMoneyWarning,BigDecimal electronicsMoneyWarning){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (LoginUtil.checkNull(platformConfig)){
            platformConfig = new PlatformConfig();
        }
        if (!LoginUtil.checkNull(ipMaxNum)){
            platformConfig.setIpMaxNum(ipMaxNum);
        }
        if (!LoginUtil.checkNull(wmMoneyWarning)){
            if (wmMoneyWarning.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("金额不能大于99999999");
            }
            platformConfig.setWmMoneyWarning(wmMoneyWarning);
        }

        if (!LoginUtil.checkNull(electronicsMoneyWarning)){
            if (electronicsMoneyWarning.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("金额不能大于99999999");
            }
            platformConfig.setElectronicsMoneyWarning(electronicsMoneyWarning);
        }
        platformConfigService.save(platformConfig);
        return ResponseUtil.success();
    }
}

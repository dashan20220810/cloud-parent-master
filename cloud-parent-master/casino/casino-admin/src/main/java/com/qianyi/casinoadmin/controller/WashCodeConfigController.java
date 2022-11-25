package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 洗码基础配置表
 */
@RestController
@RequestMapping("washCodeConfig")
@Api(tags = "资金中心")
public class WashCodeConfigController {

    @Autowired
    private WashCodeConfigService washCodeConfigService;

    @GetMapping("findAll")
    @ApiOperation("游戏洗码配置查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "platform", value = "游戏平台", required = false),
    })
    public ResponseEntity<WashCodeConfig> findAll(String platform) {
        List<WashCodeConfig> washCodeConfigList = new ArrayList<>();
        if(!LoginUtil.checkNull(platform)){
            washCodeConfigList = washCodeConfigService.findByPlatform(platform);
        }else{
            washCodeConfigList = washCodeConfigService.findAll();
        }
        List<WashCodeConfig> washCodeConfigs = washCodeConfigList.stream().filter(washCodeConfig -> !LoginUtil.checkNull(washCodeConfig.getPlatform())).collect(Collectors.toList());

        return ResponseUtil.success(washCodeConfigs);
    }

    @PostMapping("updateWashCodeConfigs")
    @Operation(summary = "编辑游戏洗码配置")
    public ResponseEntity<WashCodeConfig> updateWashCodeConfigs(@RequestBody List<WashCodeConfig> washCodeConfigList){
        if(washCodeConfigList != null && washCodeConfigList.size() > 0){
            for (WashCodeConfig washCodeConfig : washCodeConfigList) {
                if(washCodeConfig.getRate().compareTo(BigDecimal.valueOf(0.9)) > 0){
                    return ResponseUtil.custom("洗码倍率超过限制");
                }
            }
            List<WashCodeConfig> washCodeConfigs = washCodeConfigService.saveAll(washCodeConfigList);
            Map<String, List<WashCodeConfig>> collect = washCodeConfigList.stream().collect(Collectors.groupingBy(WashCodeConfig::getPlatform));

            return ResponseUtil.success(collect);
        }
        return ResponseUtil.custom("保存游戏配置失败");
    }
}
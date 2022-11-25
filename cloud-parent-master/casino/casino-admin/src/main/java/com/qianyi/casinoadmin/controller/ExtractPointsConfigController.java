package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.business.ExtractPointsConfigBusiness;
import com.qianyi.casinocore.business.ExtractPointsTestBusiness;
import com.qianyi.casinocore.co.extractpoints.ExtractPointsConfigCo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.ExtractPointsConfigService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/extractPointsConfig")
@Api(tags = "代理抽点配置")
public class ExtractPointsConfigController {

    @Autowired
    private ExtractPointsConfigService service;

    @Autowired
    private ExtractPointsConfigBusiness business;

    @GetMapping("/findAll")
    @ApiOperation("代理抽点默认配置查询")
    public ResponseEntity<List<ExtractPointsConfig>> findAll(@ModelAttribute ExtractPointsConfigCo co) {
        List<ExtractPointsConfig> list = service.findList(co);
        return ResponseUtil.success(list);
    }


    @GetMapping("/poxy/findAll")
    @ApiOperation("代理抽点配置表查询")
    public ResponseEntity<List<PoxyExtractPointsConfig>> poxyFindAll(
            @ApiParam("基础代代理id") @RequestParam Long poxyId
    ) {
        return ResponseUtil.success(business.findPoxyExtractPointsConfig(poxyId));
    }


    @GetMapping("/user/findAll")
    @ApiOperation("用户抽点配置表查询")
    public ResponseEntity<List<UserExtractPointsConfig>> userFindAll(
            @ApiParam("用户id") @RequestParam Long userId
    ) {
        return ResponseUtil.success(business.findUserExtractPointsConfig(userId));
    }

    @PostMapping("/update")
    @ApiOperation("更新默认的抽点配置")
    public ResponseEntity<Map<String, List<ExtractPointsConfig>>> updateExtractPointsConfigs(
            @RequestBody List<ExtractPointsConfig> configList
    ) {
        return ResponseUtil.success(business.updateExtractPointsConfigs(configList));
    }


    @PostMapping("/poxy/update")
    @ApiOperation("更新基础代代理抽点配置")
    public ResponseEntity<List<PoxyExtractPointsConfig>> updatePoxyExtractPointsConfigs(
            @RequestBody List<PoxyExtractPointsConfig> configList
    ) {
        return ResponseUtil.success(business.updatePoxyExtractPointsConfigs(configList));
    }


    @PostMapping("/user/update")
    @ApiOperation("更新用户抽点配置")
    public ResponseEntity<List<UserExtractPointsConfig>> updateUserExtractPointsConfigs(
            @RequestBody List<UserExtractPointsConfig> configList
    ) {
        return ResponseUtil.success(business.updateUserExtractPointsConfigs(configList));
    }

    @Autowired
    private ExtractPointsTestBusiness testBusiness;

    @NoAuthorization
    //@GetMapping("/wm/test")
    public ResponseEntity<String> testWM(){
        testBusiness.testWM();
        return ResponseUtil.success();
    }

    @NoAuthorization
    //@GetMapping("/pg/test")
    public ResponseEntity<String> testPG(){
        testBusiness.testPG();
        return ResponseUtil.success();
    }

}

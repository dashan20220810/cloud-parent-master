package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.business.ExtractPointsConfigBusiness;
import com.qianyi.casinocore.model.PoxyExtractPointsConfig;
import com.qianyi.casinocore.model.UserExtractPointsConfig;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/extractPointsConfig")
@Api(tags = "代理抽点配置")
public class ExtractPointsConfigController {

    @Autowired
    private ExtractPointsConfigBusiness business;

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

}

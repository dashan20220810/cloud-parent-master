package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("expand")
@Api(tags = "推广中心")
@Slf4j
public class ExpandController {
    @Autowired
    private PlatformConfigService platformConfigService;
    @ApiOperation("获取推广链接")
    @GetMapping("getChainedAddress")
    public ResponseEntity getChainedAddress(){

        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || ObjectUtils.isEmpty(platformConfig.getProxyConfiguration())) {
            return ResponseUtil.custom("推广链接未配置");
        }
        String domain = platformConfig.getProxyConfiguration();
        String url = domain + "/" + Constants.INVITE_TYPE_COMPANY + "/" + platformConfig.getCompanyInviteCode();
        return ResponseUtil.success(url);
    }
}

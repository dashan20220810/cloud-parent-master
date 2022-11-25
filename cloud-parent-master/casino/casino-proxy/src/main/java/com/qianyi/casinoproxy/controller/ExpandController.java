package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.QRCodeUtil;
import com.qianyi.casinocore.vo.ChainedAddressVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
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
    @Autowired
    private ProxyUserService proxyUserService;
    @ApiOperation("获取推广链接")
    @GetMapping("getChainedAddress")
    public ResponseEntity<ChainedAddressVo> getChainedAddress(){
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (CasinoProxyUtil.checkNull(byId) || byId.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("只有基层代理才可以获取链接");
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || ObjectUtils.isEmpty(platformConfig.getProxyConfiguration())) {
            return ResponseUtil.custom("推广链接未配置");
        }
        String domain = platformConfig.getProxyConfiguration();
        String url = domain + "/" + Constants.INVITE_TYPE_PROXY + "/" + byId.getProxyCode();
        ChainedAddressVo chainedAddressVo = new ChainedAddressVo();
        chainedAddressVo.setUrl(url);
        try {
            chainedAddressVo.setQrCode(QRCodeUtil.getQRCodeImageByBase64(url,null,null));
        }catch (Exception ex) {
            log.error("二维码转化异常{}",ex);
            return ResponseUtil.custom("查询失败");
        }
        return ResponseUtil.success(chainedAddressVo);
    }
}

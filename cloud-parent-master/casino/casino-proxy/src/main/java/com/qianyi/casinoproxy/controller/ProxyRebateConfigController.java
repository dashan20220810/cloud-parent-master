package com.qianyi.casinoproxy.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proxyRebateConfig")
@Api(tags = "代理中心")
public class ProxyRebateConfigController {
    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private ProxyUserService proxyUserService;

    @ApiOperation("查询代理返佣等级配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前详情页面代理id", required = true),
            @ApiImplicitParam(name = "gameType", value = "游戏类型：1:WM,2:PG,3:CQ9,4:OBDJ, 5.OBTY,SABA", required = true),
    })
    @GetMapping("/findAll")
    public ResponseEntity findAll(Long id,Integer gameType){
        ProxyUser proxyUser = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(proxyUser)){
            return ResponseUtil.custom("代理不存在");
        }
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findByProxyUserIdAndGameType(proxyUser.getFirstProxy(),gameType);
        if (!CasinoProxyUtil.checkNull(proxyRebateConfig)){
            return ResponseUtil.success(proxyRebateConfig);
        }
        RebateConfig rebateConfig = rebateConfigService.findGameType(gameType);
        return ResponseUtil.success(rebateConfig);
    }
}

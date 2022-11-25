package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/wmBalance")
@Api(tags = "资金中心")
public class WMBalanceController {
    @Autowired
    private PlatformConfigService platformConfigService;
    /**
     * web定时查询WM总余额
     *
     * @return
     */
    @NoAuthorization
    @ApiOperation("web定时查询WM总余额")
    @GetMapping("/findWMBalance")
    public ResponseEntity findWMBalance(){
        PlatformConfig first = platformConfigService.findFirst();
        JSONObject jsonObject = new JSONObject();
        if (LoginUtil.checkNull(first)){
            jsonObject.put("balance", BigDecimal.ZERO);
            jsonObject.put("risk", BigDecimal.ZERO);
            return ResponseUtil.success(jsonObject);
        }
        jsonObject.put("balance", first.getWmMoney() == null? BigDecimal.ZERO:first.getWmMoney());
        jsonObject.put("risk", first.getWmMoneyWarning() == null? BigDecimal.ZERO:first.getWmMoneyWarning());
        return ResponseUtil.success(jsonObject);
    }
}

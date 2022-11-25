package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.RebateConfiguration;
import com.qianyi.casinocore.service.RebateConfigurationService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.ProxyWashCodeConfigVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/rebateConfiguration")
@Api(tags = "运营中心")
public class RebateConfigurationController {
    @Autowired
    private RebateConfigurationService rebateConfigurationService;

    @ApiOperation("全局获取返利比例")
    @GetMapping("/findAll")
    public ResponseEntity<ProxyWashCodeConfigVo> findAll(){
        RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(0L, Constants.OVERALL_TYPE);
        ProxyWashCodeConfigVo proxyWashCodeConfigVo = new ProxyWashCodeConfigVo();
        if (!LoginUtil.checkNull(byThirdProxy)){
            BeanUtils.copyProperties(byThirdProxy, proxyWashCodeConfigVo);
        }
        return ResponseUtil.success(proxyWashCodeConfigVo);
    }

    @ApiOperation("保存")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "proxyWashCodeConfig", value = "洗码抽点比例对象", required = false)
    })
    @PostMapping("/update")
    public ResponseEntity updateProxyWashCodeConfig(RebateConfiguration proxyWashCodeConfig){
        if (LoginUtil.checkNull(proxyWashCodeConfig)){
            return ResponseUtil.custom("参数必填");
        }
        if (LoginUtil.checkNull(proxyWashCodeConfig.getCQ9Rate(),proxyWashCodeConfig.getPGRate(),proxyWashCodeConfig.getWMRate(),proxyWashCodeConfig.getOBDJRate(),proxyWashCodeConfig.getOBTYRate())){
            return ResponseUtil.custom("参数必填");
        }
        if (proxyWashCodeConfig.getCQ9Rate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getCQ9Rate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getWMRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getWMRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getPGRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getPGRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getOBDJRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getOBDJRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getOBTYRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getOBTYRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        if (proxyWashCodeConfig.getSABASPORTRate().compareTo(new BigDecimal(CommonConst.NUMBER_100)) > 0 || proxyWashCodeConfig.getSABASPORTRate().compareTo(BigDecimal.ZERO) < 0){
            return ResponseUtil.custom("参数不合法");
        }
        RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(0L, Constants.OVERALL_TYPE);
        if (!LoginUtil.checkNull(byThirdProxy)){
            proxyWashCodeConfig.setId(byThirdProxy.getId());
        }
        proxyWashCodeConfig.setUserId(0L);
        proxyWashCodeConfig.setType(Constants.OVERALL_TYPE);
        rebateConfigurationService.save(proxyWashCodeConfig);
        return ResponseUtil.success();
    }
}

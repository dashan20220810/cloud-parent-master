package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.RebateConfiguration;
import com.qianyi.casinocore.service.RebateConfigurationService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.ProxyWashCodeConfigVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
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
@RequestMapping("/rebateUserConfiguration")
@Api(tags = "客户中心")
public class RebateUserConfigurationController {
    @Autowired
    private RebateConfigurationService rebateConfigurationService;
    @Autowired
    private UserService userService;

    @ApiOperation("会员返利比例")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "当前详情页面会员id", required = true),
    })
    @GetMapping("/findAll")
    public ResponseEntity<ProxyWashCodeConfigVo> findAll(Long id){
        RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(id, Constants.USER_TYPE);
        ProxyWashCodeConfigVo proxyWashCodeConfigVo = new ProxyWashCodeConfigVo();
        if (!LoginUtil.checkNull(byThirdProxy)){
            BeanUtils.copyProperties(byThirdProxy, proxyWashCodeConfigVo);
        }
        return ResponseUtil.success(proxyWashCodeConfigVo);
    }

    @ApiOperation("保存")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "proxyWashCodeConfig", value = "洗码抽点比例对象", required = false),
        @ApiImplicitParam(name = "userId", value = "当前详情会员id", required = true),
    })
    @PostMapping("/update")
    public ResponseEntity updateProxyWashCodeConfig(RebateConfiguration proxyWashCodeConfig,Long userId){
        if (LoginUtil.checkNull(userId)){
            return ResponseUtil.custom("参数必填");
        }

        RebateConfiguration byThirdProxy = rebateConfigurationService.findByUserIdAndType(userId, Constants.USER_TYPE);
        if (LoginUtil.checkNull(proxyWashCodeConfig)){
            return ResponseUtil.custom("参数必填");
        }
        if (LoginUtil.checkNull(proxyWashCodeConfig.getCQ9Rate(),proxyWashCodeConfig.getPGRate(),proxyWashCodeConfig.getWMRate())){
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
        Boolean tag = false;
        if (proxyWashCodeConfig.getPGRate().compareTo(BigDecimal.ZERO) == 0 &&
            proxyWashCodeConfig.getCQ9Rate().compareTo(BigDecimal.ZERO) == 0 &&
            proxyWashCodeConfig.getWMRate().compareTo(BigDecimal.ZERO) == 0 &&
            proxyWashCodeConfig.getOBDJRate().compareTo(BigDecimal.ZERO) == 0 &&
            proxyWashCodeConfig.getOBTYRate().compareTo(BigDecimal.ZERO) == 0 &&
            proxyWashCodeConfig.getSABASPORTRate().compareTo(BigDecimal.ZERO) == 0){
            tag = true;
        }
        if (LoginUtil.checkNull(byThirdProxy)){
            if (tag){
                return ResponseUtil.success();
            }
            byThirdProxy = new RebateConfiguration();
            byThirdProxy.setUserId(userId);
            byThirdProxy.setType(Constants.USER_TYPE);
        }else {
            if (tag){
                rebateConfigurationService.delete(userId,byThirdProxy);
                return ResponseUtil.success();
            }
        }

        byThirdProxy.setCQ9Rate(proxyWashCodeConfig.getCQ9Rate());
        byThirdProxy.setPGRate(proxyWashCodeConfig.getPGRate());
        byThirdProxy.setWMRate(proxyWashCodeConfig.getWMRate());
        byThirdProxy.setOBDJRate(proxyWashCodeConfig.getOBDJRate());
        byThirdProxy.setOBTYRate(proxyWashCodeConfig.getOBTYRate());
        byThirdProxy.setSABASPORTRate(proxyWashCodeConfig.getSABASPORTRate());
        rebateConfigurationService.save(byThirdProxy);
        return ResponseUtil.success();
    }

}

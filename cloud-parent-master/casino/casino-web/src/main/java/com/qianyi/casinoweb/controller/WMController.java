package com.qianyi.casinoweb.controller;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.livewm.constants.LanguageEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@RestController
@RequestMapping("wm")
@Api(tags = "WM游戏厅")
@Slf4j
public class WMController {
    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    UserThirdService userThirdService;
    @Autowired
    OrderService orderService;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    PublicWMApi wmApi;
    @Autowired
    ThirdGameBusiness thirdGameBusiness;
    @Autowired
    ErrorOrderService errorOrderService;
    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

    @Value("${project.signature}")
    String signature;

    @ApiOperation("开游戏")
    @RequestLimit(limit = 1, timeout = 5)
    @PostMapping("openGame")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gameType", value = "空：大厅。1.百家乐。2.龙虎 3. 轮盘 4. 骰宝 " +
                    "5. 牛牛  6. 三公  7. 番摊  8. 色碟 9. 鱼虾蟹 10. 炸金花 11. 牌九 12. 二八杠 13.安達巴哈", required = false),
    })
    public ResponseEntity openGame(Integer gameType, HttpServletRequest request) {
        //判断平台和游戏状态
        if (gameType == null || gameType == -1) {
            ResponseEntity response = thirdGameBusiness.checkPlatformStatus(Constants.PLATFORM_WM_BIG);
            if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
                return response;
            }
        } else {
            String gameCode = gameType.toString();
            ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(Constants.PLATFORM_WM_BIG, gameCode);
            if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
                return response;
            }
        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = null;
        synchronized (authId) {
            third = userThirdService.findByUserId(authId);
            //未注册自动注册到第三方
            if (third == null || ObjectUtils.isEmpty(third.getAccount())) {
                String account = UUID.randomUUID().toString();
                account = account.replaceAll("-", "");
                if (account.length() > 30) {
                    account = account.substring(0, 30);
                }
                String password = authId + "qianyi";

                boolean register = wmApi.register(account, password, account, null, null, "", null);
                if (!register) {
                    log.error("WM注册账号失败");
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }

                if (third == null) {
                    third = new UserThird();
                    third.setUserId(authId);
                }
                third.setAccount(account);
                third.setPassword(password);
                try {
                    userThirdService.save(third);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("本地注册账号失败{}", e.getMessage());
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
            }
        }
        User user = userService.findById(authId);
//        Integer lang = user.getLanguage();
//        if (lang == null) {
//            lang = 0;
//        }
        String language = request.getHeader(Constants.LANGUAGE);
        Integer lang = LanguageEnum.getLanguageCode(language);
        //回收其他游戏的余额
        thirdGameBusiness.oneKeyRecoverOtherGame(authId,Constants.PLATFORM_WM_BIG);
        PlatformConfig platformConfig = platformConfigService.findFirst();
        //TODO 扣款时考虑当前用户余额大于平台在三方的余额最大只能转入平台余额
        UserMoney userMoney = userMoneyService.findByUserId(authId);
        BigDecimal userCenterMoney = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getMoney() != null) {
            userCenterMoney = userMoney.getMoney();
        }

        if (platformConfig != null && platformConfig.getWmMoney() != null) {
            BigDecimal wmMoney = platformConfig.getWmMoney();
            if (wmMoney != null && wmMoney.compareTo(userCenterMoney) == -1) {
                userCenterMoney = wmMoney;
                log.error("userId:{},account={},进游戏加扣点WM余额不足，用户余额={}，wm余额={}",third.getUserId(),user.getAccount(),userCenterMoney,wmMoney);
            }
        }

        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
            //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
            //先扣本地款
            userMoneyService.subMoney(authId, userCenterMoney);
            String orderNo = orderService.getOrderNo();
            PublicWMApi.ResponseEntity entity = wmApi.changeBalance(third.getAccount(), userCenterMoney, orderNo, lang);
            if (entity == null) {
                log.error("userId:{},account:{},money:{},进游戏加扣点失败",third.getUserId(),user.getAccount(),userCenterMoney);
                //异步记录错误订单并重试补偿
                errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.WM_IN, Constants.PLATFORM_WM_BIG);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (entity.getErrorCode() == 911) {
                return ResponseUtil.custom("当前游戏维护中,请选择其他游戏");
            }
            if (entity.getErrorCode() != 0) {
                log.error("进游戏加扣点失败,userId:{},account:{},money:{},errorCode={},errorMsg={}",third.getUserId(),user.getAccount(),userCenterMoney, entity.getErrorCode(), entity.getErrorMessage());
                //三方加扣点失败再把钱加回来
                userMoneyService.addMoney(authId, userCenterMoney);
                return ResponseUtil.custom("加点失败,请联系客服");
            }

            Order order = new Order();
            order.setMoney(userCenterMoney);
            order.setUserId(authId);
            order.setRemark("自动转入WM");
            order.setType(0);
            order.setState(Constants.order_wait);
            order.setNo(orderNo);
            order.setGamePlatformName(Constants.PLATFORM_WM_BIG);
            order.setFirstProxy(user.getFirstProxy());
            order.setSecondProxy(user.getSecondProxy());
            order.setThirdProxy(user.getThirdProxy());
            orderService.save(order);
            log.info("order表记录保存成功，order={}",order.toString());

            //账变中心记录账变
            AccountChangeVo vo = new AccountChangeVo();
            vo.setUserId(authId);
            vo.setChangeEnum(AccountChangeEnum.WM_IN);
            vo.setAmount(userCenterMoney.negate());
            vo.setAmountBefore(userMoney.getMoney());
            vo.setAmountAfter(userMoney.getMoney().subtract(userCenterMoney));
            asyncService.executeAsync(vo);
        }
        //开游戏
        String mode = getMode(gameType);
        //获取进游戏地址
        String url = getOpenGameUrl(request, third, mode, lang,platformConfig);
        if (CommonUtil.checkNull(url)) {
            log.error("userId:{},account:{},进游戏失败",third.getUserId(),user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        return ResponseUtil.success(url);
    }

    /**
     * 获取进游戏地址
     *
     * @param request
     * @param third
     * @param mode
     * @param lang
     * @return
     */
    private String getOpenGameUrl(HttpServletRequest request, UserThird third, String mode, Integer lang,PlatformConfig platformConfig) {
        //检测请求设备
        String ua = request.getHeader("User-Agent");
        boolean checkMobileOrPc = DeviceUtil.checkAgentIsMobile(ua);
        String returnUrl = "";
        if (!checkMobileOrPc) {
            //pc端直接获取请求地址域名作为返回地址
//            String schema = request.getScheme();
//            String host = request.getRemoteHost();
//            int port = request.getRemotePort();
//            returnUrl = schema + "://" + host + ":" + port;
            returnUrl = platformConfig.getDomainNameConfiguration();
            System.out.println(returnUrl);
        }
        String openGameUrl = wmApi.openGame(third.getAccount(), third.getPassword(), lang, null, 4, mode, null, returnUrl);
        return openGameUrl;
    }

    private String getMode(Integer gameType) {
        if (gameType == null) {
            return null;
        }
        String model = "";
// 默认：大厅。1.百家乐。2.龙虎 3. 轮盘 4. 骰宝 5. 牛牛  6. 三公  7. 番摊  8. 色碟 9. 鱼虾蟹 10. 炸金花 11. 牌九 12. 二八杠" 13 安達巴哈,
        switch (gameType) {
            case 1:
                model = "onlybac";
                break;
            case 2:
                model = "onlydgtg";
                break;
            case 3:
                model = "onlyrou";
                break;
            case 4:
                model = "onlysicbo";
                break;
            case 5:
                model = "onlyniuniu";
                break;
            case 6:
                model = "onlysamgong";
                break;
            case 7:
                model = "onlyfantan";
                break;
            case 8:
                model = "onlysedie";
                break;
            case 9:
                model = "onlyfishshrimpcrab";
                break;
            case 10:
                model = "onlygoldenflower";
                break;
            case 11:
                model = "onlypaigow";
                break;
            case 12:
                model = "onlythisbar";
                break;
            case 13:
                model = "onlyandarbahar";
                break;
            default:

        }

        return model;
    }

    public static void main(String[] args) {
        Integer lang = LanguageEnum.getLanguageCode("th_TH");
        System.out.println(lang);
    }

    @ApiOperation("查询当前登录用户WM余额")
//    @RequestLimit(limit = 1, timeout = 5)
    @GetMapping("getWmBalance")
    public ResponseEntity<BigDecimal> getWmBalance() {
        //判断平台状态
//        ResponseEntity response = thirdGameBusiness.checkPlatformStatus(Constants.PLATFORM_WM_BIG);
//        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
//            return response;
//        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null||ObjectUtils.isEmpty(third.getAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        User user = userService.findById(authId);
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        try {
            BigDecimal balance = wmApi.getBalance(third.getAccount(), lang);
            if (balance == null) {
                log.error("userId:{},获取用户WM余额为null",authId);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            return ResponseUtil.success(balance.setScale(2, BigDecimal.ROUND_HALF_UP));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("userId:{},获取用户WM余额失败{}",authId, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
    }

    @ApiOperation("查询用户WM余额外部接口")
//    @RequestLimit(limit = 1, timeout = 5)
    @GetMapping("getWmBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "第三方账号", required = true),
            @ApiImplicitParam(name = "lang", value = "语言", required = true),
    })
    public ResponseEntity<BigDecimal> getWmBalanceApi(String account, Integer lang) {
        //判断平台状态
//        ResponseEntity response = thirdGameBusiness.checkPlatformStatus(Constants.PLATFORM_WM_BIG);
//        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
//            return response;
//        }
        log.info("开始查询WM余额:account={},lang={}", account, lang);
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(account, lang)) {
            return ResponseUtil.parameterNotNull();
        }
        try {
            BigDecimal balance = wmApi.getBalance(account, lang);
            if (balance == null) {
                log.error("account:{},获取用户WM余额为null",account);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            log.info("WM余额查询成功:account={},lang={},balance={}", account, lang, balance);
            return ResponseUtil.success(balance);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("account:{},获取用户WM余额失败{}",account, e.getMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
    }

    @ApiOperation("一键回收当前登录用户WM余额")
    @GetMapping("oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //判断平台状态
        ResponseEntity response = thirdGameBusiness.checkPlatformStatus(Constants.PLATFORM_WM_BIG);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverWm(userId);
    }

    @ApiOperation("一键回收用户WM余额外部接口")
    @GetMapping("oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        //判断平台状态
        ResponseEntity response = thirdGameBusiness.checkPlatformStatus(Constants.PLATFORM_WM_BIG);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverWm(userId);
    }

    @Data
    class RespEntity {
        private Integer errorCode;
        private String errorMessage;
        private Object result;
    }
}

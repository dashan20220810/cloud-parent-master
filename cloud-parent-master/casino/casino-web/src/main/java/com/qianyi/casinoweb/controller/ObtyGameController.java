package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/obtyGame")
@Api(tags = "OB体育游戏厅")
@Slf4j
public class ObtyGameController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PublicObtyApi obtyApi;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private ErrorOrderService errorOrderService;

    @ApiOperation("开游戏")
    @PostMapping("/openGame")
    public ResponseEntity<String> openGame(HttpServletRequest request) {
        //判断平台和游戏状态
        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBTY);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = null;
        synchronized (authId) {
            third = userThirdService.findByUserId(authId);
            //未注册自动注册到第三方
            if (third == null || ObjectUtils.isEmpty(third.getObtyAccount())) {
                String account = UUID.randomUUID().toString();
                account = account.replaceAll("-", "");
                if (account.length() > 20) {
                    account = account.substring(0, 20);
                }
                boolean register = obtyApi.create(account, account);
                if (!register) {
                    log.error("OB体育注册账号失败");
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
                if (third == null) {
                    third = new UserThird();
                    third.setUserId(authId);
                }
                third.setObtyAccount(account);
                try {
                    userThirdService.save(third);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("OB体育本地注册账号失败,userId:{},{}", authId, e.getMessage());
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
            }
        }
        //回收其他游戏的余额
        thirdGameBusiness.oneKeyRecoverOtherGame(authId, Constants.PLATFORM_OBTY);
        //TODO 扣款时考虑当前用户余额大于平台在三方的余额最大只能转入平台余额
        UserMoney userMoney = userMoneyService.findByUserId(authId);
        BigDecimal userCenterMoney = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getMoney() != null) {
            userCenterMoney = userMoney.getMoney();
        }
        //三方要求金额保留到两位小数
        userCenterMoney = userCenterMoney.setScale(2, BigDecimal.ROUND_DOWN);
        String obAccount = third.getObtyAccount();
        User user = userService.findById(authId);
        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
            //钱转入第三方后本地扣减记录账变  优先扣减本地余额，否则会出现三方加点成功，本地扣减失败的情况
            //先扣本地款
            userMoneyService.subMoney(authId, userCenterMoney);
            String orderNo = orderService.getObtyOrderNo();
            //加点
            PublicObtyApi.ResponseEntity transfer = obtyApi.transfer(third.getObtyAccount(), 1, userCenterMoney, orderNo);
            if (transfer == null) {
                log.error("userId:{},account:{},money:{},进OB体育游戏加点失败,远程请求异常", third.getUserId(), user.getAccount(), userCenterMoney);
                //异步记录错误订单并重试补偿
                errorOrderService.syncSaveErrorOrder(third.getObtyAccount(), user.getId(), user.getAccount(), orderNo, userCenterMoney, AccountChangeEnum.OBTY_IN, Constants.PLATFORM_OBTY);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            if (!transfer.getStatus()) {
                log.error("userId:{},进OB体育游戏加点失败,msg:{}", authId, transfer.toString());
                //三方加扣点失败再把钱加回来
                userMoneyService.addMoney(authId, userCenterMoney);
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
            //记录账变
            thirdGameBusiness.inSaveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, "自动转入OB体育", Constants.PLATFORM_OBTY, AccountChangeEnum.OBTY_IN);
        }
        //开游戏
        String ua = request.getHeader("User-Agent");
        boolean checkMobileOrPc = DeviceUtil.checkAgentIsMobile(ua);
        String terminal = "pc";
        if (checkMobileOrPc) {
            terminal = "mobile";
        }
        PublicObtyApi.ResponseEntity login = obtyApi.login(obAccount, terminal, null);
        if (login == null) {
            log.error("userId:{}，account:{},进OB体育游戏登录失败,远程请求异常", authId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!login.getStatus()) {
            log.error("userId:{}，account:{},进OB体育游戏登录失败,msg:{}", authId, user.getAccount(), login.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject data = JSONObject.parseObject(login.getData());
        if (ObjectUtils.isEmpty(data)) {
            return ResponseUtil.success(data);
        }
        String loginUrl = data.getString("loginUrl");
        return ResponseUtil.success(loginUrl);
    }

    @ApiOperation("查询当前登录用户OB体育余额")
    @GetMapping("/getBalance")
    public ResponseEntity<BigDecimal> getBalance() {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getObtyAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        BigDecimal balance = BigDecimal.ZERO;
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceObty(third.getObtyAccount(), authId);
        if (responseEntity.getData() != null) {
            balance = responseEntity.getData();
        }
        balance = balance.setScale(2, BigDecimal.ROUND_HALF_UP);
        responseEntity.setData(balance);
        return responseEntity;
    }

    @ApiOperation("查询用户OB体育余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId) {
        log.info("开始查询OB体育余额:userId={}", userId);
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObtyAccount())) {
            return ResponseUtil.custom("当前用户暂未进入过游戏");
        }
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceObty(third.getObtyAccount(), userId);
        return responseEntity;
    }

    @ApiOperation(value = "一键回收当前登录用户OB体育余额")
    @GetMapping("/oneKeyRecover")
    public ResponseEntity oneKeyRecover() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverObty(userId);
    }

    @ApiOperation(value = "一键回收用户OB体育余额外部接口")
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public ResponseEntity oneKeyRecoverApi(Long userId) {
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverObty(userId);
    }
}

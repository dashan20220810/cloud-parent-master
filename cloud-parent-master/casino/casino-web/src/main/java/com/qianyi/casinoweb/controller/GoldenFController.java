package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.MaintenanceGameVo;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livegoldenf.constants.LanguageEnum;
import com.qianyi.livegoldenf.constants.WalletCodeEnum;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/golednf")
@Api(tags = "GoldenF游戏厅")
@Slf4j
public class GoldenFController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PublicGoldenFApi goldenFApi;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private PlatformGameService platformGameService;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Value("${project.goldenf.currency:null}")
    private String currency;

    @ApiOperation("开游戏")
//    @RequestLimit(limit = 1, timeout = 5)
    @PostMapping("/openGame")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9/SABASPORT", required = true),
            @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = true),
    })
    public ResponseEntity openGame(String vendorCode, String gameCode, HttpServletRequest request) {
        boolean checkNull = CommonUtil.checkNull(vendorCode, gameCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(vendorCode, gameCode);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = null;
        synchronized (authId) {
            third = userThirdService.findByUserId(authId);
            //未注册自动注册到第三方
            if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
                String account = UUID.randomUUID().toString();
                account = account.replaceAll("-", "");
                if (account.length() > 20) {
                    account = account.substring(0, 20);
                }
                boolean register = goldenFApi.playerCreate(account, currency);
                if (!register) {
                    log.error("GoldenF注册账号失败");
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
                if (third == null) {
                    third = new UserThird();
                    third.setUserId(authId);
                }
                third.setGoldenfAccount(account);
                try {
                    userThirdService.save(third);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("本地注册账号失败,userId:{},{}", authId, e.getMessage());
                    return ResponseUtil.custom("服务器异常,请重新操作");
                }
            }
        }
        //回收其他游戏的余额
        //适配之前的游戏
        String platform = Constants.PLATFORM_PG_CQ9;
        AccountChangeEnum changeEnum = AccountChangeEnum.PG_CQ9_IN;
        if (Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
            platform = Constants.PLATFORM_SABASPORT;
            changeEnum = AccountChangeEnum.SABASPORT_IN;
        }
        thirdGameBusiness.oneKeyRecoverOtherGame(authId,platform);
        //TODO 扣款时考虑当前用户余额大于平台在三方的余额最大只能转入平台余额
        UserMoney userMoney = userMoneyService.findByUserId(authId);
        BigDecimal userCenterMoney = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getMoney() != null) {
            userCenterMoney = userMoney.getMoney();
        }

        String goldenfAccount = third.getGoldenfAccount();
        if (userCenterMoney.compareTo(BigDecimal.ZERO) == 1) {
            String orderNo = orderService.getOrderNo();
            //加点
            ResponseEntity responseEntity = transferIn(vendorCode, goldenfAccount, authId, userCenterMoney, orderNo, platform, changeEnum);
            if (responseEntity != null) {
                return responseEntity;
            }
            //记录账变
            saveAccountChange(authId, userCenterMoney, userMoney.getMoney(), userMoney.getMoney().subtract(userCenterMoney), 0, orderNo, platform, changeEnum);
        }
        //开游戏
        String language = request.getHeader(Constants.LANGUAGE);
        String languageCode = LanguageEnum.getLanguageCode(language);
        PublicGoldenFApi.ResponseEntity entity = goldenFApi.startGame(goldenfAccount, gameCode, languageCode, null);
        if (entity == null) {
            log.error("userId:{},进游戏获取gameUrl失败", third.getUserId());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if ("1035".equals(entity.getErrorCode())){
            log.error("userId:{},errorCode={},errorMsg={}", third.getUserId(), entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("当前游戏维护中,请选择其他游戏");
        }
        if (!ObjectUtils.isEmpty(entity.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", third.getUserId(), entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonData = JSONObject.parseObject(entity.getData());
        String gameUrl = jsonData.getString("game_url");
        return ResponseUtil.success(gameUrl);
    }

    public void saveAccountChange(Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo, String platform, AccountChangeEnum changeEnum) {
        User user = userService.findById(userId);
        String remark = "自动转入PG/CQ9";
        if (Constants.PLATFORM_SABASPORT.equals(platform)) {
            remark = "自动转入SABASPORT";
        }
        Order order = new Order();
        order.setMoney(amount);
        order.setUserId(userId);
        order.setRemark(remark);
        order.setType(type);
        order.setState(Constants.order_wait);
        order.setNo(orderNo);
        order.setGamePlatformName(platform);
        order.setFirstProxy(user.getFirstProxy());
        order.setSecondProxy(user.getSecondProxy());
        order.setThirdProxy(user.getThirdProxy());
        orderService.save(order);

        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount.negate());
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }

    public ResponseEntity transferIn(String vendorCode, String playerName, Long userId, BigDecimal userCenterMoney, String orderNo, String platform, AccountChangeEnum changeEnum) {
        //优先扣减本地的钱
        userMoneyService.subMoney(userId, userCenterMoney);
        double amount = userCenterMoney.doubleValue();
        String walletCode = WalletCodeEnum.getWalletCodeByVendorCode(vendorCode);
        PublicGoldenFApi.ResponseEntity entity = goldenFApi.transferIn(playerName, amount, orderNo, walletCode);
        if (entity == null) {
            User user = userService.findById(userId);
            errorOrderService.syncSaveErrorOrder(playerName, userId, user.getAccount(), orderNo, userCenterMoney, changeEnum, platform);
            log.error("userId:{},进游戏加扣点失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(entity.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, entity.getErrorCode(), entity.getErrorMessage());
            userMoneyService.addMoney(userId, userCenterMoney);
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        //三方强烈建议提值/充值后使用 5.9 获取单个玩家的转账记录 进一步确认交易是否成功，避免造成金额损失
        long time = System.currentTimeMillis();
        PublicGoldenFApi.ResponseEntity playerTransactionRecord = goldenFApi.getPlayerTransactionRecord(playerName, time, time, walletCode, orderNo, null);
        if (playerTransactionRecord == null) {
            log.error("userId:{},进游戏查询转账记录失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerTransactionRecord.getErrorCode())) {
            log.error("userId:{},errorCode={},errorMsg={}", userId, playerTransactionRecord.getErrorCode(), playerTransactionRecord.getErrorMessage());
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        JSONObject jsonData = JSONObject.parseObject(playerTransactionRecord.getData());
        JSONArray translogs = jsonData.getJSONArray("translogs");
        if (translogs.size() == 0) {
            userMoneyService.addMoney(userId, userCenterMoney);
            return ResponseUtil.custom("加点失败,请联系客服");
        }
        return null;
    }

    @ApiOperation("PG游戏试玩,CQ9,沙巴不支持试玩")
    @PostMapping("/openGameDemo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9/SABASPORT", required = true),
            @ApiImplicitParam(name = "gameCode", value = "游戏代码", required = true),
    })
    @NoAuthentication
    public ResponseEntity gameDemo(String vendorCode, String gameCode, HttpServletRequest request) {
        boolean checkNull = CommonUtil.checkNull(vendorCode, gameCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        //判断平台和游戏状态
        ResponseEntity response = thirdGameBusiness.checkPlatformAndGameStatus(vendorCode, gameCode);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()) {
            return response;
        }
        if (!Constants.PLATFORM_PG.equals(vendorCode)) {
            return ResponseUtil.custom("游戏不支持试玩");
        }
        //开游戏
        String language = request.getHeader(Constants.LANGUAGE);
        String languageCode = LanguageEnum.getLanguageCode(language);
        PublicGoldenFApi.ResponseEntity entity = goldenFApi.startGameDemo(gameCode, languageCode);
        if (entity == null) {
            log.error("PG游戏试玩获取gameUrl失败");
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(entity.getErrorCode())) {
            log.error("errorCode={},errorMsg={}", entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonData = JSONObject.parseObject(entity.getData());
        String gameUrl = jsonData.getString("game_url");
        return ResponseUtil.success(gameUrl);
    }

    @ApiOperation("查询当前登录用户PG/CQ9/SABASPORT余额")
    @GetMapping("/getBalance")
    @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9/SABASPORT", required = false)
    public ResponseEntity<BigDecimal> getBalance(String vendorCode) {
        //获取登陆用户
        Long authId = CasinoWebUtil.getAuthId();
        UserThird third = userThirdService.findByUserId(authId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceGoldenF(third.getGoldenfAccount(), authId, vendorCode);
        if (responseEntity.getData() != null) {
            responseEntity.setData(responseEntity.getData().setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        return responseEntity;
    }

    @ApiOperation("查询用户PG/CQ9/SABASPORT余额外部接口")
    @GetMapping("/getBalanceApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true),
            @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9/SABASPORT", required = false)
    })
    public ResponseEntity<BigDecimal> getBalanceApi(Long userId,String vendorCode) {
        log.info("开始查询{}余额:userId={}", vendorCode,userId);
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        if (CasinoWebUtil.checkNull(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.custom("当前用户暂未进入过游戏");
        }
        ResponseEntity<BigDecimal> responseEntity = thirdGameBusiness.getBalanceGoldenF(third.getGoldenfAccount(), userId, vendorCode);
        return responseEntity;
    }

    @ApiOperation("一键回收当前登录用户PG/CQ9/SABASPORT余额")
    @GetMapping("/oneKeyRecover")
    @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9/SABASPORT", required = false)
    public ResponseEntity oneKeyRecover(String vendorCode) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        return thirdGameBusiness.oneKeyRecoverGoldenF(userId,vendorCode);
    }

    @ApiOperation("一键回收用户PG/CQ9/SABASPORT余额外部接口")
    @GetMapping("/oneKeyRecoverApi")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户ID", required = true),
            @ApiImplicitParam(name = "vendorCode", value = "产品代码:PG/CQ9/SABASPORT", required = false)
    })
    public ResponseEntity oneKeyRecoverApi(Long userId,String vendorCode) {
        Boolean ipWhiteCheck = thirdGameBusiness.ipWhiteCheck();
        if (!ipWhiteCheck) {
            return ResponseUtil.custom("ip禁止访问");
        }
        return thirdGameBusiness.oneKeyRecoverGoldenF(userId,vendorCode);
    }

    @ApiOperation("查询WM,PG,CQ9,OB电竞,OB体育,沙巴体育，隐藏、维护状态的游戏")
    @GetMapping("/maintenanceGameList")
    @NoAuthentication
    public ResponseEntity<List<MaintenanceGameVo>> maintenanceGameList() {
        List<PlatformGame> platformGameList = platformGameService.findAll();
        List<MaintenanceGameVo> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(platformGameList)) {
            return ResponseUtil.success(list);
        }
        for (PlatformGame platformGame : platformGameList) {
            MaintenanceGameVo vo = getMaintenanceGame(platformGame);
            list.add(vo);
        }
        return ResponseUtil.success(list);
    }

    private MaintenanceGameVo getMaintenanceGame(PlatformGame platformGame) {
        MaintenanceGameVo vo = new MaintenanceGameVo();
        vo.setGamePlatformName(platformGame.getGamePlatformName());
        vo.setPlatformStatus(platformGame.getGameStatus());
        //维护关闭状态的游戏
        List<Integer> gameStatusList = new ArrayList<>();
        gameStatusList.add(0);
        gameStatusList.add(2);
        List<AdGame> gameList = adGamesService.findByGamePlatformNameAndGamesStatusIn(platformGame.getGamePlatformName(), gameStatusList);
        vo.setGameList(gameList);
        return vo;
    }
}

package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import com.qianyi.livegoldenf.constants.WalletCodeEnum;
import com.qianyi.liveob.api.PublicObdjApi;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class ThirdGameBusiness {

    @Autowired
    private UserThirdService userThirdService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserService userService;
    @Autowired
    private PublicGoldenFApi goldenFApi;
    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private PublicObdjApi obdjApi;
    @Autowired
    private PublicObtyApi obtyApi;
    @Autowired
    private PlatformGameService platformGameService;
    @Autowired
    private AdGamesService adGamesService;
    @Autowired
    private ErrorOrderService errorOrderService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Autowired
    @Qualifier("asyncExecutor")
    private Executor executor;
    @Value("${project.ipWhite:null}")
    private String ipWhite;

    public ResponseEntity oneKeyRecoverGoldenF(Long userId,String vendorCode) {
        //适配PG/CQ9
        if (ObjectUtils.isEmpty(vendorCode)) {
            vendorCode = Constants.PLATFORM_PG_CQ9;
        }
        log.info("开始回收{}余额，userId={}", vendorCode,userId);
        if (ObjectUtils.isEmpty(userId)) {
            return ResponseUtil.parameterNotNull();
        }
        if (Constants.PLATFORM_PG_CQ9.equals(vendorCode)){
            ResponseEntity pgEnable = checkPlatformStatus(Constants.PLATFORM_PG);
            ResponseEntity cq9Enable = checkPlatformStatus(Constants.PLATFORM_CQ9);
            if (pgEnable.getCode() != ResponseCode.SUCCESS.getCode() && cq9Enable.getCode() != ResponseCode.SUCCESS.getCode()) {
                log.info("后台开启PG/CQ9维护，禁止回收，pgResponse={},cq9Response={}", pgEnable, cq9Enable);
                return pgEnable;
            }
        }
        if (Constants.PLATFORM_SABASPORT.equals(vendorCode)){
            ResponseEntity sabaEnable = checkPlatformStatus(Constants.PLATFORM_SABASPORT);
            if (sabaEnable.getCode() != ResponseCode.SUCCESS.getCode()) {
                log.info("后台开启SABASPORT维护，禁止回收，sabaResponse={}", sabaEnable);
                return sabaEnable;
            }
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getGoldenfAccount())) {
            return ResponseUtil.custom(vendorCode + "余额为0");
        }
        ResponseEntity<BigDecimal> responseEntity = getBalanceGoldenF(third.getGoldenfAccount(), userId,vendorCode);
        if (responseEntity.getCode() != ResponseCode.SUCCESS.getCode()) {
            return responseEntity;
        }
        BigDecimal balance = responseEntity.getData();
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},balance={},{}金额小于1，不可回收", userId, balance,vendorCode);
            return ResponseUtil.custom(vendorCode + "余额小于1,不可回收");
        }
        String orderNo = orderService.getOrderNo();
        String goldenfAccount = third.getGoldenfAccount();
        //调用提值接口扣减余额  存在精度问题，只回收整数部分
        BigDecimal recoverMoney = balance.setScale(0, BigDecimal.ROUND_DOWN);
        String walletCode = WalletCodeEnum.getWalletCodeByVendorCode(vendorCode);
        PublicGoldenFApi.ResponseEntity transferOut = goldenFApi.transferOut(goldenfAccount, recoverMoney.doubleValue(), orderNo, walletCode);
        AccountChangeEnum changeEnum = AccountChangeEnum.PG_CQ9_OUT;
        String platform = Constants.PLATFORM_PG_CQ9;
        String remark="自动转出PG/CQ9";
        if (Constants.PLATFORM_SABASPORT.equals(vendorCode)) {
            changeEnum = AccountChangeEnum.SABASPORT_OUT;
            platform = Constants.PLATFORM_SABASPORT;
            remark="自动转出SABASPORT";
        }
        if (transferOut == null) {
            User user = userService.findById(userId);
            errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, recoverMoney, changeEnum, platform);
            log.error("userId:{},一键回收当前登录用户{}余额失败", userId,vendorCode);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(transferOut.getErrorCode())) {
            log.error("{}余额回收失败,userId:{},errorCode={},errorMsg={}", vendorCode,userId, transferOut.getErrorCode(), transferOut.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //三方强烈建议提值/充值后使用 5.9 获取单个玩家的转账记录 进一步确认交易是否成功，避免造成金额损失
        long time = System.currentTimeMillis();
        PublicGoldenFApi.ResponseEntity playerTransactionRecord = goldenFApi.getPlayerTransactionRecord(goldenfAccount, time, time, walletCode, orderNo, null);
        if (playerTransactionRecord == null) {
            log.error("userId:{},{}一键回收余额查询转账记录失败,远程请求异常", userId,vendorCode);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerTransactionRecord.getErrorCode())) {
            log.error("{}一键回收余额查询转账记录失败,userId:{},errorCode={},errorMsg={}", vendorCode,userId, playerTransactionRecord.getErrorCode(), playerTransactionRecord.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        JSONObject jsonData = JSONObject.parseObject(playerTransactionRecord.getData());
        JSONArray translogs = jsonData.getJSONArray("translogs");
        if (translogs.size() == 0) {
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, recoverMoney);
        //记录账变
        User user = userService.findById(userId);
        saveAccountChange(platform,userId, recoverMoney, userMoney.getMoney(), recoverMoney.add(userMoney.getMoney()), 1, orderNo, changeEnum,remark, user);
        log.info("{}余额回收成功，userId={}", vendorCode,userId);
        return ResponseUtil.success();
    }

    public ResponseEntity oneKeyRecoverWm(Long userId) {
        ResponseEntity response = checkPlatformStatus(Constants.PLATFORM_WM_BIG);
        if (response.getCode() != ResponseCode.SUCCESS.getCode()){
            log.info("后台开启WM维护，禁止回收，response={}",response);
            return response;
        }
        log.info("开始回收wm余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getAccount())) {
            return ResponseUtil.custom("WM余额为0");
        }
        User user = userService.findById(userId);
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        String account = third.getAccount();
        //先退出游戏
        Boolean aBoolean = wmApi.logoutGame(account, lang);
        if (!aBoolean) {
            log.error("userId:{},account={},WM退出游戏失败", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        //查询用户在wm的余额
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = wmApi.getBalance(account, lang);
            if (balance == null) {
                log.error("userId:{},account={},获取用户WM余额为null", userId, user.getAccount());
                return ResponseUtil.custom("服务器异常,请重新操作");
            }
        } catch (Exception e) {
            log.error("userId:{},account={},获取用户WM余额失败{}", userId, user.getAccount(), e.getMessage());
            e.printStackTrace();
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},WM金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("WM余额小于1,不可回收");
        }
        //调用加扣点接口扣减wm余额  存在精度问题，只回收整数部分
        BigDecimal recoverMoney = balance.negate().setScale(0, BigDecimal.ROUND_DOWN);
        String orderNo = orderService.getOrderNo();
        PublicWMApi.ResponseEntity entity = wmApi.changeBalance(account, recoverMoney, orderNo, lang);
        if (entity == null) {
            log.error("WM加扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), recoverMoney);
            //异步记录错误订单并重试补偿
            errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, recoverMoney, AccountChangeEnum.RECOVERY, Constants.PLATFORM_WM_BIG);
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        if (entity.getErrorCode() != 0) {
            log.error("WM加扣点失败，userId:{},account={},money={},errorCode={},errorMsg={}", userId, user.getAccount(), recoverMoney, entity.getErrorCode(), entity.getErrorMessage());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        balance = recoverMoney.abs();
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        saveAccountChange(Constants.PLATFORM_WM_BIG, userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.RECOVERY, "自动转出WM", user);
        log.info("wm余额回收成功，userId={},account={}", userId, user.getAccount());
        return ResponseUtil.success();
    }

    public ResponseEntity oneKeyRecoverObdj(Long userId) {
        ResponseEntity obdjEnable = checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBDJ);
        if (obdjEnable.getCode() != ResponseCode.SUCCESS.getCode()){
            log.info("后台开启OBDJ维护，禁止回收，response={}",obdjEnable);
            return obdjEnable;
        }
        log.info("开始回收OB电竞余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObdjAccount())) {
            return ResponseUtil.custom("OB电竞余额为0");
        }
        User user = userService.findById(userId);
        String account = third.getObdjAccount();
        PublicObdjApi.ResponseEntity obApiBalance = obdjApi.getBalance(account);
        if (obApiBalance == null) {
            log.error("userId:{},account={},获取用户OB电竞余额失败,远程请求异常", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (PublicObdjApi.STATUS_FALSE.equals(obApiBalance.getStatus())) {
            log.error("userId:{},account={},获取用户OB电竞余额失败，msg={}", userId, user.getAccount(), obApiBalance.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (ObjectUtils.isEmpty(obApiBalance.getData())) {
            log.error("userId:{},account={},获取用户OB电竞余额为null,msg={}", userId, user.getAccount(), obApiBalance.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        BigDecimal balance = new BigDecimal(obApiBalance.getData());
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},OB电竞金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("OB电竞余额小于1,不可回收");
        }
        //调用加扣点接口扣减OB电竞余额  存在精度问题，只回收整数部分
        balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
        String orderNo = orderService.getObdjOrderNo();
        PublicObdjApi.ResponseEntity transfer = obdjApi.transfer(account, 2, balance, orderNo);
        if (transfer == null) {
            log.error("OB电竞扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), balance);
            //异步记录错误订单
            errorOrderService.syncSaveErrorOrder(third.getAccount(), user.getId(), user.getAccount(), orderNo, balance, AccountChangeEnum.OBDJ_OUT, Constants.PLATFORM_OBDJ);
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        if (PublicObdjApi.STATUS_FALSE.equals(transfer.getStatus())) {
            log.error("OB电竞扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance, transfer.getData());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        log.info("OB电竞余额,userMoney加回成功，userId={},balance={}", userId, balance);
        saveAccountChange(Constants.PLATFORM_OBDJ, userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.OBDJ_OUT, "自动转出OB电竞", user);
        log.info("OB电竞余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
        return ResponseUtil.success();
    }

    public ResponseEntity oneKeyRecoverObty(Long userId) {
        ResponseEntity obtyEnable = checkPlatformAndGameStatus(Constants.PLATFORM_OB, Constants.PLATFORM_OBTY);
        if (obtyEnable.getCode() != ResponseCode.SUCCESS.getCode()){
            log.info("后台开启OBTY维护，禁止回收，response={}",obtyEnable);
            return obtyEnable;
        }
        log.info("开始回收OB体育余额，userId={}", userId);
        if (userId == null) {
            return ResponseUtil.parameterNotNull();
        }
        UserThird third = userThirdService.findByUserId(userId);
        if (third == null || ObjectUtils.isEmpty(third.getObtyAccount())) {
            return ResponseUtil.custom("OB体育余额为0");
        }
        User user = userService.findById(userId);
        String account = third.getObtyAccount();
        //先退出
        PublicObtyApi.ResponseEntity logoutResult = obtyApi.kickOutUser(account);
        if (logoutResult==null){
            log.error("userId:{},account={},OB体育退出失败,远程请求异常", userId, user.getAccount());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!logoutResult.getStatus()){
            log.error("userId:{},account={},OB体育退出失败,result={}", userId, logoutResult.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        ResponseEntity<BigDecimal> balanceObty = getBalanceObty(account, userId);
        if (balanceObty.getCode()!=ResponseCode.SUCCESS.getCode()){
           return balanceObty;
        }
        BigDecimal balance = balanceObty.getData();
        if (balance.compareTo(BigDecimal.ONE) == -1) {
            log.info("userId:{},account={},balance={},OB体育金额小于1，不可回收", userId, user.getAccount(), balance);
            return ResponseUtil.custom("OB体育余额小于1,不可回收");
        }
        //调用加扣点接口扣减OB电竞余额  存在精度问题，只回收整数部分
        balance = balance.setScale(0, BigDecimal.ROUND_DOWN);
        String orderNo = orderService.getObtyOrderNo();
        PublicObtyApi.ResponseEntity transfer = obtyApi.transfer(account, 2, balance, orderNo);
        if (transfer == null) {
            log.error("OB体育扣点失败,远程请求异常,userId:{},account={},money={}", userId, user.getAccount(), balance);
            //异步记录错误订单
            errorOrderService.syncSaveErrorOrder(third.getObtyAccount(), user.getId(), user.getAccount(), orderNo, balance, AccountChangeEnum.OBTY_OUT, Constants.PLATFORM_OBTY);
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        if (!transfer.getStatus()) {
            log.error("OB体育扣点失败,userId:{},account={},money={},msg={}", userId, user.getAccount(), balance, transfer.toString());
            return ResponseUtil.custom("回收失败,请联系客服");
        }
        //把额度加回本地
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            userMoney = new UserMoney();
            userMoney.setUserId(userId);
            userMoneyService.save(userMoney);
        }
        userMoneyService.addMoney(userId, balance);
        log.info("OB体育余额,userMoney加回成功，userId={},balance={}", userId, balance);
        saveAccountChange(Constants.PLATFORM_OBTY, userId, balance, userMoney.getMoney(), balance.add(userMoney.getMoney()), 1, orderNo, AccountChangeEnum.OBTY_OUT, "自动转出OB体育", user);
        log.info("OB体育余额回收成功，userId={},account={},money={}", userId, user.getAccount(), balance);
        return ResponseUtil.success();
    }

    public ResponseEntity<BigDecimal> getBalanceGoldenF(String account, Long userId,String vendorCode) {
        String walletCode = WalletCodeEnum.getWalletCodeByVendorCode(vendorCode);
        PublicGoldenFApi.ResponseEntity playerBalance = goldenFApi.getPlayerBalance(account, walletCode);
        if (playerBalance == null) {
            log.error("userId:{},查询GoldenF余额失败", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!ObjectUtils.isEmpty(playerBalance.getErrorCode())) {
            log.error("查询GoldenF余额失败,userId:{},account={},errorCode={},errorMsg={}", userId, account, playerBalance.getErrorCode(), playerBalance.getErrorMessage());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonData = JSONObject.parseObject(playerBalance.getData());
        BigDecimal balance = new BigDecimal(jsonData.getDouble("balance"));
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getBalanceObdj(String account, Long userId) {
        PublicObdjApi.ResponseEntity balanceResult = obdjApi.getBalance(account);
        if (balanceResult == null) {
            log.error("userId:{},查询OB电竞余额失败,远程请求异常", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (PublicObdjApi.STATUS_FALSE.equals(balanceResult.getStatus())) {
            log.error("userId:{},查询OB电竞余额失败,msg:{}", userId, balanceResult.getData());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        BigDecimal balance = new BigDecimal(balanceResult.getData());
        return ResponseUtil.success(balance);
    }

    public ResponseEntity<BigDecimal> getBalanceObty(String account, Long userId) {
        PublicObtyApi.ResponseEntity balanceResult = obtyApi.checkBalance(account);
        if (balanceResult == null) {
            log.error("userId:{},查询OB体育余额失败,远程请求异常", userId);
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        if (!balanceResult.getStatus()) {
            log.error("userId:{},查询OB体育余额失败,balanceResult={}", userId, balanceResult.toString());
            return ResponseUtil.custom("服务器异常,请重新操作");
        }
        JSONObject jsonObject = JSONObject.parseObject(balanceResult.getData());
        BigDecimal balance = new BigDecimal(jsonObject.getString("balance"));
        return ResponseUtil.success(balance);
    }

    /**
     * 进游戏时一键回收其他游戏的金额(不包含当前游戏)
     * @param userId
     * @param platform
     * @return
     */
    public ResponseEntity oneKeyRecoverOtherGame(Long userId,String platform) {
        List<CompletableFuture> completableFutures = new ArrayList<>();
        if (!Constants.PLATFORM_WM_BIG.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverWm = CompletableFuture.runAsync(() -> {
                oneKeyRecoverWm(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverWm);
        }
        if (!Constants.PLATFORM_PG_CQ9.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverGoldenF = CompletableFuture.runAsync(() -> {
                oneKeyRecoverGoldenF(userId,Constants.PLATFORM_PG_CQ9);
            }, executor);
            completableFutures.add(oneKeyRecoverGoldenF);
        }
        if (!Constants.PLATFORM_SABASPORT.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverGoldenF = CompletableFuture.runAsync(() -> {
                oneKeyRecoverGoldenF(userId,Constants.PLATFORM_SABASPORT);
            }, executor);
            completableFutures.add(oneKeyRecoverGoldenF);
        }
        if (!Constants.PLATFORM_OBDJ.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverObdj = CompletableFuture.runAsync(() -> {
                oneKeyRecoverObdj(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverObdj);
        }
        if (!Constants.PLATFORM_OBTY.equals(platform)) {
            CompletableFuture<Void> oneKeyRecoverObty = CompletableFuture.runAsync(() -> {
                oneKeyRecoverObty(userId);
            }, executor);
            completableFutures.add(oneKeyRecoverObty);
        }
        //等待所有子线程计算完成
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).join();
        return ResponseUtil.success();
    }

    public void saveAccountChange(String gamePlatformName, Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo, AccountChangeEnum changeEnum, String remark, User user) {
        Order order = new Order();
        order.setMoney(amount);
        order.setUserId(userId);
        order.setRemark(remark);
        order.setType(type);
        order.setState(Constants.order_wait);
        order.setNo(orderNo);
        order.setFirstProxy(user.getFirstProxy());
        order.setSecondProxy(user.getSecondProxy());
        order.setThirdProxy(user.getThirdProxy());
        order.setGamePlatformName(gamePlatformName);
        orderService.save(order);
        log.info("order表记录保存成功，order={}", order.toString());

        //账变中心记录账变
        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount);
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }


    public ResponseEntity checkPlatformStatus(String vendorCode) {
        if (ObjectUtils.isEmpty(vendorCode)) {
            return ResponseUtil.custom("平台不存在");
        }
        PlatformGame platformGame = platformGameService.findByGamePlatformName(vendorCode);
        if (platformGame == null) {
            return ResponseUtil.custom("平台不存在");
        }
        if (platformGame.getGameStatus() == 0) {
            return ResponseUtil.custom("平台维护中");
        }
        if (platformGame.getGameStatus() == 2) {
            return ResponseUtil.custom("平台已关闭");
        }
        return ResponseUtil.success();
    }

    public ResponseEntity checkGameStatus(String vendorCode, String gameCode) {
        if (ObjectUtils.isEmpty(vendorCode)) {
            return ResponseUtil.custom("平台不存在");
        }
        if (ObjectUtils.isEmpty(gameCode)) {
            return ResponseUtil.custom("游戏不存在");
        }
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(vendorCode, gameCode);
        if (adGame == null) {
            return ResponseUtil.custom("游戏不存在");
        }
        if (adGame.getGamesStatus() == 0) {
            return ResponseUtil.custom("游戏维护中");
        }
        if (adGame.getGamesStatus() == 2) {
            return ResponseUtil.custom("游戏已关闭");
        }
        return ResponseUtil.success();
    }

    public ResponseEntity checkPlatformAndGameStatus(String vendorCode, String gameCode) {
        ResponseEntity presponse = checkPlatformStatus(vendorCode);
        if (presponse.getCode() != ResponseCode.SUCCESS.getCode()) {
            return presponse;
        }
        ResponseEntity gresponse = checkGameStatus(vendorCode, gameCode);
        return gresponse;
    }

    public void inSaveAccountChange(Long userId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter, Integer type, String orderNo, String remark,String platform,AccountChangeEnum changeEnum ) {
        User user = userService.findById(userId);
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

    public Boolean ipWhiteCheck() {
        if (ObjectUtils.isEmpty(ipWhite)) {
            return false;
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = IpUtil.getIp(request);
        String[] ipWhiteArray = ipWhite.split(",");
        for (String ipw : ipWhiteArray) {
            if (!ObjectUtils.isEmpty(ipw) && ipw.trim().equals(ip)) {
                return true;
            }
        }
        return false;
    }
}

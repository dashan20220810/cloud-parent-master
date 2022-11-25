package com.qianyi.casinoproxy.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.business.ChargeOrderBusiness;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.GenerateInviteCodeRunner;
import com.qianyi.casinocore.util.PasswordUtil;
import com.qianyi.casinocore.vo.CodeNumConfigVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.UserVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对用户表进行增删改查操作
 */
@RestController
@RequestMapping("user")
@Api(tags = "客户中心")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    UserThirdService userThirdService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private GameRecordService gameRecordService;

    public final static String agentOfBelonging = "{0}(区域)-{1}(基层)";

    @ApiOperation("查询代理下级的用户数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("getProxyUser")
    public ResponseEntity<UserVo> getProxyUser(Long id){
        User user = userService.findById(id);
        if (CasinoProxyUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }

        //查询直属代理会员
        List<User> firstUserList = userService.findFirstUser(id);
        List<Long> userIds = firstUserList.stream().map(User::getId).collect(Collectors.toList());
        if(userIds == null || userIds.size() == 0){
            List<UserMoney> userMoneyList =  userMoneyService.findAll(userIds);
            List<UserVo> userVoList = getUserVoList(firstUserList, userMoneyList);
            return ResponseUtil.success(userVoList);
        }

        List<User> secordUsersList = userService.findFirstUserList(userIds);
        secordUsersList.forEach(u -> firstUserList.add(u));

        List<Long> thridUserId = secordUsersList.stream().map(User::getId).collect(Collectors.toList());
        if(userIds == null || userIds.size() == 0){
            List<Long> ids = firstUserList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> userMoneyList =  userMoneyService.findAll(ids);
            List<UserVo> userVoList = getUserVoList(firstUserList, userMoneyList);
            return ResponseUtil.success(userVoList);
        }

        List<User> thridUsersList = userService.findFirstUserList(thridUserId);
        thridUsersList.forEach(u -> firstUserList.add(u));

        List<Long> ids = firstUserList.stream().map(User::getId).collect(Collectors.toList());
        List<UserMoney> userMoneyList =  userMoneyService.findAll(ids);
        List<UserVo> userVoList = getUserVoList(firstUserList, userMoneyList);
        return ResponseUtil.success(userVoList);
    }

    private List<UserVo> getUserVoList(List<User> firstUserList, List<UserMoney> userMoneyList) {
        List<UserVo> userVoList = new ArrayList<>();
        for (User user : firstUserList) {
            UserVo userVo = new UserVo(user);
            userMoneyList.stream().forEach(userMoney -> {
                if(user.getId().equals(userMoney.getUserId())){
                    userVo.setMoney(userMoney.getMoney());
                    userVo.setCodeNum(userMoney.getCodeNum());
                    userVo.setWithdrawMoney(userMoney.getWithdrawMoney());//可以提现金额
                }
            });
            userVoList.add(userVo);
        }
        return userVoList;
    }

    /**
     * 用户列表总计
     * @return
     */
    @ApiOperation("用户列表总计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "secondProxyAccount", value = "区域代理账号", required = false),
            @ApiImplicitParam(name = "thirdProxyAccount", value = "基层代理账号", required = false),
            @ApiImplicitParam(name = "state", value = "1：启用，其他：禁用", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    @GetMapping("findUserTotal")
    public ResponseEntity<BigDecimal> findUserTotal(  String account,Integer state,
                                                    String secondProxyAccount,String thirdProxyAccount,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        //后续扩展加参数。
        User user = new User();
        user.setAccount(account);
        user.setState(state);
        if (CasinoProxyUtil.setParameter(user)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        if (!CasinoProxyUtil.checkNull(secondProxyAccount)){
            ProxyUser byUserName = proxyUserService.findByUserName(secondProxyAccount);
            user.setSecondProxy(byUserName == null?CommonConst.LONG_0:byUserName.getId());
        }
        if (!CasinoProxyUtil.checkNull(thirdProxyAccount)){
            ProxyUser byUserName = proxyUserService.findByUserName(thirdProxyAccount);
            user.setThirdProxy(byUserName == null?CommonConst.LONG_0:byUserName.getId());
        }
        List<User> userList = userService.findUserList(user, startDate, endDate);

        if(userList != null && userList.size() > 0){
            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> all = userMoneyService.findAll(userIds);
            BigDecimal sum = all.stream().map(UserMoney::getMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            return ResponseUtil.success(sum);
        }
        return ResponseUtil.success(BigDecimal.ZERO);
    }


    /**
     * 查询操作
     * 注意：jpa 是从第0页开始的
     * @return
     */
    @ApiOperation("用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "secondProxyAccount", value = "区域代理账号", required = false),
            @ApiImplicitParam(name = "thirdProxyAccount", value = "基层代理账号", required = false),
            @ApiImplicitParam(name = "state", value = "1：启用，其他：禁用", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
    })
    @GetMapping("findUserList")
    public ResponseEntity<UserVo> findUserList(Integer pageSize, Integer pageCode, String account,Integer state,
                                               String secondProxyAccount,String thirdProxyAccount,
                                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){

        //后续扩展加参数。
        User user = new User();
        if (CasinoProxyUtil.setParameter(user)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        if (!CasinoProxyUtil.checkNull(secondProxyAccount)){
            ProxyUser byUserName = proxyUserService.findByUserName(secondProxyAccount);
            user.setSecondProxy(byUserName == null?CommonConst.LONG_0:byUserName.getId());
        }
        if (!CasinoProxyUtil.checkNull(thirdProxyAccount)){
            ProxyUser byUserName = proxyUserService.findByUserName(thirdProxyAccount);
            user.setThirdProxy(byUserName == null?CommonConst.LONG_0:byUserName.getId());
        }
        user.setAccount(account);
        user.setState(state);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        Page<User> userPage = userService.findUserPage(pageable, user,startDate,endDate);
        PageResultVO<UserVo> pageResultVO = new PageResultVO(userPage);
        List<User> userList = userPage.getContent();
        if(userList != null && userList.size() > 0){
            List<UserVo> userVoList = new LinkedList();
            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> userMoneyList =  userMoneyService.findAll(userIds);
            List<Long> thirdProxys = userList.stream().map(User::getThirdProxy).collect(Collectors.toList());
            List<Long> secondProxys = userList.stream().map(User::getSecondProxy).collect(Collectors.toList());
            thirdProxys.addAll(secondProxys);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(thirdProxys);
            if(userMoneyList != null){
                userList.stream().forEach(u -> {
                    UserVo userVo = new UserVo(u);
                    userMoneyList.stream().forEach(userMoney -> {
                        if(u.getId().equals(userMoney.getUserId())){
                            userVo.setMoney(userMoney.getMoney());
                            userVo.setCodeNum(userMoney.getCodeNum());
                            userVo.setWithdrawMoney(userMoney.getWithdrawMoney());//可以提现金额
                        }
                    });
                    proxyUsers.stream().forEach(proxyUser -> {
                        if(proxyUser.getId().equals(u.getThirdProxy() == null ? "":u.getThirdProxy())){
                            userVo.setThirdProxyAccount(proxyUser.getUserName());
                            userVo.setThirdProxyId(proxyUser.getId());
                        }
                        if(proxyUser.getId().equals(u.getSecondProxy() == null ? "":u.getSecondProxy())){
                            userVo.setSecondProxyAccount(proxyUser.getUserName());
                        }
                    });
                    GameRecord gameRecord = gameRecordService.findRecordRecordSum(u.getId(), null, null);
                    userVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
                    userVoList.add(userVo);
                });
                pageResultVO.setContent(userVoList);
            }
//            this.setWMMoney(userList);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("刷新WM余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshWM")
    public ResponseEntity getWMMoney(Long id){
        User user = userService.findById(id);
        if (CasinoProxyUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }
        UserThird userThird = userThirdService.findByUserId(user.getId());
        if (CasinoProxyUtil.checkNull(userThird)  || CasinoProxyUtil.checkNull(userThird.getAccount()) ){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.getWMonetUser(user, userThird);
        if (CasinoProxyUtil.checkNull(jsonObject) || CasinoProxyUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("查询WM余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (CasinoProxyUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询WM余额失败");
        }
    }

    @ApiOperation("查询用户PG/CQ9余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshPGAndCQ9")
    public ResponseEntity refreshPGAndCQ9(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (CasinoProxyUtil.checkNull(third) || ObjectUtils.isEmpty(third.getGoldenfAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshPGAndCQ9(third.getUserId());
        if (CasinoProxyUtil.checkNull(jsonObject) || CasinoProxyUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("查询PG/CQ9余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (CasinoProxyUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询PG/CQ9余额失败");
        }
    }

    @ApiOperation("查询用户OB余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshOB")
    public ResponseEntity refreshOB(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (CasinoProxyUtil.checkNull(third) || ObjectUtils.isEmpty(third.getGoldenfAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshOB(third.getUserId());
        if (CasinoProxyUtil.checkNull(jsonObject) || CasinoProxyUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("OB余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (CasinoProxyUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询OB余额失败");
        }
    }

    @ApiOperation("查询用户OB体育余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshOBTY")
    public ResponseEntity refreshOBTY(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (CasinoProxyUtil.checkNull(third) || ObjectUtils.isEmpty(third.getObtyAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshOBTY(third.getUserId());
        if (CasinoProxyUtil.checkNull(jsonObject) || CasinoProxyUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("OB体育余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (CasinoProxyUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询OB体育余额失败");
        }
    }

    @ApiOperation("查询用户沙巴体育余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshSABA")
    public ResponseEntity refreshSABA(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (CasinoProxyUtil.checkNull(third) || ObjectUtils.isEmpty(third.getGoldenfAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshSABA(third.getUserId());
        if (CasinoProxyUtil.checkNull(jsonObject) || CasinoProxyUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("查询沙巴余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (CasinoProxyUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询沙巴余额失败");
        }
    }
    /**
     * 修改用户
     * 只有修改电话功能，那电话不能为空
     *
     * @param id
     * @param state
     * @param phone
     * @return
     */
    @ApiOperation("修改用户电话")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
            @ApiImplicitParam(name = "phone", value = "电话号码", required = true),

    })
    @PostMapping("updateUser")
    public ResponseEntity updateUser(Long id, Integer state, String phone){
        if(CasinoProxyUtil.checkNull(id,phone)){
            return ResponseUtil.custom("参数错误");
        }
        //查询用户信息
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        if (!phone.matches(RegexEnum.PHONE.getRegex())) {
            return ResponseUtil.custom("手机号格式错误");
        }
        user.setPhone(phone);
        userService.save(user);
        return ResponseUtil.success();
    }

//    @ApiOperation("删除用户")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//    })
//    @PostMapping("deteleUser")
//    public ResponseEntity deteleUser(Long id){
//        User user = userService.findById(id);
//        if(user == null){
//            return ResponseUtil.custom("账户不存在");
//        }
//        userService.deleteById(id);
//        return ResponseUtil.success();
//    }

    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("updateUserStatus")
    public ResponseEntity updateUserStatus(Long id){
        User user = userService.findById(id);
        if (CasinoProxyUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        //开启状态，冻结
        if(user.getState() == Constants.USER_NORMAL){
            user.setState(Constants.USER_LOCK_ACCOUNT);
        }else{
            user.setState(Constants.USER_NORMAL);
        }
        userService.save(user);
        return ResponseUtil.success();
    }

    @ApiOperation("重置用户提现密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("withdrawPassword")
    public ResponseEntity withdrawPassword(Long id){
        User user = userService.findById(id);
        if (CasinoProxyUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        log.info("重置提现密码之前密码:{}账号:{}",user.getWithdrawPassword(), user.getAccount());
        //随机生成
        String withdrawPassword = PasswordUtil.getRandomPwd();
        String bcryptPassword = CasinoProxyUtil.bcrypt(withdrawPassword);
        log.info("生成密码{}",bcryptPassword);
        user.setWithdrawPassword(bcryptPassword);
        user = userService.save(user);
        log.info("重置密码之后密码{}",user.getWithdrawPassword());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("withdrawPassword", withdrawPassword);
        return ResponseUtil.success(jsonObject);
    }

    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("resetPassword")
    public ResponseEntity resetPassword(Long id){
        User user = userService.findById(id);
        if (CasinoProxyUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        log.info("重置密码之前密码:{}账号:{}",user.getPassword(), user.getAccount());
        //随机生成
        String password = UserPasswordUtil.getRandomPwd();
        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
        log.info("生成密码{}",bcryptPassword);
        user.setPassword(bcryptPassword);
        user = userService.save(user);
        log.info("重置密码之后密码{}",user.getPassword());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }

//    /**
//     * 后台新增充值订单
//     *
//     * @param id 会员id
//     * @param remitter 汇款人姓名
//     * @param chargeAmount 汇款金额
//     * @param remark 汇款备注
//     * @return
//     */
//    @ApiOperation("后台新增充值订单 上分")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "会员id", required = true),
//            @ApiImplicitParam(name = "remitter", value = "汇款人姓名", required = false),
//            @ApiImplicitParam(name = "chargeAmount", value = "汇款金额", required = true),
//            @ApiImplicitParam(name = "remark", value = "汇款备注", required = false),
//    })
//    @PostMapping("/saveChargeOrder")
//    public ResponseEntity saveChargeOrder(Long id,String remitter,String remark, String chargeAmount){
//        if (CasinoProxyUtil.checkNull(id,chargeAmount)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        BigDecimal money = CommonUtil.checkMoney(chargeAmount);
//        if(money.compareTo(BigDecimal.ZERO)<CommonConst.NUMBER_1){
//            return ResponseUtil.custom("金额类型错误");
//        }
//        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_100)) >= CommonConst.NUMBER_1){
//            return ResponseUtil.custom("测试环境加钱不能超过100RMB");
//        }
//        User user = userService.findById(id);
//        if (CasinoProxyUtil.checkNull(user)){
//            return ResponseUtil.custom("账户不存在");
//        }
//        Long authId = CasinoProxyUtil.getAuthId();
//        ProxyUser byId = proxyUserService.findById(authId);
//        String lastModifier = (byId == null || byId.getUserName() == null)? "" : byId.getUserName();
//        ChargeOrder chargeOrder = new ChargeOrder();
//        chargeOrder.setUserId(id);
//        chargeOrder.setRemitter(remitter);
//        chargeOrder.setRemark(remark);
//        chargeOrder.setOrderNo(orderService.getOrderNo());
//        chargeOrder.setChargeAmount(money);
//        chargeOrder.setLastModifier(lastModifier);
//        chargeOrder.setType(user.getType());
////        chargeOrder.setRealityAmount(money);
//        return chargeOrderBusiness.saveOrderSuccess(user,chargeOrder,Constants.chargeOrder_proxy,Constants.remitType_proxy);
//    }
//    /**
//     * 后台新增提现订单
//     *
//     * @param id 会员id
//     * @param withdrawMoney 提现金额
//     * @param bankId 银行id
//     * @return
//     */
//    @ApiOperation("后台新增提现订单 下分")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//            @ApiImplicitParam(name = "withdrawMoney", value = "提现金额", required = true),
//            @ApiImplicitParam(name = "bankId", value = "银行id", required = false),
//            @ApiImplicitParam(name = "remark", value = "备注", required = false),
//    })
//    @PostMapping("/saveWithdrawOrder")
//    public ResponseEntity saveWithdrawOrder(Long id,String withdrawMoney,String bankId,String remark){
//        if (CasinoProxyUtil.checkNull(id,withdrawMoney)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        BigDecimal money = CommonUtil.checkMoney(withdrawMoney);
//        if(money.compareTo(BigDecimal.ZERO)<CommonConst.NUMBER_1){
//            return ResponseUtil.custom("金额类型错误");
//        }
//        User user = userService.findById(id);
//        if (CasinoProxyUtil.checkNull(user)){
//            return ResponseUtil.custom("找不到这个会员");
//        }
//        Long authId = CasinoProxyUtil.getAuthId();
//        ProxyUser byId = proxyUserService.findById(authId);
//        String lastModifier = (byId == null || byId.getUserName() == null)? "" : byId.getUserName();
//        return withdrawBusiness.updateWithdrawAndUser(user,id,money,bankId,Constants.withdrawOrder_proxy,lastModifier,remark);
//    }
//    @ApiOperation("后台下分检验可提款金额")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//            @ApiImplicitParam(name = "withdrawMoney", value = "提现金额", required = true),
//    })
//    @GetMapping("/checkoutWithdrawMoney")
//    public ResponseEntity checkoutWithdrawMoney(Long id,String withdrawMoney) {
//        if (CasinoProxyUtil.checkNull(id,withdrawMoney)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        BigDecimal money = CommonUtil.checkMoney(withdrawMoney);
//        if(money.compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_1){
//            return ResponseUtil.custom("金额类型错误");
//        }
//        UserMoney byUserId = userMoneyService.findByUserId(id);
//        if (CasinoProxyUtil.checkNull(byUserId)){
//            return ResponseUtil.custom("用户钱包不存在");
//        }
//        BigDecimal drawMoney = byUserId.getWithdrawMoney();//得到可提现金额
//        if(drawMoney.compareTo(money) < CommonConst.NUMBER_0){
//            return ResponseUtil.success(false);
//        }
//        return ResponseUtil.success(true);
//    }

    /**
     * 后台配置会员收款卡修改
     *
     * @param id 会员id
     * @param creditCard 收款卡张数
     * @param cardLevel 配置收款卡等级
     * @return
     */
//    @ApiOperation("后台配置会员收款卡修改")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//            @ApiImplicitParam(name = "creditCard", value = "收款卡张数", required = true),
//            @ApiImplicitParam(name = "cardLevel", value = "配置收款卡等级", required = false),
//    })
//    @PostMapping("/saveCollectionBankcard")
//    public ResponseEntity saveCollectionBankcard(Long id,Integer creditCard,String cardLevel){
//        if (CasinoProxyUtil.checkNull(id,creditCard)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        if (creditCard != 1 && creditCard != 2){
//            return ResponseUtil.custom("收款卡张数最多两张");
//        }
//        User user = userService.findById(id);
//        if (CasinoProxyUtil.checkNull(user)){
//            return ResponseUtil.custom("账户不存在");
//        }
//        user.setCreditCard(creditCard);
//        if (!CasinoProxyUtil.checkNull(cardLevel)){
//            String[] split = cardLevel.split(CommonConst.HYPHEN);
//            if (!CommonConst.cardLevel.containsAll(Arrays.asList(split))){
//                return ResponseUtil.custom("收款卡等级不合法");
//            }
//        }
//        user.setCardLevel(cardLevel);
//        userService.save(user);
//        return ResponseUtil.success();
//    }

    /**
     * 根据id查询用户登录注册ip
     *
     * @param id 会员id
     * @param pageSize 每页大小
     * @param pageCode 当前页
     * @return
     */
    @ApiOperation("根据id查询用户登录注册ip")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @GetMapping("/findIp")
    public ResponseEntity<LoginLog> findIp(Integer pageSize, Integer pageCode,Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(id);
        Page<LoginLog> loginLogPage = loginLogService.findLoginLogPage(loginLog, pageable);
        return ResponseUtil.success(loginLogPage);
    }
    /**
     * 根据id查询推广数据
     *会员列表详情
     * @param id 会员id
     * @return
     */
    @ApiOperation("根据id查询推广数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @GetMapping("/findProxyReport")
    public ResponseEntity<ProxyReport> findProxyReport(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyReport proxyReport = proxyReportService.findByUserId(id);
        return ResponseUtil.success(proxyReport);
    }
        /**
     * 根据id查询代理归属
     *会员列表详情
     * @param id 会员id
     * @return
     */
    @ApiOperation("根据id查询代理归属")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @GetMapping("/findAgentOfBelonging")
    public ResponseEntity findAgentOfBelonging(Long id){
        if (CasinoProxyUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser byId = proxyUserService.findById(CasinoProxyUtil.getAuthId());
        User user = userService.findById(id);
        if (CasinoProxyUtil.checkNull(user)){
            return ResponseUtil.success("");
        }
        String agentOf = "";
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            ProxyUser secondProxy = proxyUserService.findById(user.getSecondProxy());
            ProxyUser thirdProxy = proxyUserService.findById(user.getThirdProxy());
            agentOf = MessageFormat.format(agentOfBelonging,secondProxy == null ? "":secondProxy.getUserName(),thirdProxy == null ? "":thirdProxy.getUserName() );
            return ResponseUtil.success(agentOf);
        }else if (byId.getProxyRole() == CommonConst.NUMBER_2){
            ProxyUser thirdProxy = proxyUserService.findById(user.getThirdProxy());
            agentOf = thirdProxy == null ? "":thirdProxy.getUserName();
            return ResponseUtil.success(agentOf);
        }
        return ResponseUtil.success(agentOf);
    }
//    /**
//     * 根据id查询上下三级代理线
//     *会员列表详情
//     * @param id 会员id
//     * @return
//     */
//    @ApiOperation("根据id查询上下三级代理线")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "用户id", required = true),
//    })
//    @GetMapping("/findAgency")
//    public ResponseEntity findAgency(Long id){
//        if (CasinoProxyUtil.checkNull(id)){
//            return ResponseUtil.custom("参数不合法");
//        }
//        User user = userService.findById(id);
//        if (CasinoProxyUtil.checkNull(user)){
//            return ResponseUtil.success("");
//        }
//        String agency = user.getAccount()+"(当前)";
//        User first = userService.findById(user.getFirstPid() == null ? 0L:user.getFirstPid());
//        if (CasinoProxyUtil.checkNull(first)){
//            return ResponseUtil.success(agency);
//        }
//        agency = first.getAccount() + " — "  + agency;
//        User second = userService.findById(user.getSecondPid() == null ? 0L:user.getSecondPid());
//        if (CasinoProxyUtil.checkNull(second)){
//            return ResponseUtil.success(agency);
//        }
//        agency = second.getAccount() + " — "  + agency;
//        User third = userService.findById(user.getThirdPid() == null ? 0L:user.getThirdPid());
//        if (CasinoProxyUtil.checkNull(third)){
//            return ResponseUtil.success(agency);
//        }
//        agency = third.getAccount() + " — "  + agency;
//        return ResponseUtil.success(agency);
//    }
}

package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.GenerateInviteCodeRunner;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.casinoweb.vo.UserInfoVo;
import com.qianyi.casinoweb.vo.UserMoneyVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.UUID;

@RestController
@RequestMapping("user")
@Api(tags = "用户中心")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    @Qualifier("loginLogJob")
    AsyncService asyncService;
    @Autowired
    GenerateInviteCodeRunner generateInviteCodeRunner;

    @GetMapping("info")
    @ApiOperation("获取当前用户的基本信息(不包含 余额，打码量，可提现金额,洗码金额)")
    public ResponseEntity<UserInfoVo> info() {
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        User firstParent = null;
        if (user.getFirstPid() != null) {
            firstParent = userService.findById(user.getFirstPid());
        }
        if (firstParent != null) {
            vo.setSuperiorAccount(firstParent.getAccount());
        }
        //查询后台配置域名
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig != null) {
            vo.setDomain(platformConfig.getDomainNameConfiguration());
        }
        return new ResponseEntity(ResponseCode.SUCCESS, vo);
    }

    @GetMapping("getMoney")
    @ApiOperation("获取当前用户的余额,打码量,可提现金额,洗码金额,分润金额")
    public ResponseEntity<UserMoneyVo> getMoney() {
        Long userId = CasinoWebUtil.getAuthId();
        UserMoneyVo vo = new UserMoneyVo();
        //TODO 查询可提金额，未完成流水(打码量)
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        if (userMoney == null) {
            return new ResponseEntity(ResponseCode.SUCCESS, vo);
        }
        vo.setMoney(userMoney.getMoney());
        vo.setDrawMoney(userMoney.getWithdrawMoney());
        vo.setUnfinshTurnover(userMoney.getCodeNum());
        vo.setWashCode(userMoney.getWashCode());
        vo.setShareProfit(userMoney.getShareProfit());
        return new ResponseEntity(ResponseCode.SUCCESS, vo);
    }

    @PostMapping("/updateUserInfo")
    @ApiOperation("登录用户修改信息")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "参数类型:0=真实姓名，1=邮箱地址，2=微信账号，3=QQ账号", required = true),
            @ApiImplicitParam(name = "value", required = true),
    })
    public ResponseEntity updateUserInfo(String type, String value) {
        if (ObjectUtils.isEmpty(type)) {
            return ResponseUtil.parameterNotNull();
        }
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        String remark="该信息不可修改";
        if ("0".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getRealName())){
                return ResponseUtil.custom(remark);
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("真实姓名不允许为空");
            }
            user.setRealName(value);
        } else if ("1".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getEmail())){
                return ResponseUtil.custom(remark);
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("邮箱不允许为空");
            }
            if (!value.matches(RegexEnum.EMAIL.getRegex())) {
                return ResponseUtil.custom("邮箱格式填写错误");
            }
            user.setEmail(value);
        } else if ("2".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getWebChat())){
                return ResponseUtil.custom(remark);
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("微信号不允许为空");
            }
            if (!value.matches(RegexEnum.WEBCHAT.getRegex())) {
                return ResponseUtil.custom("微信号"+RegexEnum.WEBCHAT.getDesc());
            }
            user.setWebChat(value);
        } else if ("3".equals(type)) {
            if(!ObjectUtils.isEmpty(user.getQq())){
                return ResponseUtil.custom(remark);
            }
            if(ObjectUtils.isEmpty(value)){
                return ResponseUtil.custom("QQ号不允许为空");
            }
            if (!value.matches(RegexEnum.QQ.getRegex())) {
                return ResponseUtil.custom("QQ号"+RegexEnum.QQ.getDesc());
            }
            user.setQq(value);
        } else{
            return ResponseUtil.custom("修改失败");
        }
        userService.save(user);
        return ResponseUtil.success();
    }


    @PostMapping("/webUpdateUserInfo")
    @ApiOperation("web端登录用户修改信息")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱"),
            @ApiImplicitParam(name = "webChat", value = "微信"),
            @ApiImplicitParam(name = "qq", value = "QQ"),
            @ApiImplicitParam(name = "phone", value = "手机号"),
    })
    public ResponseEntity webUpdateUserInfo(String email, String webChat, String qq, String phone) {
        if(ObjectUtils.isEmpty(email)&&ObjectUtils.isEmpty(webChat)&&ObjectUtils.isEmpty(qq)&&ObjectUtils.isEmpty(phone)){
            return ResponseUtil.parameterNotNull();
        }
        if (!ObjectUtils.isEmpty(webChat) && !webChat.matches(RegexEnum.WEBCHAT.getRegex())) {
            return ResponseUtil.custom("微信号"+RegexEnum.WEBCHAT.getDesc());
        }
        if (!ObjectUtils.isEmpty(qq) && !qq.matches(RegexEnum.QQ.getRegex())) {
            return ResponseUtil.custom("QQ号"+RegexEnum.QQ.getDesc());
        }
        if (!ObjectUtils.isEmpty(phone) && !phone.matches(RegexEnum.PHONE.getRegex())) {
            return ResponseUtil.custom("手机号"+RegexEnum.PHONE.getDesc());
        }
        if (!ObjectUtils.isEmpty(email) && !email.matches(RegexEnum.EMAIL.getRegex())) {
            return ResponseUtil.custom("邮箱格式填写错误");
        }
        //这4个字段数据添加后不能再修改
        boolean flag = true;
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        if (ObjectUtils.isEmpty(user.getEmail())) {
            user.setEmail(email);
            flag = false;
        }
        if (ObjectUtils.isEmpty(user.getWebChat())) {
            user.setWebChat(webChat);
            flag = false;
        }
        if (ObjectUtils.isEmpty(user.getQq())) {
            user.setQq(qq);
            flag = false;
        }
        if (ObjectUtils.isEmpty(user.getPhone())) {
            user.setPhone(phone);
            flag = false;
        }
        if(flag){
            return ResponseUtil.custom("该信息不可修改");
        }
        userService.save(user);
        return ResponseUtil.success();
    }

    @GetMapping("everyoneSpread")
    @ApiOperation("获取当前用户人人代推广链接")
    public ResponseEntity<String> everyoneSpread() {
        //查询后台配置域名
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || ObjectUtils.isEmpty(platformConfig.getProxyConfiguration())) {
            return ResponseUtil.custom("推广域名未配置，请联系客服");
        }
        String domain = platformConfig.getProxyConfiguration();
        Long authId = CasinoWebUtil.getAuthId();
        User user = userService.findById(authId);
        if (ObjectUtils.isEmpty(user.getInviteCode())) {
            return ResponseUtil.custom("当前用户邀请码为空");
        }
        String url = domain + "/" + Constants.INVITE_TYPE_EVERYONE + "/" + user.getInviteCode();
        return ResponseUtil.success(url);
    }



    @PostMapping("directOpenAccount")
    @ApiOperation("直接开户")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "confirmPassword", value = "确认密码", required = true),
            @ApiImplicitParam(name = "country", value = "区号，柬埔寨：855", required = false),
            @ApiImplicitParam(name = "phone", value = "手机号", required = false),
    })
    public ResponseEntity directOpenAccount(String account, String password, String confirmPassword, String country, String phone, HttpServletRequest request) {
        //人人代开关检查
        ResponseEntity response = checkPeopleProxySwitch();
        if (response != null) {
            return response;
        }
        boolean checkNull = CommonUtil.checkNull(account, password, confirmPassword);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        //卫语句校验
        boolean checkAccountLength = User.checkAccountLength(account);
        if (!checkAccountLength) {
            return ResponseUtil.custom("用户名" + RegexEnum.ACCOUNT.getDesc());
        }
        boolean checkPasswordLength = User.checkPasswordLength(password);
        if (!checkPasswordLength) {
            return ResponseUtil.custom("密码" + RegexEnum.ACCOUNT.getDesc());
        }
        if (!password.equals(confirmPassword)) {
            return ResponseUtil.custom("两次密码输入不一致");
        }
        if (!ObjectUtils.isEmpty(country) && !ObjectUtils.isEmpty(phone)) {
            phone = country + phone;
        }
        if (!ObjectUtils.isEmpty(phone)) {
            boolean checkPhone = User.checkPhone(phone);
            if (!checkPhone) {
                return ResponseUtil.custom("手机号" + RegexEnum.PHONE.getDesc());
            }
        }
        Long userId = CasinoWebUtil.getAuthId();
        Integer count = userService.countByFirstPid(userId);
        PlatformConfig platformConfig = platformConfigService.findFirst();
        Integer underTheLower = 20;
        if (platformConfig != null) {
            underTheLower = platformConfig.getDirectlyUnderTheLower() == null ? 20 : platformConfig.getDirectlyUnderTheLower();
        }
        if (count >= underTheLower) {
            return ResponseUtil.custom("直推数量已达上限");
        }
        User user = userService.findByAccount(account);
        if (user != null && !CommonUtil.checkNull(user.getPassword())) {
            return ResponseUtil.custom("该帐号已存在");
        }
        //设置user基本参数
        String ip = IpUtil.getIp(request);
        String inviteCodeNew = generateInviteCodeRunner.getInviteCode();
        user = User.setBaseUser(account, CasinoWebUtil.bcrypt(password), phone, ip,inviteCodeNew);
        //设置父级
        User parentUser = userService.findById(userId);
        user.setFirstPid(userId);
        user.setSecondPid(parentUser.getFirstPid());
        user.setThirdPid(parentUser.getSecondPid());
        user.setType(Constants.USER_TYPE0);
        User save = userService.save(user);
        //userMoney表初始化数据
        UserMoney userMoney = new UserMoney();
        userMoney.setUserId(save.getId());
        userMoneyService.save(userMoney);
        //记录注册日志
        setLoginLog(ip, user, request);
        //推送MQ
        sendUserMq(save);
        return ResponseUtil.success();
    }

    /**
     * 记录注册日志
     * @param ip
     * @param user
     * @param request
     */
    public void setLoginLog(String ip,User user,HttpServletRequest request){
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        //检测请求设备
        String ua = request.getHeader("User-Agent");
        boolean checkMobileOrPc = DeviceUtil.checkAgentIsMobile(ua);
        if(checkMobileOrPc){
            vo.setRemark("Mobile");
        }else{
            vo.setRemark("PC");
        }
        vo.setType(2);
        asyncService.executeAsync(vo);
    }

    /**
     * 推送团队新增成员MQ
     * @param user
     */
    public void sendUserMq(User user){
        log.info("开始推送团队新增成员消息", user);
        rabbitTemplate.convertAndSend(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.ADDUSERTOTEAM_DIRECT, user, new CorrelationData(UUID.randomUUID().toString()));
        log.info("团队新增成员消息发送成功={}", user);
    }

    /**
     * 人人代开关检查
     * @return
     */
    public ResponseEntity checkPeopleProxySwitch() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        boolean proxySwitch = PlatformConfig.checkPeopleProxySwitch(platformConfig);
        if (!proxySwitch) {
            return ResponseUtil.custom("不支持此功能");
        }
        return null;
    }

    public static void main(String[] args) {
        String regex = "^[0-9]*[1-9][0-9]*$";
        String phone="123q";
        System.out.println(phone.matches(regex));
    }

}

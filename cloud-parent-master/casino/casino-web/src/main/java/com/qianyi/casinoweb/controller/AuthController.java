package com.qianyi.casinoweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.Producer;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.GenerateInviteCodeRunner;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.util.DeviceUtil;
import com.qianyi.casinoweb.util.InviteCodeUtil;
import com.qianyi.casinoweb.vo.LoginLogVo;
import com.qianyi.moduleauthenticator.WangyiDunAuthUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.*;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Api(tags = "????????????")
@RestController
@RequestMapping("auth")
@Slf4j
public class AuthController {

    //?????????captchaProducer??????KaptchaConfig?????????bean????????????
    @Autowired
    Producer captchaProducer;

    @Autowired
    UserService userService;
    @Autowired
    UserMoneyService userMoneyService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    IpBlackService ipBlackService;
    @Autowired
    ProxyUserService proxyUserService;
    @Autowired
    GenerateInviteCodeRunner generateInviteCodeRunner;
    @Autowired
    DomainConfigService domainConfigService;

    @Autowired
    @Qualifier("loginLogJob")
    AsyncService asyncService;
    @Value("${project.smsUrl}")
    private String smsUrl;
    @Value("${project.merchant}")
    private String merchant;

    @PostMapping("spreadRegister")
    @ApiOperation("??????????????????")
    @NoAuthentication
    //1??????3???
//    @RequestLimit(limit = 3, timeout = 60)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "??????", required = true),
            @ApiImplicitParam(name = "password", value = "??????", required = true),
            @ApiImplicitParam(name = "country", value = "?????????????????????855", required = true),
            @ApiImplicitParam(name = "phone", value = "?????????", required = true),
            @ApiImplicitParam(name = "phoneCode", value = "??????????????????,??????????????????????????????", required = false),
            @ApiImplicitParam(name = "validate", value = "????????????", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "?????????", required = false),
            @ApiImplicitParam(name = "inviteType", value = "????????????:everyone:????????????proxy:???????????????888:????????????", required = false),
    })
    public ResponseEntity spreadRegister(String account, String password, String country, String phone, String phoneCode, HttpServletRequest request, String validate, String inviteCode, String inviteType) {
        if (checkRequiredParams(account, password, country, phone, phoneCode, validate)) {
            return ResponseUtil.parameterNotNull();
        }
        //???????????????????????????
        ResponseEntity checkResponse = ResponseUtil.success();
        if (ObjectUtils.isEmpty(inviteCode) && ObjectUtils.isEmpty(inviteType)) {
            checkResponse = checkRegisterDomainName(request);
        } else {
            checkResponse = checkInviteCode(inviteType, inviteCode);
        }
        if (checkResponse.getCode() != 0) {
            return checkResponse;
        }
        ResponseEntity responseEntity = registerCommon(account, password,country, phone,phoneCode, request, validate, inviteCode, inviteType,0);
        return responseEntity;
    }

    @PostMapping("register")
    @ApiOperation("??????????????????")
    @NoAuthentication
    //1??????3???
//    @RequestLimit(limit = 3, timeout = 60)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "??????", required = true),
            @ApiImplicitParam(name = "password", value = "??????", required = true),
            @ApiImplicitParam(name = "country", value = "?????????????????????855", required = true),
            @ApiImplicitParam(name = "phone", value = "????????????", required = true),
            @ApiImplicitParam(name = "phoneCode", value = "??????????????????", required = false),
            @ApiImplicitParam(name = "validate", value = "????????????", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "?????????", required = false),
    })
    public ResponseEntity register(String account, String password, String country, String phone, String phoneCode,
                                   HttpServletRequest request, String validate, String inviteCode) {
        if (checkRequiredParams(account, password, country, phone, phoneCode, validate)) {
            return ResponseUtil.parameterNotNull();
        }
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || platformConfig.getRegisterSwitch() == null || platformConfig.getRegisterSwitch() == Constants.close) {
            return ResponseUtil.custom("?????????????????????");
        }
        ResponseEntity responseEntity = registerCommon(account, password, country, phone, phoneCode, request, validate, inviteCode, null,1);
        return responseEntity;
    }

    public boolean checkRequiredParams(String account, String password, String country, String phone, String phoneCode,String validate){
        boolean checkVerificationSwitch = checkVerificationSwitch();
        boolean checkNull = false;
        if (checkVerificationSwitch) {
            checkNull = CommonUtil.checkNull(account, password, country, phone, phoneCode, validate);
        } else {
            checkNull = CommonUtil.checkNull(account, password, country, phone, validate);
        }
        return checkNull;
    }

    /**
     * ??????????????????
     * @param account
     * @param password
     * @param phone
     * @param request
     * @param validate
     * @param inviteCode
     * @param inviteType
     * @return
     */
    public ResponseEntity registerCommon(String account, String password, String country, String phone, String phoneCode,
                                         HttpServletRequest request, String validate, String inviteCode, String inviteType,Integer source) {
        boolean wangyidun = WangyiDunAuthUtil.verify(validate);
        if (!wangyidun) {
            return ResponseUtil.custom("???????????????");
        }
        //???????????????
        boolean checkAccountLength = User.checkAccountLength(account);
        if (!checkAccountLength) {
            return ResponseUtil.custom("?????????" + RegexEnum.ACCOUNT.getDesc());
        }
        boolean checkPasswordLength = User.checkPasswordLength(password);
        if (!checkPasswordLength) {
            return ResponseUtil.custom("??????" + RegexEnum.ACCOUNT.getDesc());
        }
        boolean checkPhone = User.checkPhone(phone);
        if (!checkPhone) {
            return ResponseUtil.custom("?????????" + RegexEnum.PHONE.getDesc());
        }
        phone = country + phone;
        boolean checkVerificationSwitch = checkVerificationSwitch();
        if (checkVerificationSwitch) {
            String redisKey = Constants.REDIS_SMSCODE + phone;
            Object redisCode = redisUtil.get(redisKey);
            if (!phoneCode.equals(redisCode)) {
                return ResponseUtil.custom("????????????????????????");
            }
        }
        //???????????????????????????????????????
        List<User> phoneUser = userService.findByPhone(phone);
        if (!CollectionUtils.isEmpty(phoneUser)) {
            return ResponseUtil.custom("????????????????????????");
        }
        String ip = IpUtil.getIp(request);
        //??????ip??????????????????
        PlatformConfig platformConfig = platformConfigService.findFirst();
        Integer timeLimit = 5;
        if (platformConfig != null) {
            timeLimit = platformConfig.getIpMaxNum() == null ? 5 : platformConfig.getIpMaxNum();
        }
        Integer count = userService.countByIp(ip);
        if (count != null && count > timeLimit) {
            return ResponseUtil.custom("??????IP??????????????????????????????");
        }

        List<User> userList = userService.findByAccountUpper (account);
        if (!CollectionUtils.isEmpty(userList)) {
            return ResponseUtil.custom("??????????????????");
        }
        //??????user????????????
        String inviteCodeNew = generateInviteCodeRunner.getInviteCode();
        User user = User.setBaseUser(account, CasinoWebUtil.bcrypt(password), phone, ip,inviteCodeNew);
        //????????????
        setParent(inviteCode, inviteType, user,source);
        Long firstPid = user.getFirstPid();
        //??????????????????
        if (firstPid != null && firstPid != 0) {
            Integer firstCount = userService.countByFirstPid(firstPid);
            Integer underTheLower = 20;
            if (platformConfig != null) {
                underTheLower = platformConfig.getDirectlyUnderTheLower() == null ? 20 : platformConfig.getDirectlyUnderTheLower();
            }
            if (firstCount >= underTheLower) {
                return ResponseUtil.custom("????????????????????????");
            }
        }
        //???????????????????????????
        String origin = request.getHeader("origin");
        user.setRegisterDomainName(origin);

        User save = userService.save(user);
        log.info("user????????????????????????user={}",save.toString());
        //userMoney??????????????????
        UserMoney userMoney = new UserMoney();
        userMoney.setUserId(save.getId());
        userMoneyService.save(userMoney);
        log.info("userMoney????????????????????????userMoney={}",userMoney.toString());
        //??????????????????
        setLoginLog(ip, user, request);
        //??????MQ
        sendUserMq(save);
        return ResponseUtil.success();
    }

    /**
     * ??????????????????
     * @param ip
     * @param user
     * @param request
     */
    public void setLoginLog(String ip,User user,HttpServletRequest request){
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        //??????????????????
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
     * ????????????????????????MQ
     * @param user
     */
    public void sendUserMq(User user){
        log.info("????????????????????????????????????", user);
        rabbitTemplate.convertAndSend(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.ADDUSERTOTEAM_DIRECT, user, new CorrelationData(UUID.randomUUID().toString()));
        log.info("????????????????????????????????????={}", user);
    }

    /**
     * ??????????????????????????????
     * @param inviteCode
     * @param inviteType
     * @param user
     */
    public void setParent(String inviteCode, String inviteType, User user, Integer source) {
        //??????
        if (source == 0) {
            //?????????
            if (Constants.INVITE_TYPE_EVERYONE.equals(inviteType)) {
                User parentUser = userService.findByInviteCode(inviteCode);
                user.setFirstPid(parentUser.getId());
                user.setSecondPid(parentUser.getFirstPid());
                user.setThirdPid(parentUser.getSecondPid());
                user.setType(Constants.USER_TYPE0);
                return;
            }
            //????????????
            if (Constants.INVITE_TYPE_PROXY.equals(inviteType)) {
                ProxyUser parentProxy = proxyUserService.findByProxyCode(inviteCode);
                user.setFirstProxy(parentProxy.getFirstProxy());
                user.setSecondProxy(parentProxy.getSecondProxy());
                user.setThirdProxy(parentProxy.getId());
                user.setType(Constants.USER_TYPE1);
                return;
            }
            //????????????
            if (Constants.INVITE_TYPE_COMPANY.equals(inviteType)) {
                user.setType(Constants.USER_TYPE2);
                return;
            }
            //??????????????????
            if (ObjectUtils.isEmpty(inviteCode) && ObjectUtils.isEmpty(inviteType)) {
                user.setType(Constants.USER_TYPE2);
                return;
            }
            //??????????????????
        } else if (source == 1) {
            user.setType(Constants.USER_TYPE0);
            user.setFirstPid(0L);//??????????????????
            User parentUser = null;
            if (!ObjectUtils.isEmpty(inviteCode)) {
                parentUser = userService.findByInviteCode(inviteCode);
            }
            if (parentUser != null) {
                user.setFirstPid(parentUser.getId());
                user.setSecondPid(parentUser.getFirstPid());
                user.setThirdPid(parentUser.getSecondPid());
            }
        }
    }

    @NoAuthentication
    @ApiOperation("????????????.???????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "??????", required = true),
            @ApiImplicitParam(name = "password", value = "??????", required = true),
            @ApiImplicitParam(name = "validate", value = "????????????", required = true),
            @ApiImplicitParam(name = "deviceId", value = "??????ID,???????????????", required = false),
    })
    @PostMapping("loginA")
    public ResponseEntity loginA(
            String account,
            String password,
            String validate,String deviceId,HttpServletRequest request) {
        if (CasinoWebUtil.checkNull(account, password, validate)) {
            return ResponseUtil.parameterNotNull();
        }

//        //???????????????
//        boolean captcha = CasinoWebUtil.checkCaptcha(captchaCode, captchaText);
//        if (!captcha) {
//            return ResponseUtil.custom("???????????????");
//        }

        User user = userService.findByAccount(account);
        if (user == null) {
            return ResponseUtil.custom("?????????????????????");
        }
        if(!User.checkUser(user)){
            return ResponseUtil.custom("????????????");
        }
        String bcryptPassword = user.getPassword();
        boolean bcrypt = CasinoWebUtil.checkBcrypt(password, bcryptPassword);
//        log.info("????????????{},??????????????????{},???????????????{},????????????{}", account, password, bcryptPassword, bcrypt);
        if (!bcrypt) {
            return ResponseUtil.custom("?????????????????????");
        }
        //????????????????????????
        boolean verifyFlag = false;
        if (!ObjectUtils.isEmpty(user.getDeviceId()) && !ObjectUtils.isEmpty(deviceId) && user.getDeviceId().equals(deviceId)) {
            verifyFlag = true;
        }
        if (!verifyFlag) {
            //???????????????
            boolean wangyidun = WangyiDunAuthUtil.verify(validate);
            if (!wangyidun) {
                return ResponseUtil.custom("???????????????");
            }
        }
        if (ObjectUtils.isEmpty(user.getDeviceId()) && !ObjectUtils.isEmpty(deviceId)) {
            user.setDeviceId(deviceId);
            userService.save(user);
        }
        //??????????????????
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        LoginLogVo vo = new LoginLogVo();
        vo.setIp(ip);
        vo.setAccount(user.getAccount());
        vo.setUserId(user.getId());
        //??????????????????
        String ua = request.getHeader("User-Agent");
        boolean checkMobileOrPc = DeviceUtil.checkAgentIsMobile(ua);
        if(checkMobileOrPc){
            vo.setRemark("Mobile");
        }else{
            vo.setRemark("PC");
        }
        vo.setType(1);
        asyncService.executeAsync(vo);

        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(String.valueOf(user.getId()));
        subject.setBcryptPassword(user.getPassword());
        String token = JjwtUtil.generic(subject,Constants.CASINO_WEB);
        setUserTokenToRedis(user.getId(), token);
        return ResponseUtil.success(token);
    }

//    @NoAuthentication
//    @ApiOperation("????????????.?????????????????????")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "account", value = "??????", required = true),
//            @ApiImplicitParam(name = "password", value = "??????", required = true),
//            @ApiImplicitParam(name = "code", value = "?????????", required = true),
//    })
//    @PostMapping("loginB")
//    public ResponseEntity loginB(String account, String password, Integer code) {
//        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(code)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        boolean length = User.checkLength(account, password);
//        if (!length) {
//            return ResponseUtil.custom("??????,????????????3-15???");
//        }
//
//        User user = userService.findByAccount(account);
//        if (user == null) {
//            return ResponseUtil.custom("?????????????????????");
//        }
//
//        String bcryptPassword = user.getPassword();
//        boolean bcrypt = PayUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("?????????????????????");
//        }
//
//        boolean flag = User.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("?????????????????????");
//        }
//
//        String secret = user.getSecret();
//        if (PayUtil.checkNull(secret)) {
//            return ResponseUtil.custom("?????????????????????????????????");
//        }
//        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
//        if (!checkCode) {
//            return ResponseUtil.googleAuthNoPass();
//        }
//
//        String token = JjwtUtil.generic(user.getId() + "");
//
//        //??????????????????
//        String ip = IpUtil.getIp(PayUtil.getRequest());
//        new Thread(new LoginLogJob(ip, user.getAccount(), user.getId(), "admin")).start();
//
//        return ResponseUtil.success(token);
//    }


    @ApiOperation("?????????????????????")
    @ApiImplicitParam(name = "code", value = "code????????????????????????????????????????????????????????????", required = true)
    @GetMapping("captcha")
    @NoAuthentication
    public void captcha(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (CommonUtil.checkNull(code)) {
            return;
        }
        //????????????????????????????????????session???
        String createText = captchaProducer.createText();

        String key = CasinoWebUtil.getCaptchaKey(request, code);
        ExpiringMapUtil.putMap(key, createText);

        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        ServletOutputStream responseOutputStream = response.getOutputStream();

        //?????????????????????????????????????????????BufferedImage???????????????byte?????????byte?????????
        BufferedImage challenge = captchaProducer.createImage(createText);
        ImageIO.write(challenge, "jpg", jpegOutputStream);
        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        //??????response???????????????image/jpeg???????????????response????????????????????????byte??????
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
    }
//
//    @GetMapping("google/auth/bind")
//    @NoAuthentication
//    @ApiOperation("???????????????????????????")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "account", value = "??????", required = true),
//            @ApiImplicitParam(name = "password", value = "??????", required = true)
//    })
//    @ApiResponses({
//            @ApiResponse(code = 0, message = "?????????????????????")
//    })
//    public ResponseEntity bindGoogleAuth(String account, String password) {
//        if (PayUtil.checkNull(account) || PayUtil.checkNull(password)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        User user = userService.findByAccount(account);
//        if (user == null) {
//            return ResponseUtil.custom("?????????????????????");
//        }
//
//        String bcryptPassword = user.getPassword();
//        boolean bcrypt = PayUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("?????????????????????");
//        }
//
//        boolean flag = User.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("?????????????????????");
//        }
//
//        String secret = user.getSecret();
//
//        if (PayUtil.checkNull(secret)) {
//            secret = GoogleAuthUtil.generateSecretKey();
//            userService.setSecretById(user.getId(), secret);
//        }
//
//        String qrcode = GoogleAuthUtil.getQcode(account, secret);
//        return ResponseUtil.success(qrcode);
//
//    }

    @GetMapping("getJwtToken")
    @ApiOperation("???????????????????????????????????????????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "?????????", required = true),
    })
    @NoAuthentication
    public ResponseEntity<String> getJwtToken(String account) {
        User user = userService.findByAccount(account);
        if (user == null) {
            return ResponseUtil.custom("???????????????");
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(String.valueOf(user.getId()));
        subject.setBcryptPassword(user.getPassword());
        String jwt = JjwtUtil.generic(subject,Constants.CASINO_WEB);
        setUserTokenToRedis(user.getId(), jwt);
        return ResponseUtil.success(jwt);
    }

    @GetMapping("getRegisterStatus")
    @ApiOperation("????????????????????????")
    @NoAuthentication
    public ResponseEntity<Integer> getRegisterStatus() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null || platformConfig.getRegisterSwitch() == null) {
            return ResponseUtil.success(Constants.close);
        }
        return ResponseUtil.success(platformConfig.getRegisterSwitch());
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     * @param request
     * @return
     */
    @GetMapping("checkRegisterDomainName")
    @ApiOperation("???????????????????????????????????????")
    @NoAuthentication
    public ResponseEntity checkRegisterDomainName(HttpServletRequest request) {
        //???????????????????????????
        String origin = request.getHeader("origin");
        DomainConfig domainConfig = domainConfigService.findByDomainUrlAndDomainStatus(origin, Constants.open);
        if (domainConfig == null) {
            return ResponseUtil.custom("????????????");
        }
        return ResponseUtil.success();
    }

    @GetMapping("checkInviteCode")
    @ApiOperation("???????????????")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "inviteType", value = "????????????:everyone:????????????proxy:????????????,888:????????????", required = true),
            @ApiImplicitParam(name = "inviteCode", value = "?????????", required = true),
    })
    public ResponseEntity checkInviteCode(String inviteType, String inviteCode) {
        boolean checkNull = CommonUtil.checkNull(inviteType, inviteCode);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        if (!Constants.INVITE_TYPE_EVERYONE.equals(inviteType) && !Constants.INVITE_TYPE_PROXY.equals(inviteType) && !Constants.INVITE_TYPE_COMPANY.equals(inviteType)) {
            return ResponseUtil.custom("?????????????????????");
        }
        if (Constants.INVITE_TYPE_EVERYONE.equals(inviteType)) {
            User user = userService.findByInviteCode(inviteCode);
            if (user != null) {
                return ResponseUtil.success();
            }
            IpBlack ipBlack = new IpBlack(IpUtil.getIp(CasinoWebUtil.getRequest()), Constants.no, "?????????????????????????????????IP??????");
            ipBlackService.save(ipBlack);
            return ResponseUtil.custom(Constants.IP_BLOCK);
        }
        if (Constants.INVITE_TYPE_PROXY.equals(inviteType)) {
            ProxyUser proxyUser = proxyUserService.findByProxyCode(inviteCode);
            if (proxyUser != null) {
                return ResponseUtil.success();
            }
            IpBlack ipBlack = new IpBlack(IpUtil.getIp(CasinoWebUtil.getRequest()), Constants.no, "????????????????????????????????????IP??????");
            ipBlackService.save(ipBlack);
            return ResponseUtil.custom(Constants.IP_BLOCK);
        }
        if (Constants.INVITE_TYPE_COMPANY.equals(inviteType)) {
            PlatformConfig platformConfig = platformConfigService.findFirst();
            if (platformConfig != null && !inviteCode.equals(platformConfig.getCompanyInviteCode())) {
                return ResponseUtil.custom("?????????????????????????????????");
            }
        }
        return ResponseUtil.success();
    }

    @GetMapping("getVerificationCode")
    @ApiOperation("??????????????????????????????")
    @NoAuthentication
    @RequestLimit(limit = 1, timeout = 50)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "country", value = "??????????????????86???????????????855??????????????????60????????????66", required = true),
            @ApiImplicitParam(name = "phone", value = "?????????", required = true)
    })
    public ResponseEntity getVerificationCode(String country, String phone) {
        boolean checkNull = CommonUtil.checkNull(country, phone);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        String regex = "^[0-9]*[1-9][0-9]*$";
        if (!country.matches(regex) || !phone.matches(regex)) {
            return ResponseUtil.custom("??????????????????????????????");
        }
        //??????ip???????????????????????????10???
        String today = DateUtil.dateToyyyyMMdd(new Date());
        String ip = IpUtil.getIp(CasinoWebUtil.getRequest());
        String todayIpKey = Constants.REDIS_SMSIPSENDNUM + today + "::" + ip;
        Object todayIpNum = redisUtil.get(todayIpKey);
        if (todayIpNum != null && (int) todayIpNum >= 10) {
            return ResponseUtil.custom("??????IP???????????????????????????????????????");
        }
        String phoneKey = Constants.REDIS_SMSCODE + country + phone;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("merchant", merchant);
        paramMap.put("country", country);
        paramMap.put("phone", phone);
        Integer language = 1;
        if ("855".equals(country)) {
            language = 3;
        } else if ("60".equals(country)) {
            language = 4;
        } else if ("66".equals(country)) {
            language = 5;
        }
        paramMap.put("language", language);
        String code = InviteCodeUtil.randomNumCode(6);
        paramMap.put("code", code);
        String response = HttpClient4Util.doPost(smsUrl + "/buka/sendRegister", paramMap);
        if (CommonUtil.checkNull(response)) {
            return ResponseUtil.custom("?????????????????????,???????????????");
        }
        ResponseEntity responseEntity = JSONObject.parseObject(response, ResponseEntity.class);
        if (responseEntity.getCode() != ResponseCode.SUCCESS.getCode()) {
            log.error("merchant:{},country:{},phone:{},msg???{}", merchant, country, phone, responseEntity.getMsg());
            return ResponseUtil.custom("?????????????????????,???????????????");
        }
        //?????????????????????5??????
        redisUtil.set(phoneKey, code, 60 * 5);
        //ip?????????????????????????????????1
        if (todayIpNum == null) {
            redisUtil.set(todayIpKey, 1, 60 * 60 * 24);
        } else {
            redisUtil.incr(todayIpKey, 1);
        }
        return ResponseUtil.success();
    }

    @GetMapping("serviceHealthCheck")
    @ApiOperation("????????????????????????")
    @NoAuthentication
    public ResponseEntity serverHealthCheck() {
        return ResponseUtil.success();
    }


    @GetMapping("checkPhoneIsRegister")
    @ApiOperation("?????????????????????????????????")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "country", value = "?????????????????????855", required = true),
            @ApiImplicitParam(name = "phone", value = "?????????", required = true)
    })
    public ResponseEntity checkPhoneIsRegister(String country, String phone) {
        boolean checkNull = CommonUtil.checkNull(country, phone);
        if (checkNull) {
            return ResponseUtil.parameterNotNull();
        }
        List<User> list = userService.findByPhone(country + phone);
        if(!CollectionUtils.isEmpty(list)){
            return ResponseUtil.custom("????????????????????????");
        }
        return ResponseUtil.success();
    }

    /**
     * ?????????????????????
     * @return
     */
    public boolean checkVerificationSwitch(){
        PlatformConfig platformConfig = platformConfigService.findFirst();
        if (platformConfig == null) {
            return true;
        }
        Integer verificationCode = platformConfig.getVerificationCode();
        if (verificationCode == Constants.open) {
            return true;
        }
        return false;
    }

    private void setUserTokenToRedis(Long userId, String token) {
        JjwtUtil.Token jwtToken = new JjwtUtil.Token();
        jwtToken.setOldToken(token);
        redisUtil.set(Constants.TOKEN_CASINO_WEB + userId, jwtToken,JjwtUtil.ttl + Constants.WEB_REFRESH_TTL);
    }
}

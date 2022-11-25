package com.qianyi.casinoproxy.controller.jiceng;

import com.google.code.kaptcha.Producer;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinoproxy.model.ProxyUserLoginLog;
import com.qianyi.casinoproxy.service.ProxyUserLoginLogService;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.moduleauthenticator.GoogleAuthUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "基层代理认证中心")
@RestController
@RequestMapping("login/jiceng")
@Slf4j
public class ThridLoginController {

    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    private Producer captchaProducer;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyUserLoginLogService proxyUserLoginLogService;

    @Autowired
    private RedisUtil redisUtil;

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌身份验证器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "code", value = "验证码", required = true),
    })
    @PostMapping("loginB")
    public ResponseEntity loginB(String userName, String password, Integer code) {
        if (ObjectUtils.isEmpty(userName) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(code)) {
            return ResponseUtil.parameterNotNull();
        }

        boolean length = ProxyUser.checkLength(userName, password);
        if (!length) {
            return ResponseUtil.custom("帐号,密码长度3-15位");
        }

        ProxyUser proxyUser = proxyUserService.findByUserName(userName);
        if (proxyUser == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = proxyUser.getPassWord();
        boolean bcrypt = CasinoProxyUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        boolean flag = ProxyUser.checkUser(proxyUser);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }
        if (proxyUser.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("代理级别不对应");
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(proxyUser.getId() + "");
        subject.setBcryptPassword(proxyUser.getPassWord());
        String token = JjwtUtil.generic(subject, "casino-proxy");

//        if(Constants.open != user.getGaStatus()){//谷歌验证关闭
//            return ResponseUtil.success(token);
//        }

        String secret = proxyUser.getGaKey();
        if (CasinoProxyUtil.checkNull(secret)) {
            return ResponseUtil.custom("请先绑定谷歌身份验证器");
        }
        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
        if (!checkCode) {
            return ResponseUtil.googleAuthNoPass();
        }


        //记录登陆日志
        String ip = IpUtil.getIp(CasinoProxyUtil.getRequest());
        ProxyUserLoginLog proxyUserLoginLog = new ProxyUserLoginLog(ip, proxyUser.getUserName(), proxyUser.getId(), "proxy", "");
        proxyUserLoginLogService.saveSyncLog(proxyUserLoginLog);
        this.setUserTokenToRedis(proxyUser.getId(), token);
        return ResponseUtil.success(token);
    }

//    public static void main(String[] args) {
//        //用户密码
//        String password = "123456";
//        //密码加密
//        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
//        //加密
//        String newPassword = passwordEncoder.encode(password);
//        System.out.println("加密密码为："+newPassword);
//        //对比这两个密码是否是同一个密码
//        boolean matches = passwordEncoder.matches(password, newPassword);
//        System.out.println("两个密码一致:"+matches);
//    }

//    @ApiOperation("谷歌图形验证码")
//    @ApiImplicitParam(name = "code", value = "code前端可随机数或者时间戮，以降低冲突的次数", required = true)
//    @GetMapping("captcha")
//    @NoAuthentication
//    public void captcha(String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        if (LoginUtil.checkNull(code)) {
//            return;
//        }
//        //生产验证码字符串并保存到session中
//        String createText = captchaProducer.createText();
//
//        String key = LoginUtil.getCaptchaKey(request, code);
//        ExpiringMapUtil.putMap(key, createText);
//
//        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
//        ServletOutputStream responseOutputStream = response.getOutputStream();
//
//        //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
//        BufferedImage challenge = captchaProducer.createImage(createText);
//        ImageIO.write(challenge, "jpg", jpegOutputStream);
//        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
//        response.setHeader("Cache-Control", "no-store");
//        response.setHeader("Pragma", "no-cache");
//        response.setDateHeader("Expires", 0);
//        response.setContentType("image/jpeg");
//
//        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
//        responseOutputStream.write(captchaChallengeAsJpeg);
//        responseOutputStream.flush();
//        responseOutputStream.close();
//    }

    //1分钟3次
    @RequestLimit(limit = 6,timeout = 60)
    @GetMapping("google/auth/bind")
    @NoAuthentication
    @ApiOperation("绑定谷歌身份验证器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "userName", value = "帐号", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "返回二维码地址")
    })
    public ResponseEntity bindGoogleAuth(String userName, String password) {
        if (CasinoProxyUtil.checkNull(userName) || CasinoProxyUtil.checkNull(password)) {
            return ResponseUtil.parameterNotNull();
        }

        ProxyUser proxyUser = proxyUserService.findByUserName(userName);
        if (proxyUser == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = proxyUser.getPassWord();
        boolean bcrypt = CasinoProxyUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        boolean flag = ProxyUser.checkUser(proxyUser);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }
        if (proxyUser.getProxyRole() != CommonConst.NUMBER_3){
            return ResponseUtil.custom("代理级别不对应");
        }
        String secret = proxyUser.getGaKey();

        if (CasinoProxyUtil.checkNull(secret)) {
            secret = GoogleAuthUtil.generateSecretKey();
            proxyUser.setGaKey(secret);
            proxyUserService.save(proxyUser);
        }

        String qrcode = GoogleAuthUtil.getQcode(userName, secret);
        return ResponseUtil.success(qrcode);

    }
    @PostMapping("updatePassword")
    @ApiOperation("修改当前用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPassword", value = "原密码", required = true),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true),
            @ApiImplicitParam(name = "repetitionPassword", value = "重复新密码", required = true),
    })
    public ResponseEntity updatePassword(String oldPassword,String newPassword,String repetitionPassword){
        if (CasinoProxyUtil.checkNull(oldPassword,newPassword,repetitionPassword)){
            return ResponseUtil.custom("参数必填");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        String bcryptPassword = byId.getPassWord();
        boolean bcrypt = CasinoProxyUtil.checkBcrypt(oldPassword, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("原密码错误");
        }
        if (!newPassword.equals(repetitionPassword)){
            return ResponseUtil.custom("两次输入的新密码不一致");
        }
        if (!newPassword.matches(RegexEnum.PASSWORD.getRegex())){
            return ResponseUtil.custom("密码请输入6~15位数字和字母组合");
        }
        bcryptPassword = CasinoProxyUtil.bcrypt(newPassword);
        byId.setPassWord(bcryptPassword);
        proxyUserService.save(byId);
        return ResponseUtil.success();
    }
    @GetMapping("getProxyUser")
    @ApiOperation("查询用户数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
    })
    @NoAuthentication
    public ResponseEntity getProxyUser(String account, String password) {
        ProxyUser proxyUser = proxyUserService.findByUserName(account);
        if (proxyUser == null) {
            return ResponseUtil.custom("账户或密码错误");
        }
        String bcryptPassword = proxyUser.getPassWord();
        boolean bcrypt = CasinoProxyUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        if(CasinoProxyUtil.checkNull(proxyUser.getGaBind()) || !proxyUser.getGaBind().equals("2")){
            return ResponseUtil.success();
        }else{
            return ResponseUtil.custom("已经绑定谷歌验证码");
        }

    }

    @GetMapping("gaBind")
    @ApiOperation("绑定谷歌验证码标记")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
            @ApiImplicitParam(name = "code", value = "验证码", required = true),
    })
    @NoAuthentication
    public ResponseEntity gaBind(String account, Integer code) {
        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(code)) {
            return ResponseUtil.parameterNotNull();
        }
        ProxyUser proxyUser = proxyUserService.findByUserName(account);
        if (proxyUser == null) {
            return ResponseUtil.fail();
        }

        String secret = proxyUser.getGaKey();
        if (CasinoProxyUtil.checkNull(secret)) {
            return ResponseUtil.custom("请先绑定谷歌身份验证器");
        }
        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
        if (!checkCode) {
            return ResponseUtil.googleAuthNoPass();
        }

        if(CasinoProxyUtil.checkNull(proxyUser.getGaBind()) || !proxyUser.getGaBind().equals("2")){
            proxyUser.setGaBind("2");
            proxyUserService.save(proxyUser);
            return ResponseUtil.success();
        }else{
            return ResponseUtil.custom("已经绑定谷歌验证码");
        }
    }

    //1分钟3次
    @RequestLimit(limit = 5,timeout = 60)
    @GetMapping("getJwtToken")
    @ApiOperation("开发者通过此令牌调试接口。不可用于正式请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
    })
    @NoAuthentication
    public ResponseEntity getJwtToken(String account) {
        ProxyUser proxyUser = proxyUserService.findByUserName(account);
        if (proxyUser == null) {
            return ResponseUtil.fail();
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(proxyUser.getId() + "");
        subject.setBcryptPassword(proxyUser.getPassWord());
        String jwt = JjwtUtil.generic(subject, "casino-proxy");
        this.setUserTokenToRedis(proxyUser.getId(), jwt);
        return ResponseUtil.success(jwt);
    }

    @PostMapping("rjt")
    @ApiOperation("JWT过期后，30分钟内可颁发新的token")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "旧TOKEN", required = true),
    })
    public ResponseEntity refreshJwtToken(String token) {
        JjwtUtil.Subject subject = JjwtUtil.getSubject(token);
        ProxyUser proxyUser = proxyUserService.findById(Long.parseLong(subject.getUserId()));
        String refreshToken = JjwtUtil.refreshToken(token,proxyUser.getPassWord(), "casino-proxy");
        if (ObjectUtils.isEmpty(refreshToken)) {
            return ResponseUtil.authenticationNopass();
        }
        this.setUserTokenToRedis(proxyUser.getId(), token);
        return ResponseUtil.success(refreshToken);
    }
    private void setUserTokenToRedis(Long userId, String token) {
        JjwtUtil.Token jwtToken = new JjwtUtil.Token();
        jwtToken.setOldToken(token);
        redisUtil.set(Constants.REDIS_TOKEN_PROXY + userId , jwtToken);
    }

//    //1分钟3次
//    @RequestLimit(limit = 3,timeout = 60)
//    @NoAuthentication
//    @ApiOperation("添加管理员用户")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
//            @ApiImplicitParam(name = "nickName", value = "昵称", required = false),
//            @ApiImplicitParam(name = "password", value = "密码", required = true),
//            @ApiImplicitParam(name = "proxyRole", value = "代理角色 1：总代理 2：区域代理 3：基层代理", required = true),
//            @ApiImplicitParam(name = "pid", value = "上一级代理ID", required = false),
//    })
//    @PostMapping("save")
//    public ResponseEntity<ProxyUser> save(String userName, String password, String nickName, Integer proxyRole, Long pid) {
//        if(CasinoProxyUtil.checkNull(nickName)){
//            return ResponseUtil.parameterNotNull();
//        }
//        ProxyUser proxyUser = proxyUserService.findByUserName(userName);
//        if(proxyUser != null){
//            return ResponseUtil.custom("账户已经存在");
//        }
//
//        //加密
//        String bcryptPassword = CasinoProxyUtil.bcrypt(password);
//        ProxyUser proxy = new ProxyUser();
//        proxy.setUserName(userName);
//        proxy.setNickName(nickName);
//        proxy.setPassWord(bcryptPassword);
//        if(proxyRole < 1 || proxyRole > 3){
//            return ResponseUtil.custom("参数不合法");
//        }
//        proxy.setProxyRole(proxyRole);
//        if(proxyRole == 2){//区域代理
//            ProxyUser proxySole = proxyUserService.findById(pid);
//            if(proxySole == null || proxySole.getUserFlag() != 1){
//                return ResponseUtil.custom("总代理不存在或者已被锁定");
//            }
//            proxy.setSecondProxy(pid);
//        }
//        if(proxyRole == 3){
//            ProxyUser proxySole = proxyUserService.findById(pid);
//            if(proxySole == null || proxySole.getUserFlag() != 1){
//                return ResponseUtil.custom("总代理不存在或者已被锁定");
//            }
//            proxy.setSecondProxy(pid);
//            proxy.setFirstProxy(proxySole.getSecondProxy());
//            //邀请码生成
//            proxy.setProxyCode(LoginUtil.getProxyCode());
//        }
//
//        proxy.setUserFlag(Constants.open);
//        proxyUserService.save(proxy);
//        return ResponseUtil.success();
//    }

    //1分钟3次
//    @RequestLimit(limit = 10,timeout = 60)
    @ApiOperation("获取当前登录用户")
    @PostMapping("getPaoxyUser")
    public ResponseEntity<ProxyUser> getPaoxyUser() {
        Long loginUserId = CasinoProxyUtil.getAuthId();
        ProxyUser proxyUser = proxyUserService.findById(loginUserId);
        if(proxyUser == null){
            return ResponseUtil.custom("用户不存在");
        }else{
            return ResponseUtil.success(proxyUser);
        }
    }


}

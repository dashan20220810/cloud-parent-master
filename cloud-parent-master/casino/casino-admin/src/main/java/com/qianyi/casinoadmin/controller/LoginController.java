package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.Producer;
import com.qianyi.casinoadmin.service.SysUserLoginLogService;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.SysPermissionVo;
import com.qianyi.casinoadmin.vo.SysUserVo;
import com.qianyi.casinocore.business.RoleServiceBusiness;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinoadmin.model.SysUserLoginLog;
import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.moduleauthenticator.GoogleAuthUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.annotation.RequestLimit;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.IpUtil;
import com.qianyi.modulejjwt.JjwtUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 用户谷歌验证登录
 */
@Api(tags = "认证中心")
@RestController
@RequestMapping("login")
public class LoginController {

    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    private Producer captchaProducer;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserLoginLogService sysUserLoginLogService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private RoleServiceBusiness roleServiceBusiness;

    @Autowired
    RedisUtil redisUtil;

//    @NoAuthentication
//    @ApiOperation("帐密登陆.谷歌验证码")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
//            @ApiImplicitParam(name = "password", value = "密码", required = true),
//            @ApiImplicitParam(name = "captchaCode", value = "验证码代号", required = true),
//            @ApiImplicitParam(name = "captchaText", value = "验证码文本", required = true),
//    })
//    @PostMapping("loginA")
//    public ResponseEntity loginA(
//            String userName,
//            String password,
//            String captchaCode,
//            String captchaText) {
//        if (ObjectUtils.isEmpty(userName) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(captchaCode) || ObjectUtils.isEmpty(captchaText)) {
//            return ResponseUtil.parameterNotNull();
//        }
//
//        SysUser user = sysUserService.findByUserName(userName);
//        if (user == null) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        String bcryptPassword = user.getPassWord();
//        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
//        if (!bcrypt) {
//            return ResponseUtil.custom("帐号或密码错误");
//        }
//
//        boolean flag = user.checkUser(user);
//        if (!flag) {
//            return ResponseUtil.custom("该帐号不可操作");
//        }
//
//        String token = JjwtUtil.generic(user.getId() + "");
//        return ResponseUtil.success(token);
//    }

    @NoAuthentication
    @ApiOperation("帐密登陆.谷歌身份验证器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "code", value = "验证码", required = true),
    })
    @NoAuthorization
    @PostMapping("loginB")
    public ResponseEntity loginB(String userName, String password, Integer code) {
        if (ObjectUtils.isEmpty(userName) || ObjectUtils.isEmpty(password) || ObjectUtils.isEmpty(code)) {
            return ResponseUtil.parameterNotNull();
        }

        boolean length = SysUser.checkLength(userName, password);
        if (!length) {
            return ResponseUtil.custom("帐号,密码长度3-15位");
        }

        SysUser user = sysUserService.findByUserName(userName);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = user.getPassWord();
        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        boolean flag = SysUser.checkUser(user);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(user.getId() + "");
        subject.setBcryptPassword(user.getPassWord());
        String token = JjwtUtil.generic(subject, "casino-admin");

//        if(Constants.open != user.getGaStatus()){//谷歌验证关闭
//            return ResponseUtil.success(token);
//        }

        String secret = user.getGaKey();
        if (LoginUtil.checkNull(secret)) {
            return ResponseUtil.custom("请先绑定谷歌身份验证器");
        }
        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
        if (!checkCode) {
            return ResponseUtil.googleAuthNoPass();
        }


        //记录登陆日志
        String ip = IpUtil.getIp(LoginUtil.getRequest());
        SysUserLoginLog sysUserLoginLog = new SysUserLoginLog(ip, user.getUserName(), user.getId(), "admin", "");
        sysUserLoginLogService.saveSyncLog(sysUserLoginLog);
        setUserTokenToRedis(user.getId(), token);
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
    @NoAuthorization
    @ApiOperation("绑定谷歌身份验证器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "userName", value = "帐号", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "返回二维码地址")
    })
    public ResponseEntity bindGoogleAuth(String userName, String password) {
        if (LoginUtil.checkNull(userName) || LoginUtil.checkNull(password)) {
            return ResponseUtil.parameterNotNull();
        }

        SysUser user = sysUserService.findByUserName(userName);
        if (user == null) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        String bcryptPassword = user.getPassWord();
        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }

        boolean flag = SysUser.checkUser(user);
        if (!flag) {
            return ResponseUtil.custom("该帐号不可操作");
        }

        String secret = user.getGaKey();

        if (LoginUtil.checkNull(secret)) {
            secret = GoogleAuthUtil.generateSecretKey();
            user.setGaKey(secret);
            sysUserService.save(user);
        }

        String qrcode = GoogleAuthUtil.getQcode(userName, secret);
        return ResponseUtil.success(qrcode);

    }

    @GetMapping("getSysUser")
    @ApiOperation("查询用户数据")
    @NoAuthorization
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
    })
    @NoAuthentication
    public ResponseEntity getSysUser(String account, String password) {
        SysUser user = sysUserService.findByUserName(account);
        if (user == null) {
            return ResponseUtil.custom("账户或密码错误");
        }
        String bcryptPassword = user.getPassWord();
        boolean bcrypt = LoginUtil.checkBcrypt(password, bcryptPassword);
        if (!bcrypt) {
            return ResponseUtil.custom("帐号或密码错误");
        }
        if(LoginUtil.checkNull(user.getGaBind()) || !user.getGaBind().equals("2")){
            return ResponseUtil.success();
        }else{
            return ResponseUtil.custom("已经绑定谷歌验证码");
        }

    }

    @GetMapping("gaBind")
    @ApiOperation("绑定谷歌验证码标记")
    @NoAuthorization
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
            @ApiImplicitParam(name = "code", value = "验证码", required = true),
    })
    @NoAuthentication
    public ResponseEntity gaBind(String account, Integer code) {
        if (ObjectUtils.isEmpty(account) || ObjectUtils.isEmpty(code)) {
            return ResponseUtil.parameterNotNull();
        }
        SysUser user = sysUserService.findByUserName(account);
        if (user == null) {
            return ResponseUtil.fail();
        }

        String secret = user.getGaKey();
        if (LoginUtil.checkNull(secret)) {
            return ResponseUtil.custom("请先绑定谷歌身份验证器");
        }
        boolean checkCode = GoogleAuthUtil.check_code(secret, code);
        if (!checkCode) {
            return ResponseUtil.googleAuthNoPass();
        }

        if(LoginUtil.checkNull(user.getGaBind()) || !user.getGaBind().equals("2")){
            user.setGaBind("2");
            sysUserService.save(user);
            return ResponseUtil.success();
        }else{
            return ResponseUtil.custom("已经绑定谷歌验证码");
        }
    }

    //1分钟3次
    @RequestLimit(limit = 20,timeout = 60)
    @GetMapping("getJwtToken")
    @ApiOperation("开发者通过此令牌调试接口。不可用于正式请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "已注册的帐号", required = true),
    })
    @NoAuthentication
    @NoAuthorization
    public ResponseEntity getJwtToken(String account) {
        SysUser user = sysUserService.findByUserName(account);
        if (user == null) {
            return ResponseUtil.fail();
        }
        JjwtUtil.Subject subject = new JjwtUtil.Subject();
        subject.setUserId(user.getId() + "");
        subject.setBcryptPassword(user.getPassWord());
        String jwt = JjwtUtil.generic(subject, "casino-admin");
        setUserTokenToRedis(user.getId(), jwt);
        return ResponseUtil.success(jwt);
    }

    @PostMapping("rjt")
    @ApiOperation("JWT过期后，30分钟内可颁发新的token")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "旧TOKEN", required = true),
    })
    @NoAuthorization
    public ResponseEntity refreshJwtToken(String token) {
        JjwtUtil.Subject subject = JjwtUtil.getSubject(token);
        SysUser sysUser = sysUserService.findAllById(Long.parseLong(subject.getUserId()));
        String refreshToken = JjwtUtil.refreshToken(token,sysUser.getPassWord(), "casino-admin");
        if (ObjectUtils.isEmpty(refreshToken)) {
            return ResponseUtil.authenticationNopass();
        }
        setUserTokenToRedis(sysUser.getId(), refreshToken);
        return ResponseUtil.success(refreshToken);
    }

    //1分钟3次
    @RequestLimit(limit = 3,timeout = 60)
//    @NoAuthentication
    @ApiOperation("添加管理员用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "nickName", value = "昵称", required = false),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
    })
    @PostMapping("save")
    public ResponseEntity save(String userName, String password, String nickName) {

        if(LoginUtil.checkNull(nickName,password)){
            return ResponseUtil.parameterNotNull();
        }
        if (!userName.matches(RegexEnum.ACCOUNT.getRegex())){
            return ResponseUtil.custom("账号请输入6~15位数字或字母");
        }
        if (!password.matches(RegexEnum.PASSWORD.getRegex())){
            return ResponseUtil.custom("密码6~15位数字和字母的组合");
        }
        if (!LoginUtil.checkNull(nickName)){
            if (!nickName.matches(RegexEnum.NAME.getRegex())){
                return ResponseUtil.custom("昵称请输入1~20位中文或字母");
            }
        }

        SysUser sys = sysUserService.findByUserName(userName);
        if(sys != null){
            return ResponseUtil.custom("账户已经存在");
        }
        Long loginUserId = LoginUtil.getLoginUserId();
        SysUser sysLogin = sysUserService.findById(loginUserId);

        //加密
        String bcryptPassword = LoginUtil.bcrypt(password);
        SysUser sysUser = new SysUser();
        sysUser.setUserName(userName);
        sysUser.setNickName(nickName);
        sysUser.setPassWord(bcryptPassword);
        sysUser.setUserFlag(Constants.open);
        sysUser.setCreateBy(sysLogin==null?null:sysLogin.getUserName());
        sysUserService.save(sysUser);
        return ResponseUtil.success();
    }

    //1分钟3次
//    @RequestLimit(limit = 10,timeout = 60)
    @ApiOperation("获取当前登录用户")
    @PostMapping("getSysUser")
    @NoAuthorization
    public ResponseEntity<SysUser> getSysUser() {

        Long loginUserId = LoginUtil.getLoginUserId();
        SysUser sys = sysUserService.findById(loginUserId);

        if(sys == null){
            return ResponseUtil.custom("用户不存在");
        }else{
            List<SysPermission> sysPermissionList = new ArrayList<>();
            SysUserVo sysUserVo = new SysUserVo();
//            if(sys.getUserName().equals("admin")){
//                BeanUtils.copyProperties(sys, sysUserVo);
//                sysUserVo.setSysRoleId(0l);
//                sysPermissionList = sysPermissionService.findAll();
//            }else{
                SysUserRole sysUserRole = roleServiceBusiness.getSysUserRole(sys.getId());
                BeanUtils.copyProperties(sys, sysUserVo);
                if(sysUserRole == null){
                    return ResponseUtil.success(sysUserVo);
                }
                Long roleId = sysUserRole.getSysRoleId();

                if(roleId != null){
                    sysPermissionList = roleServiceBusiness.getSysPermissionList(roleId);
                }else{
                    //得到第一层数据
                    sysPermissionList = sysPermissionService.findAll();
                }
//            }

            List<SysPermission> sysPermissions = sysPermissionList.stream().filter(sysPermission -> sysPermission.getIsDetele() == 0).collect(Collectors.toList());
            if(null == sysPermissions || sysPermissions.size() <= 0){
                return ResponseUtil.success(sysUserVo);
            }
            List<SysPermissionVo> tree = DTOUtil.toNodeTree(
                    sysPermissions,
                    SysPermissionVo.class,
                    DTOUtil.config()
                            .setChildrenKey("sysPermissionVoList")
                            .build()
            );
            sysUserVo.setSysPermissionVoList(tree);
            return ResponseUtil.success(sysUserVo);
            /*List<SysPermissionVo> sysPermissionVos = JSON.parseArray(JSONObject.toJSONString(sysPermissions), SysPermissionVo.class);
            //得到第三层权限数据
            List<SysPermissionVo> sysPermissionThird = sysPermissionVos.stream().filter(sysPermissionVo -> sysPermissionVo.getMenuLevel() == 3).collect(Collectors.toList());
            List<SysPermissionVo> sysPermissionTwo = sysPermissionVos.stream().filter(sysPermissionVo -> sysPermissionVo.getMenuLevel() == 2).collect(Collectors.toList());
            List<SysPermissionVo> sysPermissionOne = sysPermissionVos.stream().filter(sysPermissionVo -> sysPermissionVo.getMenuLevel() == 1).collect(Collectors.toList());
            for (SysPermissionVo sysTwo : sysPermissionTwo) {
                for (SysPermissionVo sysThrid : sysPermissionThird) {
                    if(sysThrid.getPid() == null){
                        continue;
                    }
                    if(sysTwo.getId().intValue() == sysThrid.getPid().intValue()){
                        sysTwo.getSysPermissionVoList().add(sysThrid);
                    }
                }

            }
            sysPermissionOne.stream().forEach(sysOne -> {
                sysPermissionTwo.stream().forEach(sysTwo ->{
                    if(sysOne.getId().intValue() == sysTwo.getPid().intValue()){
                        sysOne.getSysPermissionVoList().add(sysTwo);
                    }
                });
            });
            sysUserVo.setSysPermissionVoList(sysPermissionOne);
            return ResponseUtil.success(sysUserVo);*/
        }
    }

    /**
     * 重置用户密码
     *
     * @param userName
     * @param password
     * @return
     */
    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
    })
    @PostMapping("resetPassword")
    public ResponseEntity resetPassword(String userName, String password) {
        if(LoginUtil.checkNull(userName, password)){
            return ResponseUtil.custom("参数错误");
        }
        SysUser sys = sysUserService.findByUserName(userName);
        if(sys == null){
            return ResponseUtil.custom("账号不存在");
        }
        boolean length = ProxyUser.checkLength(password);
        if (!length) {
            return ResponseUtil.custom("密码长度3-15位");
        }
        //加密
        String bcryptPassword = LoginUtil.bcrypt(password);
        if(bcryptPassword.equals(sys.getPassWord())){
            return ResponseUtil.custom("新密码和旧密码相同");
        }
        Long loginUserId = LoginUtil.getLoginUserId();
        SysUser sysLogin = sysUserService.findById(loginUserId);

        sys.setUpdateBy(sysLogin==null?null:sysLogin.getUserName());
        sys.setPassWord(bcryptPassword);
        sysUserService.save(sys);
        return ResponseUtil.success();
    }

    /**
     * 充值谷歌验证码
     *
     * @param userName
     * @return
     */
    @ApiOperation("重置谷歌验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "帐号", required = true),
    })
    @PostMapping("resetGaKey")
    public ResponseEntity resetGaKey(String userName) {
        if(LoginUtil.checkNull(userName)){
            return ResponseUtil.custom("参数错误");
        }
        SysUser sys = sysUserService.findByUserName(userName);
        if(sys == null){
            return ResponseUtil.custom("账号不存在");
        }
        Long loginUserId = LoginUtil.getLoginUserId();
        SysUser sysLogin = sysUserService.findById(loginUserId);

        sys.setGaBind(Constants.open + "");
        sys.setGaKey(null);
        sys.setUpdateBy(sysLogin==null?null:sysLogin.getUserName());
        sysUserService.save(sys);
        return ResponseUtil.success();
    }

    @GetMapping("serviceHealthCheck")
    @ApiOperation("服务健康状态监测")
    @NoAuthentication
    public ResponseEntity serverHealthCheck() {
        return ResponseUtil.success();
    }

    private void setUserTokenToRedis(Long userId, String token) {
        JjwtUtil.Token jwtToken = new JjwtUtil.Token();
        jwtToken.setOldToken(token);
        redisUtil.set(Constants.REDIS_TOKEN_ADMIN + userId , jwtToken);
    }



    /**
     * 修改当前用户密码
     *
     * @param password
     * @return
     */
    @NoAuthorization
    @ApiOperation("修改当前用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true),
    })
    @PostMapping("resetCurrentPassword")
    public ResponseEntity resetCurrentPassword(String password) {
        if(LoginUtil.checkNull(password)){
            return ResponseUtil.custom("参数错误");
        }
        boolean length = ProxyUser.checkLength(password);
        if (!length) {
            return ResponseUtil.custom("密码长度3-15位");
        }
        Long loginUserId = LoginUtil.getLoginUserId();
        SysUser sys = sysUserService.findById(loginUserId);
        //加密
        String bcryptPassword = LoginUtil.bcrypt(password);
        if(bcryptPassword.equals(sys.getPassWord())){
            return ResponseUtil.custom("新密码和旧密码相同");
        }
        sys.setUpdateBy(sys==null?null:sys.getUserName());
        sys.setPassWord(bcryptPassword);
        sysUserService.save(sys);
        return ResponseUtil.success();
    }

}

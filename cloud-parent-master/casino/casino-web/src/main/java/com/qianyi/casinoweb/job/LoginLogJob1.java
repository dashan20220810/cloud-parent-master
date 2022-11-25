//package com.qianyi.casinoweb.job;
//
//import com.qianyi.casinocore.model.LoginLog;
//import com.qianyi.casinocore.service.LoginLogService;
//import com.qianyi.modulecommon.util.IpUtil;
//import com.qianyi.modulecommon.util.SpringContextUtil;
//
///**
// * 登陆信息处理线程
// */
//public class LoginLogJob1 implements Runnable {
//
//    LoginLogService loginLogService = SpringContextUtil.getBean(LoginLogService.class);
//
//    private String ip;
//    private String account;
//    private Long userId;
//    private String description;
//
//    private LoginLogJob1() {
//
//    }
//
//    public LoginLogJob1(String ip, String account, Long userId, String description) {
//        this.ip = ip;
//        this.account = account;
//        this.userId = userId;
//        this.description = description;
//    }
//
//    @Override
//    public void run() {
//
//        LoginLog loginLog = new LoginLog();
//
//        loginLog.setIp(ip);
//
//        loginLog.setAccount(account);
//        loginLog.setUserId(userId);
//        loginLog.setDescription(description);
//
//        String address = IpUtil.getAddress(ip);
//        if (address != null) {
//            loginLog.setAddress(address);
//        }
//
//        loginLogService.save(loginLog);
//
//    }
//
//
//}

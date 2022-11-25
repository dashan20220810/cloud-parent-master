package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class InitSysUserAdmin {
    /**
     * application没有配置，就给一个默认值
     */
    @Value("${project.username:admin}")
    public String userName;

    @Value("${project.password:123456}")
    public String password;

    @Autowired
    private SysUserService sysUserService;

//    /**
//     * 初始化，添加超级管理员
//     */
//    @PostConstruct
//    public void initSuperSysUser(){
//        if(LoginUtil.checkNull(userName, password)){
//            return;
//        }
//        SysUser sys = sysUserService.findByUserName(userName);
//        if(sys != null){
//            return;
//        }
//        SysUser sysUser = new SysUser();
//        sysUser.setUserFlag(Constants.open);
//        sysUser.setUserName(userName);
//        String bcryptPassword = LoginUtil.bcrypt(password);
//        sysUser.setPassWord(bcryptPassword);
//        sysUser.setNickName(userName);
//
//        sysUserService.save(sysUser);
//    }
}

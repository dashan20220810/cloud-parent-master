package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.service.SysUserService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统管理员缓存初始化
 */
@Component
@Slf4j
@Order(3)
public class SysUserInitialize   implements CommandLineRunner {
    @Autowired
    private SysUserService sysUserService;
    @Override
    public void run(String... args) throws Exception {
        log.info("系统管理员缓存初始化开始============================================》");
        List<SysUser> all = sysUserService.findAll();
        if (LoginUtil.checkNull(all) || all.size() == CommonConst.NUMBER_0)
            return;
        all.forEach(sysUser -> {
            sysUserService.save(sysUser);
        });
        log.info("系统管理员缓存初始化结束============================================》");
    }
}

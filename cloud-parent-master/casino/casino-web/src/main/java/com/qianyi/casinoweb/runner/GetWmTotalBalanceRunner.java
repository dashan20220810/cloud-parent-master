package com.qianyi.casinoweb.runner;

import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.casinoweb.job.GetWmTotalBalanceJob;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一次启动项目初始化wm余额
 */
@Component
@Slf4j
@Order(2)
public class GetWmTotalBalanceRunner implements CommandLineRunner {

    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private GetWmTotalBalanceJob getWmTotalBalanceJob;

    @Override
    public void run(String... args) throws Exception {
        log.info("启动项目初始化wm余额");
        PlatformConfig platformConfig = platformConfigService.findFirst();
        log.info("platformConfig查询结果data={}",platformConfig);
        if (platformConfig == null || platformConfig.getWmMoney() == null) {
            log.info("开始初始化wm余额");
            getWmTotalBalanceJob.tasks();
        }
        log.info("wm余额初始化完成");
    }
}

package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 每3分钟查询一次平台在WM的总余额，更新到本地
 */
@Component
@Slf4j
public class GetWmTotalBalanceJob {

    @Autowired
    private PublicWMApi wmApi;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    private ThirdGameBusiness thirdGameBusiness;
    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Value("${spring.profiles.active}")
    private String active;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void tasks() {
        try {
            log.info("开始查询平台在WM的总余额");
            BigDecimal agentBalance = wmApi.getAgentBalance(0);
            if (agentBalance == null) {
                log.error("查询平台在WM的总余额远程请求异常");
                gameRecordAsyncOper.sendMsgToTelegramBot(active+"环境,查询平台在WM的总余额异常,原因:远程请求异常");
                return;
            }
            PlatformConfig platformConfig = platformConfigService.findFirst();
            if (platformConfig == null) {
                platformConfig = new PlatformConfig();
                platformConfig.setWmMoney(agentBalance);
                platformConfigService.save(platformConfig);
                return;
            }
            platformConfig.setWmMoney(agentBalance);
            platformConfigService.save(platformConfig);
            log.info("平台在WM的总余额更新完成");
        } catch (Exception e) {
            log.error("查询平台在WM的总余额异常");
            gameRecordAsyncOper.sendMsgToTelegramBot(active+"环境,查询平台在WM的总余额异常,原因:" + e.getMessage());
            e.printStackTrace();
        }
    }
}

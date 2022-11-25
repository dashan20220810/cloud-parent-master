package com.qianyi.casinoweb.job;

import com.qianyi.casinocore.business.ExtractPointsConfigBusiness;
import com.qianyi.casinocore.business.TelegramBotBusiness;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.PlatformConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 开启异步洗码，打码，分润，单独写出来是为了解决异步事务失效问题
 */
@Service
public class GameRecordAsyncOper {

    @Autowired
    private UserMoneyBusiness userMoneyBusiness;
    @Autowired
    private TelegramBotBusiness telegramBotBusiness;

    @Autowired
    private ExtractPointsConfigBusiness extractPointsConfigBusiness;

    /**
     * 异步洗码
     *
     * @param platform
     * @param gameRecord
     */
    @Async("asyncExecutor")
    public void washCode(String platform, GameRecord gameRecord) {
        userMoneyBusiness.washCode(platform, gameRecord);
    }

    /**
     * 异步抽点
     * @param platform
     * @param gameRecord
     */
    @Async("asyncExecutor")
    public void extractPoints(String platform, GameRecord gameRecord) {
        extractPointsConfigBusiness.extractPoints(platform, gameRecord);
    }

    /**
     * 异步打码
     *
     * @param platformConfig
     * @param record
     */
    @Async("asyncExecutor")
    public void subCodeNum(String platform, PlatformConfig platformConfig, GameRecord record) {
        userMoneyBusiness.subCodeNum(platform, platformConfig, record);
    }

    /**
     * 异步分润
     *
     * @param record
     */
    @Async("asyncExecutor")
    public void shareProfit(String platform, GameRecord record) {
        userMoneyBusiness.shareProfit(platform, record);
    }

    /**
     * 异步返利
     * @param platform
     * @param record
     */
    @Async("asyncExecutor")
    public void rebate(String platform, GameRecord record) {
        userMoneyBusiness.rebate(platform, record);
    }

    /**
     * 异步推送MQ消息
     * @param platform
     * @param record
     */
    @Async("asyncExecutor")
    public void proxyGameRecordReport(String platform, GameRecord record) {
        userMoneyBusiness.proxyGameRecordReport(platform, record);
    }
    /**
     * 异步给TG机器人发送消息
     *
     * @param msg
     */
    @Async("asyncExecutor")
    public void sendMsgToTelegramBot(String msg) {
        telegramBotBusiness.sendMsgToTelegramBot(msg);
    }
}

package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class TelegramBotBusiness {

    @Value("${project.telegramBot.token:null}")
    private String token;
    @Value("${project.telegramBot.chatId:null}")
    private String chatId;

    /**
     * https://blog.csdn.net/dodod2012/article/details/102685519
     * 发送消息到Telegram 机器人
     *
     * @param msg
     */
    public String sendMsgToTelegramBot(String msg) {
        log.info("开始给TG机器人发送消息,token={},chatId={},msg={}", token, chatId, msg);
        if (ObjectUtils.isEmpty(token) || ObjectUtils.isEmpty(chatId) || ObjectUtils.isEmpty(msg)) {
            log.info("token或chatId或者msg为空，token={},chatId={},msg={}", token, chatId, msg);
            return null;
        }
        try {
            String sendMsgUrl = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + msg;
            String sendMsgUrlResult = HttpClient4Util.specialGet(sendMsgUrl);
            log.info("token={},chatId={},msg={},result={}消息发送结果", token, chatId, msg, sendMsgUrlResult);
            if (ObjectUtils.isEmpty(sendMsgUrlResult)) {
                log.error("token={},chatId={},msg={}消息发送失败1", token, chatId, msg);
                return null;
            }
            JSONObject msgResult = JSONObject.parseObject(sendMsgUrlResult);
            if (msgResult == null) {
                log.error("token={},chatId={},msg={}消息发送失败2", token, chatId, msg);
                return null;
            }
            if (msgResult.getBoolean("ok")) {
                log.info("token={},chatId={},msg={}消息发送成功", token, chatId, msg);
                return sendMsgUrlResult;
            }
            log.error("token={},chatId={},msg={}消息发送失败3", token, chatId, msg);
        } catch (Exception e) {
            log.error("TelegramBot消息发送失败，token={},chatId={},msg={},错误信息={}", token, chatId, msg,e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

package com.qianyi.livegoldenf.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PublicGoldenFApi {

    @Value("${project.goldenf.apiUrl:null}")
    private String apiUrl;
    @Value("${project.goldenf.recordUrl:null}")
    private String recordUrl;
    @Value("${project.goldenf.secretKey:null}")
    private String secretKey;
    @Value("${project.goldenf.operatorToken:null}")
    private String operatorToken;

    /**
     * 创建账号
     *
     * @param playerName 账号
     * @param currency   货币（选填）
     * @return
     */
    public boolean playerCreate(String playerName, String currency) {
        String url = apiUrl + "/Player/Create";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("player_name", playerName);
        if (!ObjectUtils.isEmpty(currency)) {
            params.put("currency", currency);
        }
        log.info("创建玩家账号参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("创建玩家账号结果{}：", result);
        ResponseEntity entity = entity(result);
        JSONObject jsonData = null;
        if (entity != null && ObjectUtils.isEmpty(entity.getErrorCode())) {
            jsonData = JSONObject.parseObject(entity.getData());
        }
        String actionResult = null;
        if (jsonData != null) {
            actionResult = jsonData.getString("action_result");
        }
        if ("Success".equals(actionResult)) {
            return true;
        }
        return false;
    }

    /**
     * 游戏试玩
     *
     * @param gameCode 游戏代码
     * @param language 游戏语言（选填）
     * @return
     */
    public ResponseEntity startGameDemo(String gameCode, String language) {
        String url = apiUrl + "/Demo";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("game_code", gameCode);
        if (!ObjectUtils.isEmpty(language)) {
            params.put("language", language);
        }
        log.info("试玩游戏参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("试玩游戏结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * @param palerName 玩家账号
     * @param gameCode  游戏代码
     * @param language  游戏语言（选填）
     * @param limit     游戏限红（选填）目前仅支援MGPLUS真人视讯、SBO
     * @return
     */
    public ResponseEntity startGame(String palerName, String gameCode, String language, String limit) {
        String url = apiUrl + "/Launch";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("game_code", gameCode);
        params.put("player_name", palerName);
        if (!ObjectUtils.isEmpty(language)) {
            params.put("language", language);
        }
        if (!ObjectUtils.isEmpty(limit)) {
            params.put("limit", limit);
        }
        log.info("启动游戏参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("启动游戏结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    public static void main(String[] args) {
        String url =  "http://kk.test.gf-gaming.com/gf/TransferIn";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", "16e4ef534cec559430e07e05eb71c719");
        params.put("operator_token", "7970f61d512b7b681aa149fad927eee8");
        params.put("amount", 10);
        params.put("player_name", "test001");
        log.info("启动游戏参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
    }

    /**
     * 玩家充值
     *
     * @param palerName  玩家账号
     * @param amount     转入金额
     * @param traceId    由对接平台方提供交易编号 (选填) (用于追踪唯一值）
     * @param walletCode 指定钱包代码（选填) (适用于2.04后的API） 缺省值：gf_main_balance
     * @return
     */
    public ResponseEntity transferIn(String palerName, double amount, String traceId, String walletCode) {
        String url = apiUrl + "/TransferIn";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("amount", amount);
        params.put("player_name", palerName);
        if (!ObjectUtils.isEmpty(walletCode)) {
            params.put("wallet_code", walletCode);
        }
        if (!ObjectUtils.isEmpty(traceId)) {
            params.put("traceId", traceId);
        }
        log.info("玩家充值参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("玩家充值结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 玩家提值
     *
     * @param palerName  玩家账号
     * @param amount     金额
     * @param traceId    由对接平台方提供交易编号(选填) (用于追踪唯一值)
     * @param walletCode 指定钱包代码 (选填) (适用于2.04后的API)
     * @return
     */
    public ResponseEntity transferOut(String palerName, double amount, String traceId, String walletCode) {
        String url = apiUrl + "/TransferOut";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("amount", amount);
        params.put("player_name", palerName);
        if (!ObjectUtils.isEmpty(walletCode)) {
            params.put("wallet_code", walletCode);
        }
        if (!ObjectUtils.isEmpty(traceId)) {
            params.put("traceId", traceId);
        }
        log.info("玩家提值参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("玩家提值结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 查询玩家钱包余额
     *
     * @param palerName  玩家账号
     * @param walletCode 指定钱包代码（选填）（适用于2.04后的API）
     * @return
     */
    public ResponseEntity getPlayerBalance(String palerName, String walletCode) {
        String url = apiUrl + "/GetPlayerBalance";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("player_name", palerName);
        if (!ObjectUtils.isEmpty(walletCode)) {
            params.put("wallet_code", walletCode);
        }
        log.info("查询玩家钱包余额参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("查询玩家钱包余额结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 获取单个玩家的转账记录
     *
     * @param palerName      玩家账号
     * @param startTime      开始时间，13位数时间戳格式
     * @param endTime        结束时间，13位数时间戳格式
     * @param walletCode     指定钱包代码（选填）
     * @param traceId        交易编号（选填）（补充1）
     * @param timestampDigit 指定返回时间戳格式（选填） 缺省值：13 10 = 10位数时间戳格式 13 = 13位数时间戳格式
     * @return
     */
    public ResponseEntity getPlayerTransactionRecord(String palerName, long startTime, long endTime, String walletCode, String traceId, Integer timestampDigit) {
        String url = recordUrl + "/Transaction/Record/Player/Get";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("player_name", palerName);
        params.put("start_time", startTime);
        params.put("end_time", endTime);
        if (!ObjectUtils.isEmpty(traceId)) {
            params.put("traceId", traceId);
        }
        if (!ObjectUtils.isEmpty(walletCode)) {
            params.put("wallet_code", walletCode);
        }
        if (timestampDigit != null) {
            params.put("timestamp_digit", timestampDigit);
        }
        log.info("查询单个玩家的转账记录参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("查询单个玩家的转账记录结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    public ResponseEntity getPlayerGameRecord(Long startTime,Long endTime,String vendorCode,int page,int pageSize){
        String url = recordUrl + "/v3/Bet/Record/Get";
        Map<String, Object> params = new HashMap<>();
        params.put("secret_key", secretKey);
        params.put("operator_token", operatorToken);
        params.put("vendor_code", vendorCode);
        params.put("start_time", startTime);
        params.put("end_time", endTime);
        params.put("page", page);
        params.put("page_size", pageSize);
        log.info("获取所有投注记录参数{}：", JSONObject.toJSONString(params));
        String result = HttpClient4Util.doPost(url, params);
        log.info("获取所有投注记录结果{}：", result);
        ResponseEntity entity = entity(result);
        return entity;

    }

    private static ResponseEntity entity(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        ResponseEntity entity = new ResponseEntity();
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            entity.setErrorCode("500");
            entity.setErrorMessage("远程请求GoldenF异常,请重新操作");
            return entity;
        }
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        JSONObject error = jsonObject.getJSONObject("error");
        if (error != null) {
            String code = error.getString("code");
            String message = error.getString("message");
            entity.setErrorCode(code);
            entity.setErrorMessage(message);
        }
        String data = jsonObject.getString("data");
        entity.setData(data);
        return entity;
    }

    @Data
    public static class ResponseEntity {

        private String errorCode;

        private String errorMessage;

        private String data;
    }

}

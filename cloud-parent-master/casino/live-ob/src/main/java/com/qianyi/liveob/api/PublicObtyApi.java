package com.qianyi.liveob.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.liveob.utils.GenerateSignUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.TreeMap;

/**
 * 接口文档  http://api-doc.sportxxxw1box.com/#/main/api_doc_zn
 */
@Component
@Slf4j
public class PublicObtyApi {

    @Value("${project.obty.merchantCode:null}")
    private String merchantCode;
    @Value("${project.obty.secretKey:null}")
    private String secretKey;
    @Value("${project.obty.currency:null}")
    private String currency;
    @Value("${project.obty.apiUrl:null}")
    private String apiUrl;

    /**
     * 注册
     *
     * @param userName 用户(可以包含但是不要等同于特殊字符或者空格,长度控制在30个字符以下)
     * @param nickname 昵称，非必填
     * @return
     */
    public boolean create(String userName, String nickname) {
        String url = apiUrl + "/api/user/create";
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchantCode", merchantCode);
        treeMap.put("currency", currency);
        treeMap.put("userName", userName);
        if (!ObjectUtils.isEmpty(nickname)) {
            treeMap.put("nickname", nickname);
        }
        Long timestamp = System.currentTimeMillis();
        treeMap.put("timestamp", timestamp);
        String sign = GenerateSignUtil.getObtyMd5Sign(userName, merchantCode, timestamp);
        String signature = GenerateSignUtil.getObtyMd5Sign(sign, secretKey);
        treeMap.put("signature", signature);
        log.info("OB体育创建玩家账号参数{}", treeMap.toString());
        String result = HttpClient4Util.doPost(url, treeMap);
        log.info("OB体育创建玩家账号结果{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null) {
            log.error("OB体育创建玩家账号远程请求异常");
            return false;
        }
        if (!entity.getStatus()) {
            log.error("OB体育创建玩家账号出错,result={}", entity.toString());
            return false;
        }
        return true;
    }

    /**
     * @param userName 用户名
     * @param terminal 终端类型,为空或"pc"为电脑端,"mobile"为手机端
     * @param balance  非必填 金额,至小数2位
     * @return
     */
    public ResponseEntity login(String userName, String terminal, BigDecimal balance) {
        String url = apiUrl + "/api/user/login";
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchantCode", merchantCode);
        treeMap.put("currency", currency);
        treeMap.put("userName", userName);
        treeMap.put("terminal", terminal);
        if (!ObjectUtils.isEmpty(balance)) {
            treeMap.put("balance", balance);
        }
        Long timestamp = System.currentTimeMillis();
        treeMap.put("timestamp", timestamp);
        String sign = GenerateSignUtil.getObtyMd5Sign(merchantCode, userName, terminal, timestamp);
        String signature = GenerateSignUtil.getObtyMd5Sign(sign, secretKey);
        treeMap.put("signature", signature);
        log.info("OB体育玩家登录参数{}", treeMap.toString());
        String result = HttpClient4Util.doPost(url, treeMap);
        log.info("OB体育玩家登录结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 踢出用户
     *
     * @param userName
     * @return
     */
    public ResponseEntity kickOutUser(String userName) {
        String url = apiUrl + "/api/user/kickOutUser";
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchantCode", merchantCode);
        treeMap.put("userName", userName);
        Long timestamp = System.currentTimeMillis();
        treeMap.put("timestamp", timestamp);
        String sign = GenerateSignUtil.getObtyMd5Sign(merchantCode, userName, timestamp);
        String signature = GenerateSignUtil.getObtyMd5Sign(sign, secretKey);
        treeMap.put("signature", signature);
        log.info("OB体育踢出玩家参数{}", treeMap.toString());
        String result = HttpClient4Util.doPost(url, treeMap);
        log.info("OB体育踢出玩家结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 查询用户余额
     *
     * @param userName
     * @return
     */
    public ResponseEntity checkBalance(String userName) {
        String url = apiUrl + "/api/fund/checkBalance";
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchantCode", merchantCode);
        treeMap.put("userName", userName);
        Long timestamp = System.currentTimeMillis();
        treeMap.put("timestamp", timestamp);
        String sign = GenerateSignUtil.getObtyMd5Sign(merchantCode, userName, timestamp);
        String signature = GenerateSignUtil.getObtyMd5Sign(sign, secretKey);
        treeMap.put("signature", signature);
        log.info("OB体育查询用户余额参数{}", treeMap.toString());
        String result = HttpClient4Util.doPost(url, treeMap);
        log.info("OB体育查询用户余额结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 资金转移
     *
     * @param userName
     * @param transferType 转账类型,1:加款,2:扣款
     * @param amount       金额,至小数2位
     * @param transferId   交易的讯息号,唯一标示,不可重複,19位数字类型字符串
     * @return
     */
    public ResponseEntity transfer(String userName, Integer transferType, BigDecimal amount, String transferId) {
        String url = apiUrl + "/api/fund/transfer";
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchantCode", merchantCode);
        treeMap.put("userName", userName);
        treeMap.put("transferType", transferType);
        treeMap.put("amount", amount);
        treeMap.put("transferId", transferId);
        Long timestamp = System.currentTimeMillis();
        treeMap.put("timestamp", timestamp);
        String sign = GenerateSignUtil.getObtyMd5Sign(merchantCode, userName, transferType, amount, transferId, timestamp);
        String signature = GenerateSignUtil.getObtyMd5Sign(sign, secretKey);
        treeMap.put("signature", signature);
        log.info("OB体育资金转移参数{}", treeMap.toString());
        String result = HttpClient4Util.doPost(url, treeMap);
        log.info("OB体育资金转移结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 查询投注记录  查询用户投注记录接口,查询最近一周的数据,每次最大100条
     *
     * @param userName
     * @param startTime
     * @param endTime
     * @param settleStatus 结算状态(0:未结算1:已结算),若为空则全部查询
     * @param pageNum
     * @param pageSize     每页条数,默认10条
     * @return
     */
    public ResponseEntity queryBetList(String userName, Long startTime, Long endTime, Integer settleStatus, Integer pageNum, Integer pageSize) {
        String url = apiUrl + "/api/bet/queryBetList";
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchantCode", merchantCode);
        treeMap.put("startTime", startTime);
        treeMap.put("endTime", endTime);
        if (!ObjectUtils.isEmpty(userName)) {
            treeMap.put("userName", userName);
        }
        if (!ObjectUtils.isEmpty(pageNum)) {
            treeMap.put("pageNum", pageNum);
        }
        if (!ObjectUtils.isEmpty(settleStatus)) {
            treeMap.put("settleStatus", settleStatus);
        }
        if (!ObjectUtils.isEmpty(userName)) {
            treeMap.put("pageSize", pageSize);
        }
        Long timestamp = System.currentTimeMillis();
        treeMap.put("timestamp", timestamp);
        String sign = GenerateSignUtil.getObtyMd5Sign(merchantCode, startTime, endTime, timestamp);
        String signature = GenerateSignUtil.getObtyMd5Sign(sign, secretKey);
        treeMap.put("signature", signature);
        log.info("OB体育查询投注记录参数{}", treeMap.toString());
        String result = HttpClient4Util.doPost(url, treeMap);
        log.info("OB体育查询投注记录结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }


    private static ResponseEntity entity(String result) {
        if (ObjectUtils.isEmpty(result)) {
            return null;
        }
        JSONObject jsonObject = null;
        ResponseEntity entity = new ResponseEntity();
        try {
            jsonObject = JSONObject.parseObject(result);
        } catch (Exception e) {
            String msg = "解析OB体育数据时出错,msg=" + e.getMessage();
            log.error(msg);
            entity.setCode(500);
            entity.setStatus(Boolean.FALSE);
            entity.setMsg(msg);
            entity.setData("远程请求OB体育异常");
            return entity;
        }
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        entity.setCode(jsonObject.getInteger("code"));
        entity.setStatus(jsonObject.getBoolean("status"));
        entity.setData(jsonObject.getString("data"));
        entity.setMsg(jsonObject.getString("msg"));
        return entity;
    }

    @Data
    public static class ResponseEntity {

        private Integer code;

        private Boolean status;

        private String msg;

        private String data;
    }
}

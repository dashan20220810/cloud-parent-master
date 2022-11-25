package com.qianyi.livewm.api;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

@Component
public class PublicWMApi {

    @Value("${project.wmUrl:0}")
    String url;
    @Value("${project.vendorId:0}")
    String vendorId;
    @Value("${project.signature:0}")
    String signature;

    //注册
    public boolean register(String user, String password, String username, String maxwin
            , String maxlose, String mark, String syslang) {
        String cmd = "MemberRegister";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("password", password);
        params.put("username", username);
        if (!ObjectUtils.isEmpty(maxwin)) {
            params.put("maxwin", maxwin);
        }
        if (!ObjectUtils.isEmpty(maxlose)) {
            params.put("maxlose", maxlose);
        }
        if (!ObjectUtils.isEmpty(mark)) {
            params.put("mark", mark);
        }

        params.put("timestamp", timestamp);

        if (!ObjectUtils.isEmpty(syslang)) {
            params.put("syslang", syslang);
        }

        System.out.println("注册参数："+JSONObject.toJSONString(params));
        String s = HttpClient4Util.doPost(url, params);
        System.out.println(s);
        if (s == null) {
            return false;
        }

        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }
        return false;
    }

    //开游戏
    public String openGame(String user, String password, Integer lang, String voice, Integer ui, String mode,Integer size,String returnurl) {
        String cmd = "SigninGame";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        if (size != null) {
            params.put("size", size);//1:iframe嵌入
        }
        if (!CommonUtil.checkNull(returnurl)) {
            params.put("returnurl", returnurl);//返回路径
        }
        params.put("password", password);
        params.put("lang", lang);
        if (!CommonUtil.checkNull(voice)) {
            params.put("voice", voice);
        }
        if (!CommonUtil.checkNull(mode)) {
            params.put("mode", mode);
        }
        params.put("ui", ui);
        params.put("timestamp", timestamp);

        String s = HttpClient4Util.doPost(url, params);
        System.out.println("开游戏参数："+JSONObject.toJSONString(params));

        if (CommonUtil.checkNull(s)) {
            return null;
        }

        ResponseEntity entity = entity(s);
        return (String) entity.getResult();
    }

    public Boolean logoutGame(String user,Integer syslang){
        String cmd = "LogoutGame";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("timestamp", timestamp);
        if (syslang != null) {
            params.put("syslang", syslang);
        }
        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return false;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }

        return false;

    }

    //加扣点
    public ResponseEntity changeBalance(String user, BigDecimal money, String order, Integer syslang) {
        String cmd = "ChangeBalance";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("money", money.floatValue());
        if(!ObjectUtils.isEmpty(order)){
            params.put("order", order);
        }
        params.put("timestamp", timestamp);
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);

        if (CommonUtil.checkNull(s)) {
            return null;
        }

        ResponseEntity entity = entity(s);
        return entity;

    }

    //查询代理商余额
    public BigDecimal getAgentBalance(Integer syslang) throws Exception {
        String cmd = "GetAgentBalance";
        Integer timestamp = getTimestamp();

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("timestamp", timestamp);
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
//            float balance = (float) entity.getResult();
//            BigDecimal decimal = new BigDecimal(Float.toString(balance));
//            return decimal.setScale(2);
            BigDecimal decimal = new BigDecimal(String.valueOf(entity.getResult()));
            return decimal;
        }
        throw new Exception(String.valueOf(entity));
    }

    //取用户余额
    public BigDecimal getBalance(String user, Integer syslang) throws Exception {
        String cmd = "GetBalance";

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("timestamp", getTimestamp());
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
//            float balance = (float) entity.getResult();
//            BigDecimal decimal = new BigDecimal(Float.toString(balance));
            BigDecimal decimal = new BigDecimal(String.valueOf(entity.getResult()));
            return decimal;
        }
        throw new Exception(String.valueOf(entity));
    }

    //开关游戏帐号的登陆和下注
    //type: login:啟用, bet: 下注
    //status: Y:启用, N:停用
    //syslang: 	0:中文, 1:英文(非必要)
    public boolean switchGame(String user, String type, String status, Integer syslang) {
        String cmd = "EnableorDisablemem";


        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        params.put("user", user);
        params.put("type", type);
        params.put("status", status);
        params.put("timestamp", getTimestamp());
        if (syslang != null) {
            params.put("syslang", syslang);
        }

        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return false;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 报表查询需间隔30秒，未搜寻到数据需间隔10秒。
     * 如欲查询全部用户就不用输入user，会以时间区间进行呈现。
     * 如果报表是要搜寻到起始时间到现在，endTime即可以不用代值，只需带startTime
     * @param user 帐号
     * @param startTime 例:20170809130500 (2017年08月09日 13点05分00秒)
     * @param endTime 	例:20170809130600 (2017年08月09日 13点06分00秒)
     * @param syslang 0:中文, 1:英文(非必要)
     * @param timetype 0:抓下注时间, 1:抓结算时间
     * @param datatype 0:输赢报表, 1:小费报表, 2:全部
     * @param gameno1 期数
     * @param gameno2 局号 (非必要)
     * @return
     * @throws Exception
     */
    public String getDateTimeReport(String user, String startTime, String endTime, Integer syslang, Integer timetype, Integer datatype, Integer gameno1, Integer gameno2) throws Exception {
        String cmd = "GetDateTimeReport";
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        if (!ObjectUtils.isEmpty(user)) {
            params.put("user", user);
        }
        params.put("startTime", startTime);
        if(!ObjectUtils.isEmpty(endTime)){
            params.put("endTime", endTime);
        }
        params.put("timestamp", getTimestamp());
        params.put("timetype", timetype);
        params.put("datatype", datatype);
        if (syslang != null) {
            params.put("syslang", syslang);
        }
        if (gameno1 != null) {
            params.put("gameno1", gameno1);
        }
        if (gameno2 != null) {
            params.put("gameno2", gameno2);
        }
        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        if (entity.getErrorCode() == 0) {
            return String.valueOf(entity.getResult());
        }else if(entity.getErrorCode() == 107){
            return "notData";
        }
        throw new Exception(String.valueOf(entity));
    }

    /**
     * 交易记录
     * 因网络环境问题(封包遗失，封包阻塞)，或其他不可违之因素可能在进行加扣点时导致失败，而贵公司有写入此单号，故做此功能以便查寻争议的单据。
     * 使用此功能orderid 与 order 非必要代入，如只有带入user必须带入startTime与endTime,否则只可查寻一小时内该用户的交易纪录，如果代入orderid与order则不受时间限制。
     * @param user 帐号
     * @param orderId 三方订单号
     * @param order 我放订单号
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public ResponseEntity getMemberTradeReport(String user, String orderId,String order,String startTime, String endTime,Integer syslang){
        String cmd = "GetMemberTradeReport";
        Map<String, Object> params = new HashMap<>();
        params.put("cmd", cmd);
        params.put("vendorId", vendorId);
        params.put("signature", signature);
        if (!ObjectUtils.isEmpty(user)) {
            params.put("user", user);
        }
        if (!ObjectUtils.isEmpty(orderId)) {
            params.put("orderid", orderId);
        }
        if (!ObjectUtils.isEmpty(order)) {
            params.put("order", order);
        }
        if(!ObjectUtils.isEmpty(startTime)){
            params.put("startTime", startTime);
        }
        if(!ObjectUtils.isEmpty(endTime)){
            params.put("endTime", endTime);
        }
        params.put("timestamp", getTimestamp());
        if (syslang != null) {
            params.put("syslang", syslang);
        }
        String s = HttpClient4Util.doPost(url, params);
        if (CommonUtil.checkNull(s)) {
            return null;
        }
        ResponseEntity entity = entity(s);
        return entity;
    }

    private Integer getTimestamp() {
        return Integer.parseInt(System.currentTimeMillis() / 1000 + "");
    }

    private ResponseEntity entity(String s) {
        if (s == null) {
            return null;
        }
        try {
            System.out.println(URLDecoder.decode(s, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject =null;
        try {
            jsonObject = JSONObject.parseObject(s);
        }catch (Exception e){
            ResponseEntity entity = new ResponseEntity();
            entity.setErrorCode(500);
            entity.setErrorMessage("远程请求WM异常,请重新操作");
            return entity;
        }
        Integer code = jsonObject.getInteger("errorCode");
        String messgae = jsonObject.getString("errorMessage");
        String result = jsonObject.getString("result");

        ResponseEntity entity = new ResponseEntity();
        entity.setErrorCode(code);
        entity.setErrorMessage(messgae);
        entity.setResult(result);
        return entity;
    }


    @Data
    public static class ResponseEntity {
        private Integer errorCode;
        private String errorMessage;
        private Object result;
    }

}

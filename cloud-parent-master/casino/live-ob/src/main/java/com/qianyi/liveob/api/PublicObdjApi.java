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

@Component
@Slf4j
public class PublicObdjApi {

    @Value("${project.obdj.merchant:null}")
    private String merchant;
    @Value("${project.obdj.secretKey:null}")
    private String secretKey;
    @Value("${project.obdj.currencyCode:null}")
    private String currencyCode;
    @Value("${project.obdj.tester:null}")
    private String tester;
    @Value("${project.obdj.apiUrl:null}")
    private String apiUrl;
    @Value("${project.obdj.recordUrl:null}")
    private String recordUrl;

    public static final String STATUS_TRUE = "true";
    public static final String STATUS_FALSE = "false";

    /**
     * 注册
     *
     * @param username 用户名（2-15位）
     * @param password 用户密码 （6-30位）
     * @return
     */
    public boolean register(String username, String password) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchant", merchant);
        treeMap.put("key", secretKey);
        treeMap.put("currency_code", currencyCode);
        treeMap.put("username", username);
        treeMap.put("password", password);
        treeMap.put("tester", tester);
        treeMap.put("time", System.currentTimeMillis() / 1000);
        String sign = GenerateSignUtil.getMd5Sign(treeMap);
        treeMap.put("sign", sign);
        String splicingParams = GenerateSignUtil.getParams(treeMap);
        log.info("OB电竞创建玩家账号参数{}", splicingParams);
        String url = apiUrl + "/api/member/register?" + splicingParams;
        String result = HttpClient4Util.doGet(url);
        log.info("OB电竞创建玩家账号结果{}", result);
        ResponseEntity entity = entity(result);
        if (entity == null) {
            log.error("OB电竞创建玩家账号出错,远程请求异常");
        }
        if (STATUS_TRUE.equals(entity.getStatus())) {
            return true;
        }
        log.error("OB电竞创建玩家账号出错{}", entity.getData());
        return false;
    }

    /**
     * 登录
     *
     * @param username  用户名
     * @param password  密码
     * @param client_ip 客户端ip ,传递ip时需要将ipv4 转换为long类型的数字
     * @return
     */
    public ResponseEntity login(String username, String password, String client_ip) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchant", merchant);
        treeMap.put("key", secretKey);
        treeMap.put("username", username);
        treeMap.put("password", password);
        treeMap.put("client_ip", client_ip);
        treeMap.put("time", System.currentTimeMillis() / 1000);
        String sign = GenerateSignUtil.getMd5Sign(treeMap);
        treeMap.put("sign", sign);
        String splicingParams = GenerateSignUtil.getParams(treeMap);
        log.info("OB电竞玩家登录参数{}", splicingParams);
        String url = apiUrl + "/api/v2/member/login?" + splicingParams;
        String result = HttpClient4Util.doGet(url);
        log.info("OB电竞玩家登录结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 玩家余额获取
     *
     * @param username
     * @return
     */
    public ResponseEntity getBalance(String username) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchant", merchant);
        treeMap.put("key", secretKey);
        treeMap.put("username", username);
        treeMap.put("time", System.currentTimeMillis() / 1000);
        String sign = GenerateSignUtil.getMd5Sign(treeMap);
        treeMap.put("sign", sign);
        String splicingParams = GenerateSignUtil.getParams(treeMap);
        log.info("OB电竞玩家获取余额参数{}", splicingParams);
        String url = apiUrl + "/api/fund/getBalance?" + splicingParams;
        String result = HttpClient4Util.doGet(url);
        log.info("OB电竞玩家获取余额结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 玩家资金转入/转出
     *
     * @param username   用户名（2-15位）
     * @param type       转账类型( 1-资金转入 2-资金转出)
     * @param amount     转账金额( = 0.01元) (单位：元) (￥)
     * @param merOrderId 转账订单号（20~32位）
     * @return
     */
    public ResponseEntity transfer(String username, Integer type, BigDecimal amount, String merOrderId) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchant", merchant);
        treeMap.put("key", secretKey);
        treeMap.put("currency_code", currencyCode);
        treeMap.put("username", username);
        treeMap.put("type", type);
        treeMap.put("amount", amount);
        treeMap.put("merOrderId", merOrderId);
        treeMap.put("time", System.currentTimeMillis() / 1000);
        String sign = GenerateSignUtil.getMd5Sign(treeMap);
        treeMap.put("sign", sign);
        String splicingParams = GenerateSignUtil.getParams(treeMap);
        log.info("OB电竞玩家资金转入/转出参数{}", splicingParams);
        String url = apiUrl + "/api/fund/transfer?" + splicingParams;
        String result = HttpClient4Util.doGet(url);
        log.info("OB电竞玩家资金转入/转出结果{}", result);
        ResponseEntity entity = entity(result);
        return entity;
    }

    /**
     * 1. 只能查询当前时间30天前至当前时间区间
     * 2. 查询截至时间为当前时间5分钟前
     * 3. 每次查询时间区间为30分钟以内
     * 玩家注单拉取
     *
     * @param startTime    指定拉取注单的时间范 围-起始时间。 注：时 间为北京时间，精确到秒
     * @param endTime      指定拉取注单的时间范 围-结束时间。 注：时 间为北京时间，精确到秒
     * @param lastOrderId 与page_size一起使 用，表示从 last_order_id开始拉 取page_size条数据。 值为上次拉取返回的 last_order_id。数据 拉取完时， last_order_id返回 0。第一次拉取时可传0
     * @param pageSize     页数量（1000 - 10000）
     * @param agency     总商户：true，站点商 户：false
     * @param currency_code 币种编码 1=人民币
     *                      sign 将以上参数按照参数名的 首字母自然顺序进行排 序，（如果首字母相同则 对比下一个字母，以此类 推）（在参数中加入密钥 key 一起参与排序）把 排序后的参数按照 key=value的方式拼接 为Md5key字符串。 Md5加密得到密文。再在 密文中加入干扰值。
     * @return
     */
    public String queryScroll(Long startTime, Long endTime, Long lastOrderId, Integer pageSize, String agency) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        treeMap.put("merchant", merchant);
        treeMap.put("key", secretKey);
        treeMap.put("currency_code", currencyCode);
        treeMap.put("start_time", startTime);
        treeMap.put("end_time", endTime);
        treeMap.put("last_order_id", lastOrderId);
        treeMap.put("page_size", pageSize);
        treeMap.put("agency", agency);
//        treeMap.put("time", System.currentTimeMillis() / 1000);
        String MD5Sign = GenerateSignUtil.getMd5Sign(treeMap);
        //干扰值填充说明：生成的 MD5 值，需要 首尾 及第 9 个和第 17 个字符后的位置分别插入 2 个随机的字符（数字或字母大小写）
        StringBuffer sign = new StringBuffer(MD5Sign);
        sign.insert(0, GenerateSignUtil.getTwoRandom()).insert(11, GenerateSignUtil.getTwoRandom())
                .insert(21, GenerateSignUtil.getTwoRandom()).insert(38, GenerateSignUtil.getTwoRandom());
        treeMap.put("sign", sign.toString());
        String splicingParams = GenerateSignUtil.getParams(treeMap);
        log.info("OB电竞玩家注单拉取参数{}", splicingParams);
        String url = recordUrl + "/v2/pull/order/queryScroll?" + splicingParams;
        String result = HttpClient4Util.doGet(url);
        log.info("OB电竞玩家注单拉取结果{}", result);
        return result;
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
            log.error("解析OB电竞数据时出错，msg={}", e.getMessage());
            entity.setStatus(STATUS_FALSE);
            entity.setData("远程请求OB电竞异常");
            return entity;
        }
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        entity.setStatus(jsonObject.getString("status"));
        entity.setData(jsonObject.getString("data"));
        return entity;
    }

    @Data
    public static class ResponseEntity {

        private String status;

        private String data;
    }
}

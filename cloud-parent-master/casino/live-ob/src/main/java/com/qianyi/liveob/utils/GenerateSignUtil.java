package com.qianyi.liveob.utils;

import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 校验签名工具类
 */
public class GenerateSignUtil {

    /**
     * 生成sign
     * @param treeMap
     * @return
     */
    public static String getMd5Sign(TreeMap<String, Object> treeMap) {
        String signatureKey = "";
        for (Map.Entry entry : treeMap.entrySet()) {
            String name = entry.getKey().toString();
            if (ObjectUtils.isEmpty(entry.getValue())) {
                signatureKey = signatureKey + name + "=" + "&";
            } else {
                String value = entry.getValue().toString();
                signatureKey = signatureKey + name + "=" + value + "&";
            }
        }
        if (!ObjectUtils.isEmpty(signatureKey)) {
            signatureKey = signatureKey.substring(0, signatureKey.length() - 1);
        }
        String md5Val = DigestUtils.md5DigestAsHex(signatureKey.getBytes());
        return md5Val;
    }

    public static String getParams(TreeMap<String, Object> treeMap) {
        String params = "";
        for (Map.Entry entry : treeMap.entrySet()) {
            String name = entry.getKey().toString();
            if ("key".equals(name)){
                continue;
            }
            if (ObjectUtils.isEmpty(entry.getValue())) {
                params = params + name + "=" + "&";
            } else {
                String value = entry.getValue().toString();
                params = params + name + "=" + value + "&";
            }
        }
        if (!ObjectUtils.isEmpty(params)) {
            params = params.substring(0, params.length() - 1);
        }
        return params;
    }


    public static String getObtyMd5Sign(Object... params) {
        String signatureKey = "";
        for (Object param:params){
            signatureKey=signatureKey+param+"&";
        }
        if (!ObjectUtils.isEmpty(signatureKey)) {
            signatureKey = signatureKey.substring(0, signatureKey.length() - 1);
        }
        String md5Val = DigestUtils.md5DigestAsHex(signatureKey.getBytes());
        return md5Val;
    }


    public static String getObtyMd5Sign(String sign, String key) {
        String signatureKey = sign + "&" + key;
        String md5Val = DigestUtils.md5DigestAsHex(signatureKey.getBytes());
        return md5Val;
    }

    /**
     * 获取两位随机数
     * @return
     */
    public static Integer getTwoRandom() {
        Random random = new Random();
        int num = 10 + random.nextInt(90);
        return num;
    }
}

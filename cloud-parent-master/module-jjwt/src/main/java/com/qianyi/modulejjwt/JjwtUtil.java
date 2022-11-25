package com.qianyi.modulejjwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;
@Slf4j
@Component
public class JjwtUtil {

    private static String secrect;

    @Value("${project.jwtsecrect:fda#$&%$3t55v785A45DF$^&#*JGRstTRG}")
    private void setSecrect(String secrect) {
        this.secrect = secrect;
    }
    public final static long ttl = 5 * 60 * 1000;
//    private final static long ttl = 24 * 60 * 60 * 1000;
    private final static Long refresh_ttl = 30 * 60L;//秒

    /**
     *  iss，可能会重复。约定按项目名称传入
     * @param subject
     * @param iss
     * @return
     */
    public static String generic(Subject subject,String iss) {

        return genericJwt(subject, ttl,iss);
    }

    /**
     * 生成JWT令牌
     *
     * @return
     */
    private static String genericJwt(Subject subject, long ttl,String iss) {
        if (ObjectUtils.isEmpty(subject) || ObjectUtils.isEmpty(subject.getUserId())
                || ObjectUtils.isEmpty(subject.getBcryptPassword())||ObjectUtils.isEmpty(iss)) {
            return null;
        }

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        String sub = JSON.toJSONString(subject);
        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID() + "")
                // 头部
                .setHeaderParam("typ", "JWT")
                .setSubject(sub)
                //用于设置签发时间
                .setIssuer(iss)
                .setIssuedAt(now)
                .setExpiration(new Date(nowMillis + ttl))
                //用于设置签名秘钥
                .signWith(SignatureAlgorithm.HS256, secrect);
        return builder.compact();
    }

    /**
     * 解析TOKEN
     *
     * @param token
     * @return
     */
    public static Subject parse(String token, String iss) {
        if (ObjectUtils.isEmpty(token) || ObjectUtils.isEmpty(iss)) {
            return null;
        }
        try {
            Claims body = Jwts.parser()
                    // 验证签发者字段iss 必须是 大山
                    .require("iss", iss)
                    .setSigningKey(secrect)
                    .parseClaimsJws(token)
                    .getBody();
            String json = body.getSubject();
            if (ObjectUtils.isEmpty(json)) {
                return null;
            }

            Subject subject = JSON.parseObject(json, Subject.class);
            return subject;

        } catch (ExpiredJwtException e) {
//            log.error("token={},iss={} 已过期，msg={}", token, iss, e.getMessage());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean check(String token,String iss) {

        try {
            Subject subject = parse(token,iss);
            if (ObjectUtils.isEmpty(subject)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String refreshToken(String token, String bcryptPassword,String iss) {
        return refreshToken(token, bcryptPassword, refresh_ttl,iss);
    }

    /**
     *
     * @param token
     * @param bcryptPassword 用户密码
     * @param refresh_ttl 刷新时间：秒
     * @param iss 模块标识
     * @return
     */
    public static String refreshToken(String token, String bcryptPassword, Long refresh_ttl,String iss) {
        if (ObjectUtils.isEmpty(token) || ObjectUtils.isEmpty(bcryptPassword)||ObjectUtils.isEmpty(iss)) {
            return null;
        }
        try {
            Long exp = getExp(token);
            if (exp == null) {
                return null;
            }
            String tolkenIss = getIss(token);
            if (!(iss.equals(tolkenIss))) {
                return null;
            }

            Subject subject = getSubject(token);
            if (subject == null) {
                return null;
            }

            String bcrypt = subject.getBcryptPassword();
            if (!(bcryptPassword.equals(bcrypt))) {
                return null;
            }

            Long now = System.currentTimeMillis();
            Long diff = now / 1000 - exp;
            if (diff > refresh_ttl) {
                return null;
            }

            return generic(subject,iss);

        } catch (Exception e) {
            return null;
        }
    }

    private static Long getExp(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            return jsonObject.getLong("exp");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Subject getSubject(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            JSONObject sub = jsonObject.getJSONObject("sub");
            Subject subject = new Subject();
            subject.setUserId(sub.getString("userId"));
            subject.setBcryptPassword(sub.getString("bcryptPassword"));
            return subject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getIss(String token) {

        try {
            JSONObject jsonObject = decodeBase64(token);
            return jsonObject.getString("iss");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject decodeBase64(String token) throws Exception {
        if (token == null) {
            return null;
        }

        String[] split = token.split("\\.");
        if (split.length < 1) {
            return null;
        }
        String tokenBody = split[1];
        byte[] bytes = Base64.decodeBase64(tokenBody);
        String decode = new String(bytes, "utf-8");
        JSONObject jsonObject = JSONObject.parseObject(decode);
        return jsonObject;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Subject subject = new Subject();
        subject.setUserId("1");
        subject.setBcryptPassword("aadaa");

        String token = generic(subject,"dashan");
        System.out.println(token);
//        String parse = parse(token);
//        System.out.println(parse);
//
//        boolean check = check(token);
//        System.out.println(check);
//
//        String s = refreshToken(token, subject.getBcryptPassword());
//        System.out.println(s);

//        Long ext = JjwtUtil.getExp(token);
//        System.out.println("======:" + ext);
    }


    @Data
    public static class Subject {

        private String userId;
        private String bcryptPassword;
    }

    @Data
    public static class Token {

        private String oldToken;
        private String newToken;
    }

}


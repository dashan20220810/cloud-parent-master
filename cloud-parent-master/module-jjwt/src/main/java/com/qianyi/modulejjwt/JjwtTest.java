package com.qianyi.modulejjwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JjwtTest {

    public static void main(String[] args) {
        JwtBuilder builder = Jwts.builder().setId("dashan")
                .setSubject("大山")
                //用于设置签发时间
                .setIssuedAt(new Date())
                //用于设置签名秘钥
                .signWith(SignatureAlgorithm.HS256, "dashan");
        System.out.println(builder.compact());
    }
}

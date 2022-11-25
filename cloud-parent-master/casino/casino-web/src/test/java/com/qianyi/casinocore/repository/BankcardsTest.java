package com.qianyi.casinocore.repository;

import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulejjwt.JjwtUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

public class BankcardsTest {

    @Test
    public void should_test_has_length(){

        boolean hasLen = StringUtils.hasLength("11111");

        Assert.assertEquals(true,hasLen);

        hasLen = StringUtils.hasText("");
        Assert.assertEquals(false,hasLen);

        hasLen = StringUtils.hasText(null);
        Assert.assertEquals(false,hasLen);

        hasLen = StringUtils.hasLength(null);
        Assert.assertEquals(false,hasLen);

        System.out.println(hasLen);

    }

    @Test
    public void should_compare_pass(){
        System.out.println(CasinoWebUtil.checkBcrypt("123456", "$2a$10$o5kFtteqcVrZdbgEGQw8cu8m9CP3C4XHYSIGaz4LrshAoBqB5Zkme"));
    }

    @Test
    public void should_token_parse_value(){
        String jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2ZDA3OWM2OC0zMGU5LTQyZDAtOWZjMS1iYmZlNDM4OTJlYzYiLCJzdWIiOiIxIiwiaXNzIjoiZGFzaGFuIiwiaWF0IjoxNjMxMzY4Njk3LCJleHAiOjE2MzE0NTUwOTd9.dlqrHxQp84FWDPbn5sgQcWxbheBQSgGwgQWfdgGc3rs";
        System.out.println(JjwtUtil.parse(jwtToken, Constants.CASINO_WEB));
    }
}

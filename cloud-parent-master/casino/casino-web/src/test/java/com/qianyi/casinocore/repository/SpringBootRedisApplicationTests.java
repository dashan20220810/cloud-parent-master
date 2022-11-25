package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootRedisApplicationTests {

//    @Autowired
//    private RedisTemplate<String,String> strRedisTemplate;
//
//    @Autowired
//    private RedisTemplate<String, Serializable> serializableRedisTemplate;
//
//    @Test
//    public void should_write_string_test(){
//        strRedisTemplate.opsForValue().set("test","hello");
//    }
//
//    @Test
//    public void should_write_object_test(){
//        User user = new User();
//        user.setAccount("test");
//        user.setId(1l);
//        user.setPhone("178936633636");
//        serializableRedisTemplate.opsForValue().set("user",user);
//    }
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringCacheTest {

    @Autowired
    UserService userService;


    @Test
    public void should_cache_user(){
        User user = userService.findById(1l);
//        userService.findById(2l);
        log.info("{}",user);
    }

    @Test
    public void should_change_cache_user(){
        User user = userService.findById(1l);
        user.setLanguage(1);
        userService.save(user);
        log.info("{}",user);
    }
}

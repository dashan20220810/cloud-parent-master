package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service("userDetailsService")
public class SpringSecurityUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        User user = userService.findByAccount(userName);
        if(user != null){
            return user;
        }
        log.info("User is {}",user);
        throw new UsernameNotFoundException("帐号或密码错误");
    }

    public UserDetails getUserDetaisByUserId(Long userid){
        User user = userService.findById(userid);
        if(user == null){
            throw new BadCredentialsException("TOKEN已过期，请重新登录!");
        }
        return user;
    }
}

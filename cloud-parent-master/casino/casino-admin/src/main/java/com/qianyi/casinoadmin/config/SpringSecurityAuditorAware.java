package com.qianyi.casinoadmin.config;

import com.qianyi.casinoadmin.util.LoginUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null||authentication.getPrincipal().toString().equals("anonymousUser")) {
            return Optional.empty();
        }
        Long authId= LoginUtil.getLoginUserId();
       if(authId==null){
           return Optional.empty();
       }
        return Optional.of(authId+"");
    }

    /**
     * 配置地址栏不能识别 // 的情况
     * @return
     */
    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        //此处可添加别的规则,目前只设置 允许多 //
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }
}

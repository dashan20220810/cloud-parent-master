package com.qianyi.casinoweb.config;

import com.qianyi.casinoweb.util.CasinoWebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null||authentication.getPrincipal().toString().equals("anonymousUser")) {
            return Optional.empty();
        }
        log.info("authentication princial {}", authentication.getPrincipal().toString());
        Long authId = CasinoWebUtil.getAuthId();
        return Optional.of(authId + "");
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

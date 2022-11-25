package com.qianyi.casinoproxy.config.security.filter;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.SpringSecurityUserDetailsService;
import com.qianyi.casinoproxy.config.security.util.Constants;
import com.qianyi.casinoproxy.config.security.util.MultiReadHttpServletRequest;
import com.qianyi.casinoproxy.config.security.util.MultiReadHttpServletResponse;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Slf4j
@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;

    private ProxyUserService proxyUserService;

    protected MyAuthenticationFilter(ProxyUserService proxyUserService) {
        this.proxyUserService = proxyUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("请求头类型： " + request.getContentType());
        if ((request.getContentType() == null && request.getContentLength() > 0) || (request.getContentType() != null && !request.getContentType().contains(Constants.REQUEST_HEADERS_CONTENT_TYPE))) {
            filterChain.doFilter(request, response);
            return;
        }
        log.debug("进行request，respone的转换");
        MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest(request);
        MultiReadHttpServletResponse wrappedResponse = new MultiReadHttpServletResponse(response);
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            // 记录请求的消息体
            logRequestBody(wrappedRequest);

            // 前后端分离情况下，前端登录后将token储存在cookie中，每次访问接口时通过token去拿用户权限
            String jwtToken = wrappedRequest.getHeader(Constants.REQUEST_HEADER);
//            log.info("后台检查令牌:{}", jwtToken);
            if (jwtToken != null) {
                Long userId = CasinoProxyUtil.getAuthId();
//                log.info("userid is {}",userId);
                if(userId !=null){
                    ProxyUser proxyUser = proxyUserService.findById(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(proxyUser, null, proxyUser.getAuthorities());
                    // 全局注入角色权限信息和登录用户基本信息
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (ExpiredJwtException e) {
            // jwt令牌过期
            log.error("{}",e);
            SecurityContextHolder.clearContext();
            this.authenticationEntryPoint.commence(wrappedRequest, response, null);
        } catch (AuthenticationException e) {
            log.error("{}",e);
            SecurityContextHolder.clearContext();
            this.authenticationEntryPoint.commence(wrappedRequest, response, e);
        } finally {
            stopWatch.stop();
            long usedTimes = stopWatch.getTotalTimeMillis();
            // 记录响应的消息体
            logResponseBody(wrappedRequest, wrappedResponse, usedTimes);
        }

    }

    private String logRequestBody(MultiReadHttpServletRequest request) {
        //日志打印traceId，同一次请求的traceId相同，方便定位日志
        ThreadContext.put("traceId", UUID.randomUUID().toString().replaceAll("-",""));
        MultiReadHttpServletRequest wrapper = request;
        if (wrapper != null) {
            try {
                String bodyJson = wrapper.getBodyJsonStrByJson(request);
                String url = wrapper.getRequestURI().replace("//", "/");
                log.debug("-------------------------------- 请求url: " + url + " --------------------------------");
                Constants.URL_MAPPING_MAP.put(url, url);
                log.debug("`{}` 接收到的参数: {}", url, bodyJson);
                return bodyJson;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void logResponseBody(MultiReadHttpServletRequest request, MultiReadHttpServletResponse response, long useTime) {
        MultiReadHttpServletResponse wrapper = response;
        if (wrapper != null) {
            byte[] buf = wrapper.getBody();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }
                log.debug("`{}`  耗时:{}ms  返回的参数: {}", Constants.URL_MAPPING_MAP.get(request.getRequestURI()), useTime, payload);
            }
        }
    }

}

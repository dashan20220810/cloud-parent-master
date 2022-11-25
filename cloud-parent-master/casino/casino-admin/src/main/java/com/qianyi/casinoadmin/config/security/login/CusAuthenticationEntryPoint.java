package com.qianyi.casinoadmin.config.security.login;

import com.qianyi.casinoadmin.config.security.util.ApiResult;
import com.qianyi.casinoadmin.config.security.util.ResponseUtils;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CusAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        if(e!=null){
            ResponseUtils.out(httpServletResponse, ResponseUtil.authenticationNopass());
//            ResponseUtils.out(httpServletResponse, ApiResult.expired(e.getMessage()));
        }else{
            ResponseUtils.out(httpServletResponse, ApiResult.expired("jwtToken过期!"));
        }
    }
}

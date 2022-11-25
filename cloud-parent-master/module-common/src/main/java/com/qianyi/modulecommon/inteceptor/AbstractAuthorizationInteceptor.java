package com.qianyi.modulecommon.inteceptor;

import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public abstract class AbstractAuthorizationInteceptor  implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //不是映射到方法不用拦截
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        NoAuthorization noAuthorization = method.getAnnotation(NoAuthorization.class);
        NoAuthentication annotation = method.getAnnotation(NoAuthentication.class);
        if (annotation != null) {
            return true;
        }
        //临时调试
        if (noAuthorization == null) {
            if(discharged()){
                return true;
            }
            if(hasBan()){
                String url=request.getServletPath();
                if(url.contains("authenticationBan")){
                    return true;
                }
                response.sendRedirect(request.getContextPath()+"/authenticationNopass");
                return false;
            }
            String url=request.getServletPath();

            if (!hasPermission(request)) {
                response.sendRedirect(request.getContextPath()+"/authorizationNopass");
                return false;
            }
        }
        return true;
    }

    protected abstract boolean hasBan();

    protected abstract boolean discharged();

    public abstract boolean hasPermission(HttpServletRequest request);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

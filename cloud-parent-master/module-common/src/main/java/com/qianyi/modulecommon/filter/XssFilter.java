package com.qianyi.modulecommon.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class XssFilter implements Filter {

    List<String> passList = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setPassList(passList);
    }

    /**
     * 不需要过滤的白名单地址
     * @param passList
     */
    public abstract void setPassList( List<String> passList);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request =  (HttpServletRequest)servletRequest;
        String requestURI = request.getRequestURI();

        if(!passList.contains(requestURI)){
            filterChain.doFilter(new XSSRequestWrapper(request) , servletResponse);
        }else{
            filterChain.doFilter(servletRequest,servletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private class XSSRequestWrapper extends HttpServletRequestWrapper {

        public XSSRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String[] getParameterValues(String name) {
            //获取所有参数值的集合
            String[] results = this.getParameterMap().get(name);
            if (results != null && results.length > 0) {
                int length = results.length;
                for (int i = 0; i < length; i++) {
                    //过滤参数值
                    results[i] = HtmlUtils.htmlEscape(results[i]);
                }
                return results;
            }
            return null;
        }
    }

}

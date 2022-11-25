package com.qianyi.casinoadmin.config;

import com.qianyi.casinoadmin.inteceptor.AuthenticationInteceptor;
import com.qianyi.casinoadmin.inteceptor.AuthorizationInteceptor;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GloabConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //添加映射路径
        registry.addMapping("/**")
                //是否发送Cookie
                .allowCredentials(true)
                //设置放行哪些原始域   SpringBoot2.4.4下低版本使用.allowedOrigins("*")
                .allowedOriginPatterns("*")
                //放行哪些请求方式
                .allowedMethods(new String[]{"GET", "POST", "PUT", "DELETE"})
                //.allowedMethods("*") //或者放行全部
                //放行哪些原始请求头部信息
                .allowedHeaders("*")
                //暴露哪些原始请求头部信息
                .exposedHeaders("*");
    }

    @Autowired
    AuthenticationInteceptor authenticationInteceptor;


    @Autowired
    AuthorizationInteceptor authorizationInteceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInteceptor).addPathPatterns("/**")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/swagger**/**")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/v3/**")
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("/public/**");
        registry.addInterceptor(authorizationInteceptor).addPathPatterns("/**")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/swagger**/**")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/v3/**")
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("/public/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = CommonUtil.getLocalPicPath();
        //本地资源映射
        registry.addResourceHandler("/public/img/**").addResourceLocations("file:" + path);
    }
}

package com.qianyi.casinoadmin.util;

import com.qianyi.modulecommon.Constants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class HttpContextUtils {

    public static HttpServletRequest getHttpServletRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getHeader(String name){
        try {
            HttpServletRequest request = getHttpServletRequest();
            return request.getHeader(name);
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 这个方法移动到你所需要用到的地方，或者就在你这里调用
     * @return
     */
    private String getLanguageHeader(){
        String language = this.getHeader("language");
        if(LoginUtil.checkNull(language)){
            language = Constants.LANGUAGE_CN;
        }
        return language;
    }
}

package com.qianyi.modulecommon.util;


import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.config.LocaleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * 国际化工具类
 */
@Component
@Slf4j
public class MessageUtil {

    @Autowired
    private MessageSource messageSource;

    /**
     * 获取单个国际化翻译值
     */
    public String get(String msgKey) {
        try {
            Locale locale = getLocale();
//            Locale locale = LocaleContextHolder.getLocale();
            //以中文为key,中文翻译可以不配，直接取key
            if (locale.equals(Locale.CHINA)) {
                return msgKey;
            }
            String message = messageSource.getMessage(msgKey, null, locale);
            return message;
        } catch (Exception e) {
//            log.error("多语言获取失败，key:{}", msgKey);
            //e.printStackTrace();
            return msgKey;
        }
    }

    private Locale getLocale() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String language = request.getHeader(Constants.LANGUAGE);
        //默认英文
        Locale locale = LocaleConfig.en_US;
        if (LocaleConfig.zh_CN.toString().equals(language)) {
            locale = LocaleConfig.zh_CN;
        } else if (LocaleConfig.km_KH.toString().equals(language)) {
            locale = LocaleConfig.km_KH;
        } else if (LocaleConfig.as_MY.toString().equals(language)) {
            locale = LocaleConfig.as_MY;
        } else if (LocaleConfig.th_TH.toString().equals(language)) {
            locale = LocaleConfig.th_TH;
        }
        return locale;
    }
}
package com.qianyi.modulecommon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

/**
 * 配置国际化语言
 */
@Configuration
public class LocaleConfig {
    //英文
    public static final Locale en_US = Locale.US;
    //中文
    public static final Locale zh_CN = Locale.CHINA;
    //柬埔寨语
    public static final Locale km_KH = new Locale("km", "KH");
    //马来西亚语
    public static final Locale as_MY = new Locale("as", "MY");
    //泰语
    public static final Locale th_TH = new Locale("th", "TH");

    @Bean(name = "messageSource")
    public ResourceBundleMessageSource getMessageSource() throws Exception {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding("GBK");
        resourceBundleMessageSource.setBasenames("static/i18n/messages");
        return resourceBundleMessageSource;
    }
}

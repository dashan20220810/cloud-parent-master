package com.qianyi.liveob.constants;

import com.qianyi.modulecommon.config.LocaleConfig;
import org.springframework.util.ObjectUtils;

public enum LanguageEnum {

    en_US("en", "英文", LocaleConfig.en_US.toString()),
    zh_CN("cn", "中文", LocaleConfig.zh_CN.toString()),
    ;
    private String code;
    private String name;
    private String systemCode;

    LanguageEnum(String code, String name, String systemCode) {
        this.code = code;
        this.name = name;
        this.systemCode = systemCode;
    }

    LanguageEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public static String getLanguageCode(String language) {
        for (com.qianyi.liveob.constants.LanguageEnum languageEnum : values()) {
            if (!ObjectUtils.isEmpty(language) && language.equals(languageEnum.getSystemCode())) {
                return languageEnum.getCode();
            }
        }
        return en_US.code;
    }
}

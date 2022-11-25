package com.qianyi.livegoldenf.constants;

import com.qianyi.modulecommon.config.LocaleConfig;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.util.Locale;

public enum LanguageEnum {

    en_US("en-US", "English (英文)", LocaleConfig.en_US.toString()),
    zh_CN("zh-CN", "Chinese Simplified(简中)", LocaleConfig.zh_CN.toString()),
    ID("ID", "Indonesian (印尼语)"),
    TH("TH", "Thai (泰语)", LocaleConfig.th_TH.toString()),
    VI("VI", "Vietnamese (越南文)"),
    JA("JA", "Japanese (日文)"),
    KO("KO", "Korean (韩文)"),
    ES("ES", "Spanish（西班牙文"),
    //三方马来西亚代码未MY,实际上显示中文
    MY("en-US", "Malaysia(馬來西亞文)", LocaleConfig.as_MY.toString()),
    TR("TR", "Turkish (土耳其语)"),
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
        for (LanguageEnum languageEnum : LanguageEnum.values()) {
            if (!ObjectUtils.isEmpty(language) && language.equals(languageEnum.getSystemCode())) {
                return languageEnum.getCode();
            }
        }
        return en_US.code;
    }
}

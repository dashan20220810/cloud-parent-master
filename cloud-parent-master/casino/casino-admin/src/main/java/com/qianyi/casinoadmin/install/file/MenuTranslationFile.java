package com.qianyi.casinoadmin.install.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "menu")
@PropertySource("classpath:install/menuTranslation.properties")
@Data
public class MenuTranslationFile {
    private Map<String, String> englishNames;
    private Map<String, String> cambodianNames;
}

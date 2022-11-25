package com.qianyi.casinoadmin.install.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "platform")
@PropertySource("classpath:install/platformConfig.properties")
@Data
public class PlatformConfigFile {
    private BigDecimal clearCodeNum;
    private BigDecimal betRate;
    private BigDecimal chargeMinMoney;
    private BigDecimal chargeMaxMoney;
    private BigDecimal chargeServiceMoney;
    private BigDecimal chargeRate;
    private BigDecimal withdrawMinMoney;
    private BigDecimal withdrawMaxMoney;
    private BigDecimal withdrawServiceMoney;
    private BigDecimal withdrawRate;
    private Integer ipMaxNum;
    private BigDecimal wmMoney;
    private BigDecimal wmMoneyWarning;
    private BigDecimal firstCommission;
    private BigDecimal secondCommission;
    private BigDecimal thirdCommission;
    private String domainNameConfiguration;
    private String webConfiguration;
    private Integer registerSwitch;
    private String proxyConfiguration;
    private BigDecimal sendMessageWarning;
    private Integer directlyUnderTheLower;
    private String companyInviteCode;
    private String customerCode;
    private String uploadUrl;
    private String readUploadUrl;
    private String moneySymbol;
    private Integer peopleProxySwitch;
    private Integer bankcardRealNameSwitch;
    private BigDecimal totalPlatformQuota;
    private Integer verificationCode;
}

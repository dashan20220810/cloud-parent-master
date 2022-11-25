package com.qianyi.casinoadmin.install.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "rebate")
@PropertySource("classpath:install/proxyRebateConfig.properties")
@Data
public class ProxyRebateConfigFile {
    private Integer firstMoney;
    private BigDecimal firstMoneyLine;
    private BigDecimal firstProfit;
    private Integer secondMoney;
    private BigDecimal secondMoneyLine;
    private BigDecimal secondProfit;
    private Integer thirdMoney;
    private BigDecimal thirdMoneyLine;
    private BigDecimal thirdProfit;
    private Integer fourMoney;
    private BigDecimal fourMoneyLine;
    private BigDecimal fourProfit;
    private Integer fiveMoney;
    private BigDecimal fiveMoneyLine;
    private BigDecimal fiveProfit;
    private Integer sixMoney;
    private BigDecimal sixMoneyLine;
    private BigDecimal sixProfit;
    private Integer sevenMoney;
    private BigDecimal sevenMoneyLine;
    private BigDecimal sevenProfit;
    private Integer eightMoney;
    private BigDecimal eightMoneyLine;
    private BigDecimal eightProfit;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("代理佣金配置表")
public class ProxyCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("总代返佣")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal firstCommission = BigDecimal.ZERO;

    @ApiModelProperty("区域代理返佣")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal secondCommission = BigDecimal.ZERO;

    @ApiModelProperty("基层代理返佣")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal thirdCommission = BigDecimal.ZERO;

    @ApiModelProperty("代理ID")
    @Column(unique = true)
    private Long proxyUserId;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;
}

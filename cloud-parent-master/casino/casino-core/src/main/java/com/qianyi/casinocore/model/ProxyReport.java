package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("代理报表")
public class ProxyReport extends BaseEntity{

    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("账号")
    private String account;
    //全部
    @ApiModelProperty("分润")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal allProfitAmount=BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal allBetAmount=BigDecimal.ZERO;
    @ApiModelProperty("团队人数")
    private Integer allGroupNum=0;
    @ApiModelProperty("投注人数")
    private Integer allBetNum=0;
    @ApiModelProperty("充值人数")
    private Integer allChargeNum=0;

    //直属
    @ApiModelProperty("分润")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal directProfitAmount=BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal directBetAmount=BigDecimal.ZERO;
    @ApiModelProperty("团队人数")
    private Integer directGroupNum=0;
    @ApiModelProperty("投注人数")
    private Integer directBetNum=0;
    @ApiModelProperty("充值人数")
    private Integer directChargeNum=0;

    //非直属
    @ApiModelProperty("分润")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal otherProfitAmount=BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal otherBetAmount=BigDecimal.ZERO;
    @ApiModelProperty("团队人数")
    private Integer otherGroupNum=0;
    @ApiModelProperty("投注人数")
    private Integer otherBetNum=0;
    @ApiModelProperty("充值人数")
    private Integer otherChargeNum=0;
}

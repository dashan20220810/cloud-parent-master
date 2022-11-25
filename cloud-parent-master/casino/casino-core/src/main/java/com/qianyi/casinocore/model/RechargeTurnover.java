package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("充值流水记录表")
public class RechargeTurnover extends BaseEntity{

    private Long orderId;
    @ApiModelProperty("客户id")
    private Long userId;
    @ApiModelProperty("订单金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal orderMoney;
    @ApiModelProperty("打码量")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal codeNum;
    @ApiModelProperty("实时打码总量")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal codeNums;
    @ApiModelProperty("打码倍率")
    private Float codeTimes;
    @ApiModelProperty(value = "汇款方式 银行卡充值1  总控上分2  代理上分3 活动赠送4")
    private Integer remitType;
}

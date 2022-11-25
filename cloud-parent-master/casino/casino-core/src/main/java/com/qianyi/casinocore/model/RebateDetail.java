package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("注单返利详情")
@Table(name = "rebate_detail")
public class RebateDetail extends BaseEntity {

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "游戏记录ID")
    private Long gameRecordId;

    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validbet;

    @ApiModelProperty(value = "平台返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal platformRebateRate;

    @ApiModelProperty(value = "用户分成比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal userDivideRate;

    @ApiModelProperty(value = "总返利金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "用户返利金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal userAmount;

    @ApiModelProperty(value = "剩余返利金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal surplusAmount;
}

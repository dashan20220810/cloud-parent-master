package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("返利比例")
@Table(indexes = {@Index(name="identity_index",columnList = "userId",unique=true),@Index(name="identity_index",columnList = "type",unique=true)})
public class RebateConfiguration  extends BaseEntity{


    @ApiModelProperty(value = "WM返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal WMRate;

    @ApiModelProperty(value = "PG返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal PGRate;

    @ApiModelProperty(value = "CQ9返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal CQ9Rate;

    @ApiModelProperty(value = "SABASPORT返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal SABASPORTRate;

    @ApiModelProperty(value = "OBDJ返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal OBDJRate;

    @ApiModelProperty(value = "OBTY返利比例")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal OBTYRate;

    //全局配置 userId=0
    @ApiModelProperty("会员id")
    private Long userId;

    @ApiModelProperty("配置类型 1会员 2代理 0全局")
    private Integer type;
}

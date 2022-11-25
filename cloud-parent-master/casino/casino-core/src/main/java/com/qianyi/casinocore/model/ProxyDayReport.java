package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("代理日报表")
public class ProxyDayReport extends BaseEntity{

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("存款")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal deppositeAmount = BigDecimal.ZERO;
    @ApiModelProperty("有效投注")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal betAmount = BigDecimal.ZERO;
    @ApiModelProperty("分润")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal profitAmount= BigDecimal.ZERO;
    @ApiModelProperty("今日新增")
    private Integer newNum=0;
    @ApiModelProperty("日期")
    private String dayTime;


    @ApiModelProperty("团队业绩流水")
    private BigDecimal groupBeAmount=BigDecimal.ZERO;

}

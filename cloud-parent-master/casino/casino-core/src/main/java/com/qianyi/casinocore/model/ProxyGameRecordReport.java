package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("代理游戏报表")
@Table(indexes = {@Index(columnList = "firstProxy"),
    @Index(columnList = "secondProxy"),@Index(columnList = "thirdProxy"),@Index(name="identity_index",columnList = "proxyGameRecordReportId",unique=true)})
public class ProxyGameRecordReport{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("唯一标识")
    private Long proxyGameRecordReportId;

    @ApiModelProperty(value = "会员id")
    private Long userId;
    /**
     * 投注金额
     */
    @ApiModelProperty(value = "投注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount;
    /**
     * 有效下注
     */
    @ApiModelProperty(value = "有效下注")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal validAmount;

    @ApiModelProperty("投注笔数")
    private Integer bettingNumber;
    /**
     * 输赢金额
     */
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLoss;
    /**
     * 统计时间段
     */
    @ApiModelProperty(value = "统计时间段yyyy-MM-dd(以美东时间为维度统计一天)")
    private String orderTimes;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;
}

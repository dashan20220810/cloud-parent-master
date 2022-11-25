package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("会员游戏报表")
@Table(indexes = {@Index(columnList = "userId"),
    @Index(columnList = "orderTimes"),@Index(columnList = "platform"),@Index(name="identity_index",columnList = "userGameRecordReportId",unique=true)})
public class UserGameRecordReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("唯一标识")
    private Long userGameRecordReportId;

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
    @ApiModelProperty(value = "统计时间段yyyy-MM-dd(wm以美东时间为维度统计一天,其余为北京时间)")
    private String orderTimes;

    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;

}

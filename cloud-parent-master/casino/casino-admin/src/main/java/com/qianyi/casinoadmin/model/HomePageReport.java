package com.qianyi.casinoadmin.model;

import com.qianyi.casinocore.model.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("首页报表")
@Table(indexes = {@Index(columnList = "staticsTimes",unique=true)})
public class HomePageReport extends BaseEntity {

    @ApiModelProperty(value = "汇款金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal chargeAmount;

    @ApiModelProperty(value = "汇款笔数")
    private Integer chargeNums;

    @ApiModelProperty(value = "提款金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "提款笔数")
    private Integer withdrawNums;

    //    @ApiModelProperty(value = "有效下注金额")
    //    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    //    private BigDecimal validbetAmount;
    //
    //    @ApiModelProperty(value = "输赢金额")
    //    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    //    private BigDecimal winLossAmount;

    @ApiModelProperty(value = "洗码金额")
    @Column(columnDefinition = "Decimal(15,6) default '0.00'")
    private BigDecimal washCodeAmount;

    @ApiModelProperty(value = "抽点金额")
    @Column(columnDefinition = "Decimal(15,6) default '0.00'")
    private BigDecimal extractPointsAmount;

    @ApiModelProperty(value = "结算人人代佣金")
    @Column(columnDefinition = "Decimal(15,6) default '0.00'")
    private BigDecimal  shareAmount;

    @ApiModelProperty(value = "发放红利")
    @Column(columnDefinition = "Decimal(15,6) default '0.00'")
    private BigDecimal bonusAmount;

    @ApiModelProperty(value = "充提手续费")
    @Column(columnDefinition = "Decimal(15,6) default '0.00'")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    //    @ApiModelProperty(value = "活跃玩家数")
    //    private Integer activeUsers;

    @ApiModelProperty(value = "新增玩家数")
    private Integer newUsers;

    @ApiModelProperty(value = "统计时间段(日)")
    private String staticsTimes;

    @ApiModelProperty(value = "统计时间段(月)")
    private String staticsMonth ;

    @ApiModelProperty(value = "统计时间段(年)")
    private String staticsYear;
}

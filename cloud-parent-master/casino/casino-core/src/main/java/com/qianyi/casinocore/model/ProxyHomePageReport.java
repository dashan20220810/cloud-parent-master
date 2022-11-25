package com.qianyi.casinocore.model;

import com.qianyi.casinocore.util.CommonConst;
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
@ApiModel("代理首页报表")
@Table(indexes = {@Index(name="identity_index",columnList = "proxyUserId",unique=true),@Index(columnList = "firstProxy"),
        @Index(columnList = "secondProxy"),@Index(name="identity_index",columnList = "staticsTimes",unique=true)})
public class ProxyHomePageReport extends BaseEntity {

    @ApiModelProperty(value = "代理id")
    private Long proxyUserId;

    @ApiModelProperty("代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty(value = "新增基层代理")
    private Integer newThirdProxys = CommonConst.NUMBER_0;

    @ApiModelProperty(value = "新增玩区域代理")
    private Integer newSecondProxys = CommonConst.NUMBER_0;

    @ApiModelProperty(value = "新增玩家数")
    private Integer newUsers;

    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers;

    @ApiModelProperty(value = "统计时间段(日)")
    private String staticsTimes;

    @ApiModelProperty(value = "统计时间段(年)")
    private String staticsYear;

    @ApiModelProperty(value = "统计时间段(月)")
    private String staticsMonth ;

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

    @ApiModelProperty(value = "有效下注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal validbetAmount;

    @ApiModelProperty(value = "输赢金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal winLossAmount;

    public ProxyHomePageReport(Long proxyUserId,String staticsTimes,String staticsMonth,String staticsYear,Long firstProxy,Integer proxyRole){
        this.proxyUserId = proxyUserId;
        this.staticsTimes = staticsTimes;
        this.staticsMonth = staticsMonth;
        this.staticsYear = staticsYear;
        this.firstProxy = firstProxy;
        this.secondProxy = CommonConst.LONG_0;
        this.proxyRole = proxyRole;
        this.activeUsers = CommonConst.NUMBER_0;
        this.chargeAmount = BigDecimal.ZERO;
        this.chargeNums = CommonConst.NUMBER_0;
        this.withdrawMoney = BigDecimal.ZERO;
        this.withdrawNums = CommonConst.NUMBER_0;
        this.validbetAmount = BigDecimal.ZERO;
        this.winLossAmount = BigDecimal.ZERO;
        this.newUsers = CommonConst.NUMBER_0;
    }

    public ProxyHomePageReport(){

    }
}

package com.qianyi.casinoproxy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Data
public class ProxyHomePageReportVo  implements Serializable {

    private static final long serialVersionUID = -6875617745630305179L;
    @ApiModelProperty(value = "代理id")
    private Long proxyUserId;

    @ApiModelProperty(value = "新增基层代理")
    private Integer newThirdProxys;

    @ApiModelProperty(value = "新增玩区域代理")
    private Integer newSecondProxys;

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

    @ApiModelProperty(value = "时间段")
    private String time ;

    @ApiModelProperty(value = "汇款金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal chargeAmount;

    @ApiModelProperty(value = "汇款笔数")
    private Integer chargeNums;

    @ApiModelProperty(value = "提款金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "提款笔数")
    private Integer withdrawNums;

    @ApiModelProperty(value = "有效下注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal validbetAmount;

    @ApiModelProperty(value = "输赢金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal winLossAmount;

    @ApiModelProperty(value = "团队结算佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal groupTotalProfit = BigDecimal.ZERO;

    @ApiModelProperty(value = "本人佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @ApiModelProperty(value = "充投比")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal oddsRatio;

    private Set<Long> userIdSet;

    public ProxyHomePageReportVo(ProxyHomePageReport proxyHomePageReport,Set<Long> userIdSet){
        this.proxyUserId = proxyHomePageReport.getProxyUserId();
        this.newSecondProxys = proxyHomePageReport.getNewSecondProxys();
        this.newThirdProxys = proxyHomePageReport.getNewThirdProxys();
        this.newUsers = proxyHomePageReport.getNewUsers();
        this.userIdSet = userIdSet;
        this.activeUsers = userIdSet == null ? CommonConst.NUMBER_0 : userIdSet.size();
        this.staticsTimes = proxyHomePageReport.getStaticsTimes();
        this.time = proxyHomePageReport.getStaticsTimes();
        this.staticsYear = proxyHomePageReport.getStaticsYear();
        this.staticsMonth = proxyHomePageReport.getStaticsMonth();
        this.chargeAmount = proxyHomePageReport.getChargeAmount();
        this.chargeNums = proxyHomePageReport.getChargeNums();
        this.withdrawMoney = proxyHomePageReport.getWithdrawMoney();
        this.withdrawNums = proxyHomePageReport.getWithdrawNums();
        this.winLossAmount = proxyHomePageReport.getWinLossAmount();
        this.validbetAmount = proxyHomePageReport.getValidbetAmount();
        if (proxyHomePageReport.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || proxyHomePageReport.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            this.oddsRatio = proxyHomePageReport.getChargeAmount();
        }else {
            this.oddsRatio = proxyHomePageReport.getChargeAmount().divide(proxyHomePageReport.getValidbetAmount(),2, RoundingMode.HALF_UP);
        }
    }
    public ProxyHomePageReportVo(ProxyHomePageReport proxyHomePageReport){
        this.proxyUserId = proxyHomePageReport.getProxyUserId();
        this.newSecondProxys = proxyHomePageReport.getNewSecondProxys();
        this.newThirdProxys = proxyHomePageReport.getNewThirdProxys();
        this.newUsers = proxyHomePageReport.getNewUsers();
        this.activeUsers = proxyHomePageReport.getActiveUsers();
        this.staticsTimes = proxyHomePageReport.getStaticsTimes();
        this.time = proxyHomePageReport.getStaticsTimes();
        this.staticsYear = proxyHomePageReport.getStaticsYear();
        this.staticsMonth = proxyHomePageReport.getStaticsMonth();
        this.chargeAmount = proxyHomePageReport.getChargeAmount();
        this.chargeNums = proxyHomePageReport.getChargeNums();
        this.withdrawMoney = proxyHomePageReport.getWithdrawMoney();
        this.withdrawNums = proxyHomePageReport.getWithdrawNums();
        this.winLossAmount = proxyHomePageReport.getWinLossAmount();
        this.validbetAmount = proxyHomePageReport.getValidbetAmount();
        if (proxyHomePageReport.getValidbetAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 || proxyHomePageReport.getChargeAmount().compareTo( BigDecimal.ZERO) == CommonConst.NUMBER_0 ){
            this.oddsRatio = proxyHomePageReport.getChargeAmount();
        }else {
            this.oddsRatio = proxyHomePageReport.getChargeAmount().divide(proxyHomePageReport.getValidbetAmount(),2, RoundingMode.HALF_UP);
        }
    }
    public ProxyHomePageReportVo(){

    }
}

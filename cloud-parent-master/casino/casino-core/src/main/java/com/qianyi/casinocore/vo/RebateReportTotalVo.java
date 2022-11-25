package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class RebateReportTotalVo {

    @ApiModelProperty(value = "num")
    private Integer num;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("bet_amount")
    @ApiModelProperty(value = "投注金额")
    private BigDecimal betAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("win_loss")
    @ApiModelProperty(value = "玩家输赢")
    private BigDecimal winLoss;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("total_rebate")
    @ApiModelProperty(value = "返利总额")
    private BigDecimal totalRebate;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("user_amount")
    @ApiModelProperty(value = "用户返利金额")
    private BigDecimal userAmount;
    //
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("surplus_amount")
    @ApiModelProperty(value = "平台返利金额")
    private BigDecimal surplusAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("service_charge")
    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("avg_benefit")
    @ApiModelProperty(value = "毛利1")
    private BigDecimal avgBenefit;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("total_amount")
    @ApiModelProperty(value = "毛利2")
    private BigDecimal totalAmount;

}

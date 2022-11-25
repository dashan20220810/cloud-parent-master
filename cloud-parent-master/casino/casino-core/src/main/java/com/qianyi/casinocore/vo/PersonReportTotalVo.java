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
public class PersonReportTotalVo {

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
    @JsonProperty("wash_amount")
    @ApiModelProperty(value = "洗码金额")
    private BigDecimal washAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("service_charge")
    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("all_profit_amount")
    @ApiModelProperty(value = "allProfitAmount")
    private BigDecimal allProfitAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("avg_benefit")
    @ApiModelProperty(value = "avgBenefit")
    private BigDecimal avgBenefit;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("total_amount")
    @ApiModelProperty(value = "totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("all_water")
    @ApiModelProperty(value = "贡献代理抽点, 表示该用户，对上级代理贡献的抽点金额")
    private BigDecimal allWater;

}

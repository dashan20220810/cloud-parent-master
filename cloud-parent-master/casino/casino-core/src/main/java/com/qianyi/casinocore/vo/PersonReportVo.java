package com.qianyi.casinocore.vo;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class PersonReportVo {

    @ApiModelProperty(value = "num")
    private Integer num;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("bet_amount")
    @ApiModelProperty(value = "投注金额")
    private BigDecimal betAmount;

    //@JsonProperty("third_proxy")
    @ApiModelProperty(value = "基础id")
    private String thirdProxy;

    public String getThirdProxy(){
        if (StrUtil.isNotBlank(thirdProxyName)) {
            return thirdProxyName;
        }
        return thirdProxy;
    }

    @JsonIgnore
    private String thirdProxyName;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("all_profit_amount")
    @ApiModelProperty(value = "用户输赢金额")
    private BigDecimal allProfitAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("service_charge")
    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("total_amount")
    @ApiModelProperty(value = "总结算(毛利2)")
    private BigDecimal totalAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("avg_benefit")
    @ApiModelProperty(value = "毛利1")
    private BigDecimal avgBenefit;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("win_loss")
    @ApiModelProperty(value = "玩家输赢")
    private BigDecimal winLoss;

    @ApiModelProperty(value = "主键")
    private Long id;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "有效投注")
    private BigDecimal validbet;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @JsonProperty("wash_amount")
    @ApiModelProperty(value = "洗码金额")
    private BigDecimal washAmount;

    @ApiModelProperty(value = "账号")
    private String account;

    @JsonProperty("all_water")
    @ApiModelProperty(value = "贡献代理抽点, 表示该用户，对上级代理贡献的抽点金额")
    private BigDecimal allWater;
}

package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordReportVo implements Serializable {
    private static final long serialVersionUID = -6875674563240305179L;
    @ApiModelProperty(value = "代理账号")
    private String account;
    private Long accountId;
    private Boolean hasChildren;
    @ApiModelProperty("投注笔数")
    private Integer bettingNumber;
    @ApiModelProperty(value = "洗码金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal amount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "下注金额")
    private BigDecimal betAmount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLossAmount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "有效下注")
    private BigDecimal validAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal totalWinLossAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "用户返利金额")
    private BigDecimal userAmount;

    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "代理返利金额")
    private BigDecimal surplusAmount;
}

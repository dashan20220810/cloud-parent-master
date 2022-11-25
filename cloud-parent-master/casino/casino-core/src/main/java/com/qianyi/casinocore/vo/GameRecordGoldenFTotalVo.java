package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordGoldenFTotalVo implements Serializable {

    private static final long serialVersionUID = -694878416362305179L;

    @ApiModelProperty(value = "下注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betAmount = BigDecimal.ZERO;
    @ApiModelProperty(value = "派彩或退回金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal winAmount = BigDecimal.ZERO;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLoss = BigDecimal.ZERO;
}

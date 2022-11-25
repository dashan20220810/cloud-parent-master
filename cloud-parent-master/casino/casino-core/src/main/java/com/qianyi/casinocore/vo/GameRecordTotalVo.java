package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordTotalVo implements Serializable {
    private static final long serialVersionUID = -6975317983240305179L;

    @ApiModelProperty(value = "下注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal bet = BigDecimal.ZERO;
    @ApiModelProperty(value = "有效下注")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal validbet = BigDecimal.ZERO;

    @ApiModelProperty(value = "输赢金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal winLoss = BigDecimal.ZERO;
}

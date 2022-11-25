package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordObtyTotalVo implements Serializable {

    private static final long serialVersionUID = -587693640305179L;

    @ApiModelProperty(value = "订单实际投注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal orderAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "结算金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal settleAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "盈利金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal profitAmount = BigDecimal.ZERO;
}

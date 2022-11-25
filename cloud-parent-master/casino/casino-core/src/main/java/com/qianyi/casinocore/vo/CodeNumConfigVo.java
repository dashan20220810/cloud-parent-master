package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CodeNumConfigVo implements Serializable {

    private static final long serialVersionUID = -6875617946853305179L;

    @ApiModelProperty("打码倍率")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betRate = BigDecimal.ZERO;

    @ApiModelProperty("每笔最低充值")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal chargeMinMoney = BigDecimal.ZERO;

    @ApiModelProperty("每笔最高充值")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal chargeMaxMoney = BigDecimal.ZERO;

    @ApiModelProperty("每笔最低提现额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal withdrawMinMoney = BigDecimal.ZERO;

    @ApiModelProperty("每笔最高提现额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal withdrawMaxMoney = BigDecimal.ZERO;
}

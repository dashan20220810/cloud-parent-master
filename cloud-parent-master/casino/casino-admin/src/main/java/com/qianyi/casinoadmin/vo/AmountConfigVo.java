package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AmountConfigVo implements Serializable {

    private static final long serialVersionUID = -6875617929812305179L;
    /**
     * 固定金额
     */
    @ApiModelProperty(value = "固定金额")
    private BigDecimal fixedAmount;

    /**
     * 百分比金额
     */
    @ApiModelProperty(value = "百分比金额")
    private BigDecimal percentage;

    /**
     * 最大金额
     */
    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxMoney;

    /**
     * 最小金额
     */
    @ApiModelProperty(value = "最小金额")
    private BigDecimal minMoney;
}

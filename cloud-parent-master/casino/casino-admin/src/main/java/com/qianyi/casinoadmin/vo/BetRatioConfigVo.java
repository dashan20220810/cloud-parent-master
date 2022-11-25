package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
public class BetRatioConfigVo {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("打码倍率")
    private BigDecimal codeTimes = BigDecimal.ZERO;

    @ApiModelProperty("最低金额重置打码量")
    private BigDecimal minMoney = BigDecimal.ZERO;
}

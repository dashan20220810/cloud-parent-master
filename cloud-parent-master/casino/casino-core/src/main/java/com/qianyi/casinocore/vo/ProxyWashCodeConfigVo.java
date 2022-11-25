package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProxyWashCodeConfigVo implements Serializable {

    private static final long serialVersionUID = -6975317983479532179L;

    @ApiModelProperty(value = "WM返利比例")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal WMRate = BigDecimal.ZERO;

    @ApiModelProperty(value = "PG返利比例")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal PGRate = BigDecimal.ZERO;

    @ApiModelProperty(value = "CQ9返利比例")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal CQ9Rate = BigDecimal.ZERO;

    @ApiModelProperty(value = "OBDJRate返利比例")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal OBDJRate = BigDecimal.ZERO;

    @ApiModelProperty(value = "OBTY返利比例")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal OBTYRate = BigDecimal.ZERO;

    @ApiModelProperty(value = "SABASPORT返利比例")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal SABASPORTRate = BigDecimal.ZERO;

    //全局配置 userId=0
    @ApiModelProperty("基层代理ID")
    private Long userId;
}

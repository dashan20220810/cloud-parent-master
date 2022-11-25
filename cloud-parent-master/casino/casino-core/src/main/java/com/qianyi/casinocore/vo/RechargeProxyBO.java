package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeProxyBO {
    @ApiModelProperty(value = "代理用户ID")
    private Long proxyUserId;
    @ApiModelProperty(value = "充值金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal amount;
    @ApiModelProperty("是否首充 0 是 1 不是")
    private Integer isFirst;
    @ApiModelProperty(value = "是否直属")
    private boolean direct;
    private String dayTime;

    @ApiModelProperty(value = "充值订单ID")
    private Long chargeOrderId;
}

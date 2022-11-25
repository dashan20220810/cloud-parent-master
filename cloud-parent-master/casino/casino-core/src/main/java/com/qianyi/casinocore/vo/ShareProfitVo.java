package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("用户分润")
public class ShareProfitVo{

    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "有效下注")
    private BigDecimal validbet;
    @ApiModelProperty(value = "是否首次下注")
    private Boolean isFirst = false;
    @ApiModelProperty(value = "第一级用户")
    private Long firstUserId;
    @ApiModelProperty(value = "第一级分润")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal firstMoney;
    @ApiModelProperty(value = "第二级用户ID")
    private Long secondUserId;
    @ApiModelProperty(value = "第二级分润")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal secondMoney;
    @ApiModelProperty(value = "第三级用户ID")
    private Long thirdUserId;
    @ApiModelProperty(value = "第三级分润")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal thirdMoney;
}

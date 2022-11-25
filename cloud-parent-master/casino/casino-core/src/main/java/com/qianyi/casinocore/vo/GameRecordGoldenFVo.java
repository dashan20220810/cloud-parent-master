package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordGoldenFVo implements Serializable {

    private static final long serialVersionUID = -6974582348505179L;

    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "我方会员账号")
    private String account;
    @ApiModelProperty(value = "我方账号")
    private Long userId;
    @ApiModelProperty(value = "三方会员账号")
    private String playerName;
    @ApiModelProperty(value = "父主单号")
    private String parentBetId;
    @ApiModelProperty(value = "注单号")
    private String betId;
    @ApiModelProperty(value = "游戏代码")
    private String gameCode;
    @ApiModelProperty(value = "交易类型")
    private String transType;
    @ApiModelProperty(value = "下注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betAmount;
    @ApiModelProperty(value = "派彩或退回金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal winAmount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLoss;
    @ApiModelProperty(value = "结算时间")
    private String createAtStr;

}

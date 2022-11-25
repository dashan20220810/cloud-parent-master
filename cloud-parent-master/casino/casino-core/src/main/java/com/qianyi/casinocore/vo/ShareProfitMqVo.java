package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("用户分润MQ实体")
public class ShareProfitMqVo implements Serializable {

    @ApiModelProperty(value = "平台:wm,PG,CQ9,OBDJ,OBTY,SABA")
    private String platform;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "有效下注")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal validbet;
    @ApiModelProperty(value = "游戏记录表ID")
    private Long gameRecordId;
    @ApiModelProperty(value = "下注時間")
    private String betTime;
}

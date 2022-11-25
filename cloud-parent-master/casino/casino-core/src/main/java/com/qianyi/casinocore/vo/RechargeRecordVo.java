package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("充值消息对象")
public class RechargeRecordVo  implements Serializable {
    private static final long serialVersionUID = -6898736529250305179L;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "充值订单ID")
    private Long chargeOrderId;
    @ApiModelProperty("是否首充 0 是 1 不是")
    private Integer isFirst;
    @ApiModelProperty(value = "充值金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal chargeAmount;
    @ApiModelProperty(value = "第一级用户")
    private Long firstUserId;
    @ApiModelProperty(value = "第二级用户ID")
    private Long secondUserId;
    @ApiModelProperty(value = "第三级用户ID")
    private Long thirdUserId;
    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

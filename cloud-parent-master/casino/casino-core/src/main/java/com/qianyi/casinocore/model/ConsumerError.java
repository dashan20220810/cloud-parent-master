package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("消费错误日志")
public class ConsumerError extends BaseEntity{

    @ApiModelProperty(value = "sharePoint:分润，user:用户，recharge:充值")
    private String consumerType;
    private Long mainId;
    @ApiModelProperty(value = "0:未修复 1：已修复")
    private int repairStatus;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("联系客服")
public class DepositSendActivity extends BaseEntity {

    @ApiModelProperty(value = "活动名称")
    private String activityName;
    @ApiModelProperty(value = "活动类型 1 存款>=  2 存款每满")
    private Integer activityType;
    @ApiModelProperty(value = "存金额")
    private BigDecimal depositAmount;
    @ApiModelProperty(value = "送金额")
    private BigDecimal sendAmount;
    @ApiModelProperty(value = "流水出款倍数")
    private Integer amountTimes;
    @ApiModelProperty(value = "状态  0 停用  1 启用")
    private Integer activityStatus;
    @ApiModelProperty(value = "是否删除 true 删除  false  未删")
    private Boolean del;

}

package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class AccountChangeBackVo  implements Serializable {

    private static final long serialVersionUID = -6875617929250305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "会员账号")
    private String account;
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "账变类型")
    private Integer type;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "额度变化")
    private BigDecimal amount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "额度变化前")
    private BigDecimal amountBefore;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "额度变化后")
    private BigDecimal amountAfter;
    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("最后修改人")
    private String updateBy;
    public AccountChangeBackVo(){

    }
    public AccountChangeBackVo(AccountChange accountChange){
        this.id = accountChange.getId();
        this.userId = accountChange.getUserId();
        this.orderNo = accountChange.getOrderNo();
        this.type = accountChange.getType();
        this.amount = accountChange.getAmount();
        this.amountBefore = accountChange.getAmountBefore();
        this.amountAfter = accountChange.getAmountAfter();
        this.createTime = accountChange.getCreateTime();
        this.createBy = accountChange.getCreateBy();
        this.updateTime = accountChange.getUpdateTime();
        this.updateBy = accountChange.getUpdateBy();
    }
}

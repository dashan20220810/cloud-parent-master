package com.qianyi.casinocore.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("充值订单")
public class ChargeOrder extends BaseEntity{

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    //0.未确认。 1.成功   2.失败, 3.失效
    @ApiModelProperty(value = "订单状态0.未确认,1.成功,2.失败,4.总控上分 5.代理上分")
    private Integer status;

    @ApiModelProperty(value = "汇款人")
    private String remitter;

    @ApiModelProperty(value = "汇款方式 银行卡充值1  总控上分2  代理上分3 活动赠送4")
    private Integer remitType;

    @ApiModelProperty(value = "汇款金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal chargeAmount;


    @ApiModelProperty("打码倍率")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal betRate;


//    @ApiModelProperty(value = "实际充值金额")
//    private BigDecimal realityAmount;

//    @ApiModelProperty(value = "实际到账金额")
//    private BigDecimal practicalAmount;

//    @ApiModelProperty(value = "手续费")
//    private BigDecimal serviceCharge;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "收款银行卡ID")
    private Long bankcardId;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty("审核人")
    private String lastModifier;

    @ApiModelProperty("会员类型:0、公司会员，1、渠道会员")
    private Integer type;

    public ChargeOrder(){
    }

    public ChargeOrder(BigDecimal chargeAmount){
        this.chargeAmount = chargeAmount==null?BigDecimal.ZERO:chargeAmount;
    }
}

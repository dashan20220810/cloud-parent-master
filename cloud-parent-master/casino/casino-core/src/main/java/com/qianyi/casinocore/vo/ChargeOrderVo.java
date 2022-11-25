package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ChargeOrderVo implements Serializable {

    private static final long serialVersionUID = -6875617983240305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "会员账号")
    private String account;
    @ApiModelProperty(value = "基层代理账号")
    private String thirdProxy;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "订单状态0.未确认,1.成功,2.失败 4.总控上分 5.代理上分")
    private Integer status;
    @ApiModelProperty(value = "汇款人")
    private String remitter;
    @ApiModelProperty(value = "汇款方式 1银行卡 2支付宝 3微信 4人工操作")
    private Integer remitType;
    @ApiModelProperty(value = "汇款金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal chargeAmount;
    @ApiModelProperty(value = "实际充值金额")
    private BigDecimal realityAmount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "实际到账金额")
    private BigDecimal practicalAmount;
    @ApiModelProperty(value = "手续费")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal serviceCharge;
    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "收款银行卡ID")
    private Long bankcardId;
    @ApiModelProperty(value = "收款银行号")
    private String bankNo;
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    @ApiModelProperty(value = "开户名")
    private String accountName;
    @ApiModelProperty("会员类型:0、公司会员，1、渠道会员")
    private Integer type;
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

    @ApiModelProperty("打码倍率")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betRate;

    public ChargeOrderVo(ChargeOrder chargeOrder){
        this.id = chargeOrder.getId();
        this.userId = chargeOrder.getUserId();
        this.bankcardId =chargeOrder.getBankcardId();
        this.chargeAmount = chargeOrder.getChargeAmount();
        this.orderNo = chargeOrder.getOrderNo();
        this.status = chargeOrder.getStatus();
        this.remitter = chargeOrder.getRemitter();
        this.remitType = chargeOrder.getRemitType();
        this.remark = chargeOrder.getRemark();
//        this.realityAmount = chargeOrder.getRealityAmount();
//        this.practicalAmount = chargeOrder.getPracticalAmount();
//        this.serviceCharge = chargeOrder.getServiceCharge();
        this.type = chargeOrder.getType();
        this.createTime = chargeOrder.getCreateTime();
        this.updateTime = chargeOrder.getUpdateTime();
        this.createBy = chargeOrder.getCreateBy();
        this.updateBy = chargeOrder.getLastModifier();
        this.betRate=chargeOrder.getBetRate();
    }
    public ChargeOrderVo(){

    }
}

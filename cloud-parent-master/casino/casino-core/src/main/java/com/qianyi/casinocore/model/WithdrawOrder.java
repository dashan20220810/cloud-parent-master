package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@ApiModel("提币订单")
public class WithdrawOrder extends BaseEntity {

    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "状态 0: 未确认 1：通过，2：审核拒绝，4.总控下分 5.代理下分 6.审核接单(审核中) 7.审核通过 8 出款拒绝")
    private Integer status;
    @ApiModelProperty(value = "订单号")
    private String no;

    @ApiModelProperty(value = "金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal withdrawMoney;

    @ApiModelProperty(value = "实际提现金额")
    private BigDecimal practicalAmount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;

    public BigDecimal getServiceCharge(){
        // 管理后台下分是没有手续费的，这里防止出现空指针
        if (serviceCharge == null) {
            return BigDecimal.ZERO;
        }
        return serviceCharge;
    }

    @ApiModelProperty(value = "银行卡Id")
    private String bankId;

    @ApiModelProperty(value = "银行账号")
    private String bankAccount;

    @ApiModelProperty(value = "收款方式 1银行卡 2支付宝 3微信 4人工操作")
    private Integer remitType;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "出款备注")
    private String remark;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty("财务出款人")
    private String lastModifier;

    @ApiModelProperty("会员类型:0、公司会员，1、渠道会员")
    private Integer type;

    @ApiModelProperty("审核人Id")
    private Long auditId = 0L;

    //    @ApiModelProperty("出款人Id")
    //    private Long withdraw = 0L;

    @ApiModelProperty("审核时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    @ApiModelProperty("出款时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date withdrawTime;

    public WithdrawOrder(){
    }

    public WithdrawOrder(BigDecimal withdrawMoney,BigDecimal practicalAmount,BigDecimal serviceCharge){
        this.withdrawMoney = withdrawMoney==null?BigDecimal.ZERO:withdrawMoney;
        this.practicalAmount = practicalAmount==null?BigDecimal.ZERO:practicalAmount;
        this.serviceCharge = serviceCharge==null?BigDecimal.ZERO:serviceCharge;
    }

    public WithdrawOrder(BigDecimal withdrawMoney){
        this.withdrawMoney = withdrawMoney==null?BigDecimal.ZERO:withdrawMoney;
    }
}

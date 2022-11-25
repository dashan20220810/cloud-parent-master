package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.WithdrawOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WithdrawOrderVo implements Serializable {
    private static final long serialVersionUID = -6854789998456387632L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "会员账号")
    private String account;
    @ApiModelProperty(value = "状态 0: 未确认 1：通过，2：拒绝，4.总控下分 5.代理下分")
    private Integer status;
    @ApiModelProperty(value = "订单号")
    private String no;
    @ApiModelProperty(value = "金额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawMoney;
    @ApiModelProperty(value = "实际提现金额")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal practicalAmount;
    @ApiModelProperty(value = "手续费")
    private BigDecimal serviceCharge;
    @ApiModelProperty(value = "银行卡Id")
    private String bankId;
    @ApiModelProperty(value = "用户的银行号")
    private String bankNo;
    @ApiModelProperty(value = "银行卡名称")
    private String bankName;
    @ApiModelProperty(value = "开户名")
    private String accountName;
    @ApiModelProperty(value = "收款方式 1银行卡 2支付宝 3微信 4人工操作")
    private Integer remitType;
    @ApiModelProperty("会员类型:0、公司会员，1、渠道会员")
    private Integer type;
    @ApiModelProperty(value = "审核备注")
    private String auditRemark;
    @ApiModelProperty(value = "出款备注")
    private String remark;
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
    @ApiModelProperty("出款操作人")
    private String lastModifier;
    @ApiModelProperty("出款时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date withdrawTime;
    @ApiModelProperty("审核操作人")
    private String auditIdModifier;
    @ApiModelProperty("审核时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    public WithdrawOrderVo(WithdrawOrder withdrawOrder){
        this.id = withdrawOrder.getId();
        this.userId = withdrawOrder.getUserId();
        this.bankId = withdrawOrder.getBankId();
        this.no = withdrawOrder.getNo();
        this.practicalAmount = withdrawOrder.getPracticalAmount();
        this.withdrawMoney = withdrawOrder.getWithdrawMoney();
        this.serviceCharge = withdrawOrder.getServiceCharge();
        this.remitType = withdrawOrder.getRemitType();
        this.status = withdrawOrder.getStatus();
        this.type = withdrawOrder.getType();
        this.auditRemark = withdrawOrder.getAuditRemark();
        this.remark = withdrawOrder.getRemark();
        this.createBy = withdrawOrder.getCreateBy();
        this.createTime =withdrawOrder.getCreateTime();
        this.updateBy = withdrawOrder.getUpdateBy();
        this.updateTime = withdrawOrder.getUpdateTime();
        this.lastModifier = withdrawOrder.getLastModifier();
        this.withdrawTime = withdrawOrder.getWithdrawTime();
        this.auditTime = withdrawOrder.getAuditTime();
    }
    public WithdrawOrderVo(){

    }
}

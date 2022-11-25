package com.qianyi.casinoadmin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.RechargeTurnover;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RechargeTurnoverVo implements Serializable {

    private static final long serialVersionUID = -6875617998456305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "充值订单号")
    private String orderNo;
    @ApiModelProperty(value = "会员账号")
    private String account;
    @ApiModelProperty("订单id")
    private Long orderId;
    @ApiModelProperty("客户id")
    private Long userId;

    @ApiModelProperty("订单金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal orderMoney;

    @ApiModelProperty("打码量")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal codeNum;

    @ApiModelProperty("实时打码总量")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal codeNums;

    @ApiModelProperty("打码倍率")
    private Float codeTimes;
    @ApiModelProperty(value = "汇款方式 1 银行卡  和 其他")
    private Integer remitType;
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
    public RechargeTurnoverVo(RechargeTurnover rechargeTurnover){
        this.id = rechargeTurnover.getId();
        this.orderId = rechargeTurnover.getOrderId();
        this.userId = rechargeTurnover.getUserId();
        this.orderMoney =rechargeTurnover.getOrderMoney();
        this.codeNum = rechargeTurnover.getCodeNum();
        this.codeNums = rechargeTurnover.getCodeNums();
        this.codeTimes = rechargeTurnover.getCodeTimes();
        this.remitType = rechargeTurnover.getRemitType();
        this.createTime = rechargeTurnover.getCreateTime();
        this.updateTime = rechargeTurnover.getUpdateTime();
        this.createBy = rechargeTurnover.getCreateBy();
        this.updateBy = rechargeTurnover.getUpdateBy();
    }
    public RechargeTurnoverVo(){

    }
}

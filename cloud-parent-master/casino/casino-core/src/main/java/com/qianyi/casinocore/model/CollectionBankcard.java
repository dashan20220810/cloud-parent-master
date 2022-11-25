package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("收款银行卡")
public class CollectionBankcard extends BaseEntity{

    /**
     * 银行卡账号
     */
    @ApiModelProperty(value = "银行账号")
    private String bankNo;
    /**
     * 银行卡id
     */
    @ApiModelProperty(value = "银行卡id")
    private String bankId;
    /**
     * 开户名
     */
    @ApiModelProperty(value = "开户名")
    private String accountName;
    /**
     * 0:未禁用 1：禁用
     */
    @ApiModelProperty(value = "0:未禁用 1：禁用")
    private Integer disable;

    @ApiModelProperty(value = "排序")
    private Integer sortId;

//    @ApiModelProperty(value = "渠道等级 1A 2B 3C 4D")
//    private Integer grade;
//
//    @ApiModelProperty(value = "性质 1对公账号 2个人账号")
//    private Integer nature;
//
//    @ApiModelProperty(value = "使用属性 1常用卡 2备用卡")
//    private Integer attribute;
//
//    @ApiModelProperty(value = "单日最大收款")
//    private BigDecimal dayMaxAmount;
//
//    @ApiModelProperty(value = "单月最大收款")
//    private BigDecimal monthMaxAmount;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("用户已解绑银行卡")
public class BankcardsDel extends BaseEntity {
    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 银行卡id
     */
    @ApiModelProperty(value = "银行卡id")
    private String bankId;

    /**
     * 用户的银行/支付宝账号
     */
    @ApiModelProperty(value = "用户的银行账号")
    private String bankAccount;

    /**
     * 开户地址
     */
    @ApiModelProperty(value = "开户地址")
    private String address;

    @ApiModelProperty(value = "默认卡，主卡= 1")
    private Integer defaultCard;

    /**
     * 开户名
     */
    @ApiModelProperty(value = "开户名")
    private String realName;

    public BankcardsDel(){

    }
    public BankcardsDel(Bankcards bankcards){
        this.userId = bankcards.getUserId();
        this.bankId = bankcards.getBankId();
        this.bankAccount = bankcards.getBankAccount();
        this.address = bankcards.getAddress();
        this.realName = bankcards.getRealName();
        this.defaultCard = bankcards.getDefaultCard();
    }
}

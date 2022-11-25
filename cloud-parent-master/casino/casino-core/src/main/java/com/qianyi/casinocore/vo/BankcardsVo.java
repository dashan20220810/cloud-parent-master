package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.BankcardsDel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BankcardsVo  implements Serializable {
    private static final long serialVersionUID = -3009873269250305179L;
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("会员id")
    private Long userId;
    @ApiModelProperty("会员账号")
    private String account;
    @ApiModelProperty(value = "银行卡id")
    private String bankId;
    @ApiModelProperty(value = "用户的银行账号")
    private String bankAccount;
    @ApiModelProperty(value = "开户地址")
    private String address;
    @ApiModelProperty(value = "开户名")
    private String realName;
    @ApiModelProperty(value = "默认卡，主卡= 1")
    private Integer defaultCard;
    @ApiModelProperty(value = "0:未禁用 1：禁用")
    private Integer disable;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "修后修改时间")
    private Date updateTime;
    @ApiModelProperty(value = "修后修改人")
    private String updateBy;
    public BankcardsVo(){

    }
    public BankcardsVo(Bankcards bankcards){
        this.id = bankcards.getId();
        this.userId = bankcards.getUserId();
        this.bankId = bankcards.getBankId();
        this.bankAccount = bankcards.getBankAccount();
        this.address = bankcards.getAddress();
        this.realName = bankcards.getRealName();
        this.disable = 0;
        this.defaultCard = bankcards.getDefaultCard();
        this.updateTime = bankcards.getUpdateTime();
        this.updateBy = bankcards.getUpdateBy();
    }
    public BankcardsVo(BankcardsDel bankcards){
        this.id = bankcards.getId();
        this.userId = bankcards.getUserId();
        this.bankId = bankcards.getBankId();
        this.bankAccount = bankcards.getBankAccount();
        this.address = bankcards.getAddress();
        this.realName = bankcards.getRealName();
        this.defaultCard = bankcards.getDefaultCard();
        this.disable = 1;
        this.updateTime = bankcards.getUpdateTime();
        this.updateBy = bankcards.getUpdateBy();
    }
}

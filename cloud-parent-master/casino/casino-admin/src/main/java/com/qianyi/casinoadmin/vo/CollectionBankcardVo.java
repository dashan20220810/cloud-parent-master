package com.qianyi.casinoadmin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.CollectionBankcard;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CollectionBankcardVo implements Serializable {

    private static final long serialVersionUID = -6875617983249678979L;

    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    @ApiModelProperty(value = "银行账号")
    private String bankNo;
    @ApiModelProperty(value = "银行卡id")
    private String bankId;
    @ApiModelProperty(value = "开户名")
    private String accountName;
    @ApiModelProperty(value = "0:未禁用 1：禁用")
    private Integer disable;
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
    public CollectionBankcardVo(CollectionBankcard collectionBankcard){
        this.id = collectionBankcard.getId();
        this.bankNo = collectionBankcard.getBankNo();
        this.bankId = collectionBankcard.getBankId();
        this.accountName = collectionBankcard.getAccountName();
        this.disable = collectionBankcard.getDisable();
        this.createTime = collectionBankcard.getCreateTime();
        this.createBy = collectionBankcard.getCreateBy();
        this.updateTime = collectionBankcard.getUpdateTime();
        this.updateBy = collectionBankcard.getUpdateBy();
    }
}

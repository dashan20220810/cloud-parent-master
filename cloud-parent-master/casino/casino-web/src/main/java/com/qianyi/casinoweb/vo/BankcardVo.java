package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("银行卡")
@Data
public class BankcardVo {

    @ApiModelProperty("银行卡ID")
    private Long id;
    @ApiModelProperty("银行ID")
    private Long bankId;
    @ApiModelProperty("银行卡账号")
    private String bankAccount;
    @ApiModelProperty("银行名称")
    private String bankName;
    @ApiModelProperty("银行LOGO")
    private String bankLogo;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty(value = "默认卡，主卡= 1")
    private Integer defaultCard;
}

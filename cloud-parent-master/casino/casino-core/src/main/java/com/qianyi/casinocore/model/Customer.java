package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
@ApiModel("联系客服")
public class Customer extends BaseEntity {
    @ApiModelProperty(value = "qq")
    private String qq;
    @ApiModelProperty(value = "telegram")
    private String telegram;
    @ApiModelProperty(value = "skype")
    private String skype;
    @ApiModelProperty(value = "whatsApp")
    private String whatsApp;
    @ApiModelProperty(value = "facebook")
    private String facebook;
    @ApiModelProperty(value = "onlineUrl")
    private String onlineUrl;
    @ApiModelProperty(value = "微信")
    private String wechat;
    @ApiModelProperty(value = "美洽")
    private String meiqia;
    @ApiModelProperty(value = "telephone")
    private String telephone;

}

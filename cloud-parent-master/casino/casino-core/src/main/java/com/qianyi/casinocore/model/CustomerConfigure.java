package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("客服中心配置")
public class CustomerConfigure extends BaseEntity {

    @ApiModelProperty(value = "客服平台标识(1:qq,2:skype,3:telegram,4:whatsApp,5:手机号码,6:onlineUrl,7：微信)")
    private Integer customerMark;

    @ApiModelProperty(value = "客服平台(qq,skype,telegram...)")
    private String customer;

    @ApiModelProperty(value = "客服账号(qq账号,skype账号...)")
    private String customerAccount;

    @ApiModelProperty(value = "状态(1:启用,0:停用)")
    private Integer state;

    @ApiModelProperty(value = "移动端图标")
    private String appIconUrl;

    @ApiModelProperty(value = "pc端图标")
    private String pcIconUrl;


}

package com.qianyi.casinoweb.vo;

import com.qianyi.casinocore.model.CustomerConfigure;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("客服中心VO")
@Data
public class CustomerVo {

    @ApiModelProperty(value = "在线沟通地址")
    private CustomerConfigure onlineUrl;
    @ApiModelProperty(value = "联系电话")
    private CustomerConfigure telephone;
    @ApiModelProperty(value = "微信")
    private CustomerConfigure webChat;
    @ApiModelProperty(value = "其他联系方式")
    private List<CustomerConfigure> customerList;
}

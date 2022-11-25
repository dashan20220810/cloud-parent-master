package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VisitsVo {

    @ApiModelProperty("域名")
    private String domainName;

    @ApiModelProperty("访问IP数")
    private Integer domainIpCount;

    @ApiModelProperty("总访问量")
    private Integer domainCount;

    @ApiModelProperty("注册会员数")
    private Integer nums;

    @ApiModelProperty("充值会员数")
    private Integer ChargeNums;
}

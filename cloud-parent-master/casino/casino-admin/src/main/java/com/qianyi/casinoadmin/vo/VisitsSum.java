package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VisitsSum {

    @ApiModelProperty("域名")
    private String domainName;

    @ApiModelProperty("IP")
    private String ip;

    @ApiModelProperty("访问总数量")
    private Integer domainCount;
}

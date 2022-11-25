package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 域名配置
 */
@Data
public class DomainNameVo implements Serializable {

    private static final long serialVersionUID = -6875698563249676423L;
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("首页域名配置")
    private String domainNameConfiguration;

    @ApiModelProperty("推广注册域名配置")
    private String proxyConfiguration;
}

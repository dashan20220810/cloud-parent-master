package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("域名管理表")
public class DomainConfig extends BaseEntity {

    @ApiModelProperty(value = "域名名称")
    private String domainName;

    @ApiModelProperty(value = "域名地址")
    private String domainUrl;

    @ApiModelProperty(value = "状态 0：禁用， 1：启用")
    private Integer domainStatus;



}

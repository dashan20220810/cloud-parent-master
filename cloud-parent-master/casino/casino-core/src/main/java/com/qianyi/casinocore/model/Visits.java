package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("访问量")
public class Visits extends BaseEntity {
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("域名")
    private String domainName;
}

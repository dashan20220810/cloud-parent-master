package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("访问设备列表")
public class VisitsDevice extends BaseEntity {

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "设备厂商")
    private String manufacturer;

    @ApiModelProperty(value = "设备型号")
    private String model;

    @ApiModelProperty(value = "系统版本号")
    private String version;

    @ApiModelProperty(value = "设备编码")
    private String udid;
}

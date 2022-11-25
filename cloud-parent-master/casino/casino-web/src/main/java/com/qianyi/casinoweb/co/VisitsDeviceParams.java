package com.qianyi.casinoweb.co;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("设备信息")
public class VisitsDeviceParams {

    @ApiModelProperty(value = "设备厂商")
    private String manufacturer;

    @ApiModelProperty(value = "设备型号")
    private String model;

    @ApiModelProperty(value = "系统版本号")
    private String version;

    @ApiModelProperty(value = "设备编码")
    private String udid;
}

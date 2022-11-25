package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("访问设备通讯录列表")
public class VisitsDeviceAddressBook extends BaseEntity {

    @ApiModelProperty(value = "访问设备ID")
    private Long visitsDeviceId;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String phone;
}

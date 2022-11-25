package com.qianyi.casinoweb.co;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("新增访问设备入参")
@Data
public class VisitsDeviceCo {

    private VisitsDeviceParams device;

    private List<VisitsDeviceAddressBookParams> addressBook;

}

package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MaintenanceVo implements Serializable {
    private static final long serialVersionUID = -6875667978423895179L;

    @ApiModelProperty("平台维护开关 0:维护 1:开启")
    private Integer platformMaintenance;

    @ApiModelProperty("维护起始时间")
    private Date maintenanceStart;

    @ApiModelProperty("维护结束时间")
    private Date maintenanceEnd;
}

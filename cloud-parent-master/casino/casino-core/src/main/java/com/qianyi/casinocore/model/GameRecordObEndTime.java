package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("OB游戏记录抓取时间记录表")
public class GameRecordObEndTime extends BaseEntity{

    @ApiModelProperty(value = "数据拉取开始时间")
    private Long startTime;

    @ApiModelProperty(value = "数据拉取结束时间")
    private Long endTime;

    @ApiModelProperty(value = "平台编码")
    private String vendorCode;

    @ApiModelProperty(value = "状态:0.失败，1.成功")
    private Integer status;


}

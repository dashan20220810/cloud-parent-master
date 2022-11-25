package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("GoldenF游戏记录抓取结束时间记录表")
public class GameRecordGoldenfEndTime extends BaseEntity{


    @ApiModelProperty(value = "数据拉取结束时间")
    private Long endTime;

    @ApiModelProperty(value = "数据拉取开始时间")
    private Long startTime;

    @ApiModelProperty(value = "平台编码")
    private String vendorCode;


}

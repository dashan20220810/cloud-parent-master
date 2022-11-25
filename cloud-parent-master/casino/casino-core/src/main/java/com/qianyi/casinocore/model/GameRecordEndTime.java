package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("游戏记录抓取结束时间记录表")
public class GameRecordEndTime extends BaseEntity{

    /**
     * 数据拉取结束时间
     */
    private String endTime;


}

package com.qianyi.casinoweb.vo;

import com.qianyi.casinocore.model.AdGame;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("维护、关闭状态游戏列表")
@Data
public class MaintenanceGameVo {

    @ApiModelProperty(value = "平台:WM,PG,CQ9")
    private String gamePlatformName;

    @ApiModelProperty(value = "平台状态：0：维护，1：正常，2：关闭")
    private Integer platformStatus;

    @ApiModelProperty(value = "维护/关闭状态游戏列表")
    private List<AdGame> gameList;
}

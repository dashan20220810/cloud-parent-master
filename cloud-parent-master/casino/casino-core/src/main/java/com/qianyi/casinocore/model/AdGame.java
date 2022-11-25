package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("游戏表")
public class AdGame extends BaseEntity{

    @ApiModelProperty(value = "平台:WM,PG,CQ9,OBDJ,OBTY")
    private String gamePlatformName;

    @ApiModelProperty(value = "游戏编码")
    private String gameCode;

    @ApiModelProperty(value = "游戏中文名称")
    private String gameName;

    @ApiModelProperty(value = "游戏英文名称")
    private String gameEnName;

    @ApiModelProperty(value = "游戏状态 0:维护 1:正常 2:关闭")
    private Integer gamesStatus;

    public AdGame() {
    }

    public AdGame(String gamePlatformName, String gameCode, String gameName, String gameEnName, Integer gamesStatus) {
        this.gamePlatformName = gamePlatformName;
        this.gameCode = gameCode;
        this.gameName = gameName;
        this.gameEnName = gameEnName;
        this.gamesStatus = gamesStatus;
    }
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("三方游戏平台表")
public class PlatformGame extends BaseEntity  {

    @ApiModelProperty(value = "平台:WM,PG,CQ9")
    private String gamePlatformName;

    @ApiModelProperty(value = "平台状态：0：维护，1：正常，2：关闭")
    private Integer gameStatus;

    public PlatformGame() {
    }

    public PlatformGame(String gamePlatformName, Integer gameStatus) {
        this.gamePlatformName = gamePlatformName;
        this.gameStatus = gameStatus;
    }
}

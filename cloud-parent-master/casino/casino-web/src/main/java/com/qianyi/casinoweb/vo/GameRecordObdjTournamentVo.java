package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("OB游戏记录锦标赛Vo")
public class GameRecordObdjTournamentVo {

    @ApiModelProperty(value = "联赛ID")
    private Long tournamentId;

    @ApiModelProperty(value = "联赛名称")
    private String tournamentName;
}

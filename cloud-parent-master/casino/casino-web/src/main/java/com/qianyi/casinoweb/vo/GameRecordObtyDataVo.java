package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("OB体育游戏记录Vo")
public class GameRecordObtyDataVo {

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize;

    @ApiModelProperty(value = "总数")
    private Integer totalCount;

    @ApiModelProperty(value = "当前页数")
    private Integer pageNum;

    @ApiModelProperty(value = "注单记录")
    private List<GameRecordObtyVo> list;

}

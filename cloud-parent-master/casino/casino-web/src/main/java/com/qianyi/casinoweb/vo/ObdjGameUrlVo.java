package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("OB电竞游戏链接")
public class ObdjGameUrlVo {

    @ApiModelProperty("PC")
    private String pc;

    @ApiModelProperty("H5")
    private String h5;
}

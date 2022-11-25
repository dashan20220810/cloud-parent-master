package com.qianyi.casinocore.co.extractpoints;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ExtractPointsConfigCo {

    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;

}

package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DirectlyVo implements Serializable {

    private static final long serialVersionUID = -6845896563249676423L;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("人人代直属下级最大个数")
    private Integer directlyUnderTheLower;
}

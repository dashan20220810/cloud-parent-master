package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 注册开关
 */
@Data
public class RegisterSwitchVo {
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("注册开关 0 关闭 1 开启")
    private Integer registerSwitch;
}

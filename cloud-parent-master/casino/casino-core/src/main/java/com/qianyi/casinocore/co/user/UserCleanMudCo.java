package com.qianyi.casinocore.co.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserCleanMudCo {

    @ApiModelProperty(value = "用户id")
    private Long userId;

}

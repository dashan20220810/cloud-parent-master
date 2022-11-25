package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class LunboPic extends BaseEntity{
    //编号，1-5
    @ApiModelProperty("编号")
    private Integer no;
    @ApiModelProperty("地址")
    private String url;
    @ApiModelProperty("备注")
    private String remark;
    /**
     * 展示端 1 web 2 app
     */
    @ApiModelProperty("展示端 1 web 2 app")
    private Integer theShowEnd;
}

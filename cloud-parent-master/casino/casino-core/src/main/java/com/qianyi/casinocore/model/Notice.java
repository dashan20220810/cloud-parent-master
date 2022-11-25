package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("公告/活动")
public class Notice extends BaseEntity {
    @ApiModelProperty("内容")
    private String title;
    @ApiModelProperty("英文内容")
    private String enTitle;
    @ApiModelProperty("简介")
    private String introduction;
    @ApiModelProperty("英文简介")
    private String enIntroduction;
    @ApiModelProperty("详情访问页")
    private String url;
    @ApiModelProperty("是否上架")
    private Boolean isShelves = true;
}

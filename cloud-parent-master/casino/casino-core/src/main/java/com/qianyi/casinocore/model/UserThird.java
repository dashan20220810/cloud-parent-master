package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 三方用户信息
 */
@Data
@Entity
@ApiModel("我方与三方帐户信息表")
public class UserThird extends BaseEntity{

    @ApiModelProperty(value = "我方用户id")
    @Column(unique = true)
    private Long userId;

    @Column(unique = true)
    @ApiModelProperty(value = "WMF账号")
    private String account;

    @Column(unique = true)
    @ApiModelProperty(value = "GoldenF账号")
    private String goldenfAccount;

    @Column(unique = true)
    @ApiModelProperty(value = "OB电竞账号")
    private String obdjAccount;

    @Column(unique = true)
    @ApiModelProperty(value = "OB体育账号")
    private String obtyAccount;

    private String password;

    private String language;

}

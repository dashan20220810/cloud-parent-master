package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 登陆日志
 */
@Entity
@Data
@Table(indexes = {@Index(columnList = "userId"), @Index(columnList = "account"), @Index(columnList = "ip"), @Index(columnList = "createTime")})
public class LoginLog extends BaseEntity{
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "用户id")
    private Long userId;
    @ApiModelProperty(value = "账号")
    private String account;
    //帐号类型（归属）\
    @ApiModelProperty(value = "帐号类型")
    private String description;
    //IP归属地\
    @ApiModelProperty(value = "IP归属地")
    private String address;
    //类型 1 登录 2 注册
    @ApiModelProperty(value = "类型 1 登录 2 注册")
    private Integer type = 1;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

}

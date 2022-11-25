package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("用户信息")
public class UserInfoVo {
    private Long userId;
    @ApiModelProperty("名称")
    private String name;
    private String account;
    private String headImg;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("QQ")
    private String qq;
    @ApiModelProperty("微信")
    private String webChat;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("邀请码")
    private String inviteCode;
    @ApiModelProperty("域名配置")
    private String domain;
    @ApiModelProperty("上级账号")
    private String superiorAccount;
}

package com.qianyi.modulecommon.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    //系统级代码，上限100
    FAIL(-2,"fail"),
    ERROR(-1, "error"),
    SUCCESS(0, "success"),
    AUTHENTICATION_NOPASS(1, "登录已过期，请重新登录"),
    AUTHORIZATION_NOPASS(2, "授权失败"),
    GOOGLEAUTH_NOPASS(3, "谷歌身份验证码错误"),
    REQUEST_LIMIT(4, "规定时间超过请求次数"),
    RISK(6, "风险操作"),
    EMPTY_TWITHDRAWMONEY(7, "未设置交易密码"),
    MULTIDEVICE(8, "帐号已在其他设备登录,请重新登录"),
    PLATFORM_MAINTAIN(9, "平台维护中"),

    //业务级代码（101-999）
    PARAMETER_NOTNULLL(101, "参数必填"),
    CUSTOM(999, "自定义");

    private int code;
    private String msg;
}

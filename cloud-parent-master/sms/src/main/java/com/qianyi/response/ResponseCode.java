package com.qianyi.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {
    SUCCESS(0, "success"),
    AUTHENTICATION_NOPASS(-1, "认证失败"),
    IP_ACCESS_RESTRICTED(-2, "IP访问受限"),
    SENSITIVE(-3, "短信内容含有敏感字符"),
    EMPTY_CONTENT(-4, "短信内容为空"),
    CONTENT_TOOLONG(-5, "短信内容过长"),
    NOT_TEMPLATE(-6, "不是模板的短信"),
    NUM_TOO_MUCH(-7, "号码个数过多"),
    NUM_EMPTY(-8, "号码为空"),
    NUM_ERROR(-9, "号码异常"),
    NOT_BALANCE(-10, "客户余额不足，不能满足本次发送"),
    TIME_FORMAT_ERROR(-11, "定时时间格式不对"),
    PLATFORM_ERROR(-12, "由于平台的原因，批量提交出错，请与管理员联系"),
    LOCK_USER(-13, "用户被锁定"),
    QUERY_ERROR(-14, "Field为空或者查询id异常"),
    TOO_OFTEN(-15, "查询过频繁"),
    TIMESTAMP_EXPIRES(-16, "timestamp expires"),
    TEMPLATE_EMPTY(-17, "短信模版不能为空"),
    ERROR(-18, "接口异常"),
    AUTH_COMPLETE(-19, "认证完成后，需要联系商务经理为您开启短信之旅"),
    ;
    private int code;
    private String msg;

    public static String getMsgByCode(int code) {
        for (ResponseCode responseCode : ResponseCode.values()) {
            if (code == responseCode.getCode()) {
                return responseCode.getMsg();
            }
        }
        return null;
    }
}

package com.qianyi.casinocore;

public class CoreConstants {

    //系统配置组别表,分组
    public interface SysConfigGroup {
        int GROUP_FINANCE = 1; //财务相关
        int GROUP_IP = 2; //IP配置相关
        int GROUP_BET = 3; //打码配置相关
    }

    public interface SysConfigName {
        String CAPTCHA_RATE = "captchaRate"; //打码倍率名称
        String CAPTCHA_MIN = "captchaMin";   //最低金额重置打码量
        String REGISTER_IP = "registerIp";
        String LOGIN_IP = "loginIp";
        String WM_TOTAL_BALANCE = "wmTotalBalance";//平台在WM的总余额
        String WM_TOTALBALANCE_RISK = "wmTotalBalanceRisk";//平台在WM的总余额警告值
    }

    public enum SysConfigEnum{
        registerIp( 2, "registerIp" ),
        loginIp( 2, "loginIp" ),
        captchaRate(3, "captchaRate"),
        wmTotalBalanceRisk(1, "wmTotalBalanceRisk"),
        captchaMin( 3, "captchaMin" );
        int group;
        String code;
        SysConfigEnum(int group, String code) {
            this.group = group;
            this.code = code;
        }
        public String getCode() {
            return code;
        }
        public int getGroup() {
            return group;
        }
    }
}

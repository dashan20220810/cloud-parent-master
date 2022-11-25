package com.qianyi.casinocore.enums;

/**
 * 账变类型配置
 */
public enum AccountChangeEnum {

    WASH_CODE(0, "XM","洗码领取"),
    TOPUP_CODE(1, "CZ","充值"),
    ADD_CODE(2, "RGZJ","人工增加"),
//    WITHDRAW_CODE(3, "TX","提现"),
    WITHDRAWDEFEATED_CODE(4, "TXSB","提现失败"),
    SUB_CODE(5, "RGKJ","人工扣减"),
    WITHDRAW_APPLY(6, "QY","提现申请"),
    WM_IN(7, "WMIN","WM转入"),
    RECOVERY(8, "RECOVERY","WM转出"),
    SHARE_PROFIT(9, "SP","代理佣金领取"),
    PG_CQ9_IN(10, "PGCQ9IN","PG/CQ9转入"),
    PG_CQ9_OUT(11, "PGCQ9OUT","PG/CQ9转出"),
    SYSTEM_UPP (12, "SystemUpp","系统上分"),
    OBDJ_IN(13, "OBDJIN","OB电竞转入"),
    OBDJ_OUT(14, "OBDJOUT","OB电竞转出"),
    OBTY_IN(15, "OBTYIN","OB体育转入"),
    OBTY_OUT(16, "OBTYOUT","OB体育转出"),
    SABASPORT_IN(17, "SABASPORTIN","沙巴体育转入"),
    SABASPORT_OUT(18, "SABASPORTOUT","沙巴体育转出"),
    ;

    private Integer type;

    private String code;

    private String name;

    AccountChangeEnum(Integer type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}

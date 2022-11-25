package com.qianyi.casinocore.enums;

public enum PlatformTransterEnum {

    WM_PLATFORM(1, "WM"),
    PG_CQ9_PLATFORM(2, "PG/CQ9"),
    ;

    private Integer type;

    private String name;

    PlatformTransterEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}

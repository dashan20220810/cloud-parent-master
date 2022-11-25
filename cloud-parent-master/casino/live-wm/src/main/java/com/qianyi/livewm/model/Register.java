package com.qianyi.livewm.model;

import lombok.Data;

@Data
public class Register {

    private String signature;
    private String user;
    private String password;
    private String username;
    private Integer profile;
    private Integer maxwin;
    private Integer maxlose;
    private String mark;
    private Integer rakeback;
    private String limitType;
    private String chip;
    private Integer timestamp;
    private Integer syslang;

}

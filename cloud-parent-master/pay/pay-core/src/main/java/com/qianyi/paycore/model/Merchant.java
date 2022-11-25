package com.qianyi.paycore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
public class Merchant extends BaseEntity{

    private String name;
    @Column(unique = true)
    private String no;
    @JsonIgnore
    private String password;
    //余额
    private BigDecimal balance;

    //联系方式
    private String telegram;

    //归属于(某管理员或代理）
    private Long ownedId;
}

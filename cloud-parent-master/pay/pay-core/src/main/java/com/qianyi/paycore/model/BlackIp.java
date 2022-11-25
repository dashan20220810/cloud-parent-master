package com.qianyi.paycore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@ApiModel("IP黑名单")
@Data
@Entity
public class BlackIp extends BaseEntity {

    @Column(unique = true)
    private String ip;
    private String num;
}

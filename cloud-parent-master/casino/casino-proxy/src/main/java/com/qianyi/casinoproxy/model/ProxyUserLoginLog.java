package com.qianyi.casinoproxy.model;

import com.qianyi.casinocore.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyUserLoginLog extends BaseEntity {

    private String ip;
    private String userName;
    private Long userId;
    private String description;
    private String address;


}

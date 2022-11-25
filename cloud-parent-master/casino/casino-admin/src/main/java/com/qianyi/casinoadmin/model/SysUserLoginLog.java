package com.qianyi.casinoadmin.model;

import com.qianyi.casinocore.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserLoginLog extends BaseEntity {

    private String ip;
    private String userName;
    private Long userId;
    private String description;
    private String address;

}

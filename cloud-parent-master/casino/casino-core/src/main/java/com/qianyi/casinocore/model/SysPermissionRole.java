package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
@ApiModel("角色权限中间表")
public class SysPermissionRole extends BaseEntity{
    private static final long serialVersionUID = 5565743387073532981L;

    private Long roleId;

    private Long PermissionId;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;

/**
 *
 */
@Entity
@Data
@ApiModel("用户角色中间表")
public class SysUserRole extends BaseEntity {

    private static final long serialVersionUID = 5618839392019941019L;

    private Long sysUserId;

    private Long sysRoleId;
}

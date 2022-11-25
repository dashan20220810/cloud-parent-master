package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

/**
 * 用户角色表
 */
@Entity
@Data
@ApiModel("用户角色表")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 948342732674529727L;
    /**
     * 角色表，超级管理员等等
     */
    @ApiModelProperty(value = "角色代称role_admin等")
    private String name;

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @ApiModelProperty(value = "备注")
    private String remark;
}

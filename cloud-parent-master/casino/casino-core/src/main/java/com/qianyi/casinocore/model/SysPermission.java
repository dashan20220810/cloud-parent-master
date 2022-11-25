package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Data
@ApiModel("权限表")
public class SysPermission extends BaseEntity{

    private static final long serialVersionUID = -6955662629740214874L;
    //权限名称
    @ApiModelProperty(value = "权限名称")
    private String name;

    @ApiModelProperty(value = "英语")
    private String englishName;

    @ApiModelProperty(value = "柬埔寨语")
    private String cambodianName ;

    //权限描述
    @ApiModelProperty(value = "权限描述")
    private String descritpion;

    //权限链接
    @ApiModelProperty(value = "权限链接")
    private String url;

    //父节点id
    @ApiModelProperty(value = "父节点id")
    private Long pid;

    //菜单层级
    @ApiModelProperty(value = "菜单层级")
    private Integer menuLevel;

    //是否删除
    @ApiModelProperty(value = "是否删除0：否，1：是")
    private Integer isDetele;

    @ApiModelProperty(value = "标识ID")
    private Integer tagId;

    public SysPermission() {
    }

    public SysPermission(String name, String descritpion, String url, Long pid, Integer menuLevel, Integer isDetele) {
        this.name = name;
        this.descritpion = descritpion;
        this.url = url;
        this.pid = pid;
        this.menuLevel = menuLevel;
        this.isDetele = isDetele;
    }
}

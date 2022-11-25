package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SysPermissionVo implements Serializable {

    private static final long serialVersionUID = -1696313780913023377L;

    private Long id;

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
    private Long menuLevel;

    //是否删除
    @ApiModelProperty(value = "是否删除0：否，1：是")
    private Integer isDetele;

    private List<SysPermissionVo> sysPermissionVoList = new ArrayList<>();
}

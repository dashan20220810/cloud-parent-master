package com.qianyi.casinoadmin.model.dto;

import lombok.Data;

/**
 * 系统权限dto
 *
 * @author lance
 * @since 2022 -03-02 15:30:49
 */
@Data
public class SysPermissionDTO {

    // 主建
    private Long id;

    // 权限名称
    private String name;

    // 英语
    private String englishName;

    // 柬埔寨语
    private String cambodianName;

    //权限描述
    private String descritpion;

    //权限链接
    private String url;

    //菜单层级
    private Integer menuLevel;

    // 是否删除
    private Boolean delete;

    // 更新
    private SysPermissionDTO update;

}

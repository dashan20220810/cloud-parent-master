package com.qianyi.casinoadmin.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 系统权限节点
 *
 * @author lance
 * @since 2022 -03-02 15:31:00
 */
@Data
public class SysPermissionDTONode extends SysPermissionDTO{

    // 父级id
    private Long pid;

    // 子集
    private List<SysPermissionDTONode> children;

}

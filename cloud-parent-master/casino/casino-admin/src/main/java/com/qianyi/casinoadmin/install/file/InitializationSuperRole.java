package com.qianyi.casinoadmin.install.file;

import com.qianyi.casinocore.business.RoleServiceBusiness;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 初始化创建超级管理员
 */
@Component
@Slf4j
public class InitializationSuperRole{

    @Autowired
    private RoleServiceBusiness roleServiceBusiness;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysRoleService sysRoleService;

    public void saveSuperRole() {
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("系统超级管理员");
        List<SysRole> sysRoleList = sysRoleService.findbyRoleName(sysRole);
//        if(sysRoleList != null && sysRoleList.size() > 0){
//            return;
//        }
        List<Long> permissionIds = new ArrayList<>();
        //超级管理员只有系统权限
        List<SysPermission> sysPermissionList = sysPermissionService.findAll();
        sysPermissionList.forEach(sysPermission -> {
            if(sysPermission.getUrl().equals("/systemCenter") || sysPermission.getName().equals("系统管理")){
                permissionIds.add(sysPermission.getId());
            }
//            if(sysPermission.getUrl().equals("/operateCenter") || sysPermission.getName().equals("运营中心")){
//               permissionIds.add(sysPermission.getId());
//            }
        });
        if(permissionIds != null && permissionIds.size() > 0){
            List<SysPermission> allConditionPid = sysPermissionService.findAllConditionPid(permissionIds);
            if(allConditionPid != null && allConditionPid.size() > 0){
                List<Long> syspermissionIds = allConditionPid.stream().map(SysPermission::getId).collect(Collectors.toList());
                permissionIds.addAll(syspermissionIds);
                List<SysPermission> permissions = sysPermissionService.findAllConditionPid(syspermissionIds);
                if(permissions != null && permissions.size() > 0){
                    List<Long> permissionsId = permissions.stream().map(SysPermission::getId).collect(Collectors.toList());
                    permissionIds.addAll(permissionsId);
                }
            }
        }
        Long roleId = null;
        if(!sysRoleList.isEmpty() && sysRoleList.size() > 0){
            roleId = sysRoleList.get(0).getId();
        }
        Boolean result = roleServiceBusiness.save("系统超级管理员", "系统生成，不可更改", roleId, permissionIds, false);
    }
}

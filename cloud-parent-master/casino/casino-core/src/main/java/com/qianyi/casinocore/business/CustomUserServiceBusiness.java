package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserServiceBusiness {//自定义UserDetailsService 接口

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysPermissionService sysPermissionService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPermissionRoleService sysPermissionRoleService;



    /**
     * 获取当前用户的所有权限
     * @param id
     * @return
     */
    public List<SysPermission> findByAdminUserId(Long id) {
        try {
            List<SysPermission> sysPermissionList = new ArrayList<>();
            //得到用户角色表数据
            SysUserRole sysUserRole = sysUserRoleService.findbySysUserId(id);
            if(sysUserRole == null){
                return sysPermissionList;
            }
            //得到用户的角色
            SysRole sysRole = sysRoleService.findById(sysUserRole.getSysRoleId());
            if(sysRole == null){
                return sysPermissionList;
            }
            List<SysPermissionRole> sysPermissionRoles = sysPermissionRoleService.findByRoleId(sysRole.getId());
            if(sysPermissionRoles == null || sysPermissionRoles.size() <= 0){
                return sysPermissionList;
            }
            List<Long> permissionIds = sysPermissionRoles.stream().map(SysPermissionRole::getPermissionId).collect(Collectors.toList());
            sysPermissionList = sysPermissionService.findAllCondition(permissionIds);
            return sysPermissionList;
        }catch (Exception e){
            throw new UsernameNotFoundException("admin: do not exist!");
        }

    }
}

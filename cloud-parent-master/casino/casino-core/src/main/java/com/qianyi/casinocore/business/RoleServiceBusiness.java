package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceBusiness {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysPermissionRoleService sysPermissionRoleService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Transactional
    public Boolean save(String roleName, String remark, Long roleId, List<Long> menuIdList, boolean flag) {
        List<Long> permissionList = new ArrayList<>();
        if(roleId != null){
            SysRole sysRole = sysRoleService.findById(roleId);
            if(sysRole == null){
                return false;
            }
            String name = "ROLE_" + roleName;
            sysRole.setName(name);
            sysRole.setRoleName(roleName);
            sysRole.setRemark(remark);
            SysRole role = sysRoleService.save(sysRole);
            List<SysPermissionRole> byRoleId = sysPermissionRoleService.findByRoleId(roleId);
            if(menuIdList.size() < byRoleId.size() && roleName.equals("系统超级管理员") && (menuIdList.size() != 0 && byRoleId.size() != 0)){
                List<Long> ids = byRoleId.stream().map(SysPermissionRole::getId).collect(Collectors.toList());
                sysPermissionRoleService.deleteAllIds(byRoleId);
                this.save(roleName, remark, roleId, menuIdList, flag);
            }
            permissionList = byRoleId.stream().map(SysPermissionRole::getPermissionId).collect(Collectors.toList());
            if(flag){
                if(byRoleId != null && byRoleId.size() > 0){
                    sysPermissionRoleService.deleteAllIds(byRoleId);

                }
            }
        }else{
            SysRole sysRole = new SysRole();
            String name = "ROLE_" + roleName;
            sysRole.setName(name);
            sysRole.setRoleName(roleName);
            sysRole.setRemark(remark);
            SysRole role = sysRoleService.save(sysRole);
            if(role == null || role.getId() == null){
                return false;
            }
            roleId = sysRole.getId();
        }
        List<SysPermissionRole> sysPermissionRoleList = new ArrayList<>();
        for (Long id : menuIdList) {
            if(flag){
                SysPermissionRole sysPermissionRole = new SysPermissionRole();
                sysPermissionRole.setRoleId(roleId);
                sysPermissionRole.setPermissionId(id);
                sysPermissionRoleList.add(sysPermissionRole);
            }else{
                if(!permissionList.contains(id)){
                    SysPermissionRole sysPermissionRole = new SysPermissionRole();
                    sysPermissionRole.setRoleId(roleId);
                    sysPermissionRole.setPermissionId(id);
                    sysPermissionRoleList.add(sysPermissionRole);
                }
            }

        }
        if(sysPermissionRoleList.size() > 0){
            ArrayList<SysPermissionRole> sysPermissionRoles = removeSysPermission(sysPermissionRoleList);
            sysPermissionRoleService.saveAll(sysPermissionRoles);
        }
        return true;
    }

    private static ArrayList<SysPermissionRole> removeSysPermission( List<SysPermissionRole> sysPermissionRoleList){
        Set<SysPermissionRole> set = new TreeSet<SysPermissionRole>(new Comparator<SysPermissionRole>() {
            @Override
            public int compare(SysPermissionRole o1, SysPermissionRole o2) {
                return o1.getPermissionId().compareTo(o2.getPermissionId());
            }
        });
        set.addAll(sysPermissionRoleList);
        return new ArrayList<>(set);
    }

    public List<SysPermission> getSysPermissionList(Long roleId) {
        List<SysPermissionRole> byRoleId = sysPermissionRoleService.findByRoleId(roleId);
        List<Long> permissionIds = byRoleId.stream().map(SysPermissionRole::getPermissionId).collect(Collectors.toList());
        List<SysPermission> sysPermissionList = sysPermissionService.findByPromission(permissionIds);
        return sysPermissionList;
    }


    public List<SysRole> findRoleList(Long roleId, Long userId) {
        if(roleId != null){
            List<SysRole> sysRoleList = new ArrayList<>();
            SysRole sysRole = sysRoleService.findById(roleId);
            if(sysRole != null){
                sysRoleList.add(sysRole);
            }
            return sysRoleList;
        }

        if(userId != null){
            List<SysRole> sysRoleList = new ArrayList<>();
            SysUserRole sysUserRole = sysUserRoleService.findbySysUserId(userId);
            if(sysUserRole == null){
                return sysRoleList;
            }
            SysRole sysRole = sysRoleService.findById(sysUserRole.getSysRoleId());
            if(sysRole != null){
                sysRoleList.add(sysRole);
            }
            return sysRoleList;
        }

        return sysRoleService.findAll();
    }

    public void saveSysUserRole(SysUserRole sysUserRole) {
        SysUserRole sys = sysUserRoleService.save(sysUserRole);

    }

    public Boolean savePermission(String descritpion, String name, Long pid, String permissionUrl, Long id) {
        SysPermission sys = sysPermissionService.findById(pid);
        if(sys == null){
            return false;
        }
        SysPermission sysPermission = sysPermissionService.findById(id);
        if(sysPermission == null){
            sysPermission = new SysPermission();
        }
        sysPermission.setName(name);
        sysPermission.setUrl(permissionUrl);
        sysPermission.setPid(pid);
        sysPermission.setMenuLevel(sys.getMenuLevel() + 1);
        if(descritpion != null && !sysPermission.equals("")){
            sysPermission.setDescritpion(descritpion);
        }else{
            sysPermission.setDescritpion(name);
        }
        sysPermissionService.save(sysPermission);
        return true;
    }

    public Boolean deleteAllPermission(List<Long> permissionList) {
        sysPermissionService.deleteAllIds(permissionList);
        return true;
    }

    public void deleteRoleList(Long roleId) {
        //删除角色对应的用户权限
        sysUserRoleService.deleteById(roleId);
        sysRoleService.deleteById(roleId);
        List<SysPermissionRole> byRoleId = sysPermissionRoleService.findByRoleId(roleId);
        if(byRoleId != null && byRoleId.size() > 0){
            List<Long> longList = byRoleId.stream().map(SysPermissionRole::getId).collect(Collectors.toList());
            sysPermissionRoleService.deleteAllIds(byRoleId);

        }
    }

    public List<SysUserRole> findSysRoleUserList(List<Long> userIds) {
        return sysUserRoleService.findAllIds(userIds);
    }


    public List<SysRole> findRoleIdsList(List<Long> roleIds) {
        return sysRoleService.findAllIds(roleIds);
    }

    public SysUserRole getSysUserRole(Long id) {
        return sysUserRoleService.findByRoleUserId(id);
    }
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long>, JpaSpecificationExecutor<SysUserRole> {

   SysUserRole findBySysUserId(Long sysUserId);

    void deleteBySysRoleId(Long roleId);
}

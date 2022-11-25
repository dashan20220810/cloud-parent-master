package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.SysPermissionRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SysPermissionRoleRepository extends JpaRepository<SysPermissionRole, Long>, JpaSpecificationExecutor<SysPermissionRole> {
    //    List<SysPermissionRole> findByRoleIdOrderByNameDesc(Long id);
    List<SysPermissionRole> findByRoleId(Long id);

    void deleteByRoleId(Long id);
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.model.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long>, JpaSpecificationExecutor<SysPermission> {

    public SysPermission findByUrl(String url);
    SysPermission findByName(String name);
}

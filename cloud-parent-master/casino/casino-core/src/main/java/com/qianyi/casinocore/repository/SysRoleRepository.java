package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.model.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {


}

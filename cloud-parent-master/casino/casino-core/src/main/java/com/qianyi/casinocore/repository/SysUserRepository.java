package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SysUserRepository extends JpaRepository<SysUser,Long> , JpaSpecificationExecutor<SysUser> {

    SysUser findByUserName(String userName);

    SysUser findAllById(Long id);

//    @Query("update SysUser u set u.gaKey=?2 where u.id=?1")
//    @Modifying
//    void setSecretById(Long id, String gaKey);
}

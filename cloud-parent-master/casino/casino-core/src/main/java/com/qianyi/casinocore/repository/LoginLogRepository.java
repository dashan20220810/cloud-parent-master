package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog,Long>, JpaSpecificationExecutor<LoginLog> {
    @Query(value = "select * from login_log l where l.ip = ?",nativeQuery = true)
    List<LoginLog> findLoginLogList(String ip);
}

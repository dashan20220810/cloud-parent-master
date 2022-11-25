package com.qianyi.paycore.repository;

import com.qianyi.paycore.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginLogRepository extends JpaRepository<LoginLog,Long>, JpaSpecificationExecutor<LoginLog> {
}

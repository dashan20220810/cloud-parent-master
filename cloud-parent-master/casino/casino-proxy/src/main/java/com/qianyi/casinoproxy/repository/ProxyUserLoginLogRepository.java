package com.qianyi.casinoproxy.repository;

import com.qianyi.casinoproxy.model.ProxyUserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyUserLoginLogRepository  extends JpaRepository<ProxyUserLoginLog,Long> {
}

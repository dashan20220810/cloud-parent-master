package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProxyCommissionRepository extends JpaRepository<ProxyCommission,Long>, JpaSpecificationExecutor<ProxyCommission> {
    ProxyCommission findByProxyUserId(Long proxyUserId);

    List<ProxyCommission> findBySecondProxy(Long secondProxy);
}

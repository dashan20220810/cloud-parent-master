package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyHomePageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProxyHomePageReportRepository extends JpaRepository<ProxyHomePageReport, Long>, JpaSpecificationExecutor<ProxyHomePageReport> {
    ProxyHomePageReport findByProxyUserIdAndStaticsTimes(Long proxyUserId,String staticsTimes);

    List<ProxyHomePageReport> findByStaticsTimes(String staticsTimes);

    List<ProxyHomePageReport> findByProxyUserId(Long proxyUserId);

    @Modifying
    @Query("update ProxyHomePageReport p set p.firstProxy= ?2 where p.proxyUserId=?1")
    void updateFirstProxy(Long proxyUserId, Long firstProxy);

    @Modifying
    @Query("update ProxyHomePageReport p set p.secondProxy= ?2 where p.proxyUserId=?1")
    void updateSecondProxy(Long proxyUserId, Long secondProxy);
}

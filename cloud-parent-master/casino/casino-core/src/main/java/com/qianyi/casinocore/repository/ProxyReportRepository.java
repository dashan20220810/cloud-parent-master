package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyReport;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import java.math.BigDecimal;

public interface ProxyReportRepository extends JpaRepository<ProxyReport,Long>, JpaSpecificationExecutor<ProxyReport> {


    ProxyReport findProxyReportByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ProxyReport findByUserId(Long userId);
}

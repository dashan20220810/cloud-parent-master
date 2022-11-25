package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyDayReport;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import java.math.BigDecimal;

public interface ProxyDayReportRepository extends JpaRepository<ProxyDayReport,Long>, JpaSpecificationExecutor<ProxyDayReport> {

    ProxyDayReport findProxyDayReportByUserIdAndDayTime(Long userId,String dayTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ProxyDayReport findByUserIdAndDayTime(Long userId,String dayTime);

    @Modifying
    @Query("update ProxyDayReport r set r.profitAmount=r.profitAmount+?1,r.betAmount=r.betAmount+?2 where r.userId=?3 and r.dayTime=?4")
    void updateProxyDayReport(BigDecimal profitAmount, BigDecimal betAmount,Long userId,String dayTime);

}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ExtractPointsChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExtractPointsChangeRepository extends JpaRepository<ExtractPointsChange,Long>, JpaSpecificationExecutor<ExtractPointsChange> {
    @Query(value = "select ifnull(sum(amount),0) amount  from extract_points_change e where create_time >= ?1 and create_time <= ?2 ",nativeQuery = true)
    BigDecimal sumAmount(String startTime, String endTime);

    @Query(value = "select ifnull(sum(amount),0) amount  from extract_points_change e ",nativeQuery = true)
    BigDecimal sumAmount();

    @Query(value = "select LEFT(date_sub(e.create_time, interval 12 hour),10) as orderTimes,ifnull(sum(amount),0) as amount  from "
        + "extract_points_change e where e.create_time >= ?1 and e.create_time <= ?2 "
        + "GROUP BY LEFT(date_sub(e.create_time, interval 12 hour),10); ",nativeQuery = true)
    List<Map<String, Object>> getMapSumAmount(String startTime, String endTime);
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.WashCodeChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WashCodeChangeRepository extends JpaRepository<WashCodeChange,Long>, JpaSpecificationExecutor<WashCodeChange> {
    @Query(value = "  select \n" +
        "  ifnull(sum(amount),0) wash_amount  \n" +
        "  from wash_code_change w \n" +
        "  where create_time >= ?1 and create_time <= ?2\n"
        ,nativeQuery = true)
    BigDecimal sumAmount(String startTime, String endTime);

    @Query(value = "  select \n" +
        "  ifnull(sum(amount),0) wash_amount  \n" +
        "  from wash_code_change w \n"
        ,nativeQuery = true)
    BigDecimal sumAmount();

    @Query(value = "select LEFT(date_sub(e.create_time, interval 12 hour),10) as orderTimes,ifnull(sum(amount),0) as amount  "
        + "from wash_code_change e where e.create_time >= ?1 and e.create_time <= ?2 "
        + "GROUP BY LEFT(date_sub(e.create_time, interval 12 hour),10);"
        ,nativeQuery = true) List<Map<String, Object>> getMapSumAmount(String startTime, String endTime);
}

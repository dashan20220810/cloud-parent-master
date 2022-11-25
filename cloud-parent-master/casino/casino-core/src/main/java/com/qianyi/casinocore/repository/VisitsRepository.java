package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Visits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface VisitsRepository extends JpaRepository<Visits,Long>, JpaSpecificationExecutor<Visits> {

    @Query(value = "select domain_name as domainName , ip as ip, count(0) as domainCount  from visits " +
            "where create_time between ?1 and ?2 " +
            "and if(?3!='', domain_name =?3, 1=1) " +
            "and if(?4!='', ip =?4, 1=1) GROUP BY domain_name , ip", nativeQuery = true)
    List<Map<String,Object>> findListGroupBy(String startTime, String endTime, String domainName, String ip);
}

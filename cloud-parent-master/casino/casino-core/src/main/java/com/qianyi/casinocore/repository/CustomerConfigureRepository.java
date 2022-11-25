package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CustomerConfigure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerConfigureRepository extends JpaRepository<CustomerConfigure,Long> {

    List<CustomerConfigure> findByState(Integer state);

    @Query(value = "select COUNT(1) from customer_configure where state=1 and customer_mark <=4 ",nativeQuery = true)
    int  countCustomerConfigure(Integer state);
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ErrorOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ErrorOrderRepository extends JpaRepository<ErrorOrder, Long>, JpaSpecificationExecutor<ErrorOrder> {

    @Query(value = "select * from error_order c where c.id = ? for update",nativeQuery = true)
    ErrorOrder findErrorOrderByIdUseLock(Long id);

    @Modifying
    @Query(value = "update error_order c set c.status = ?1 where  c.id = ?2",nativeQuery = true)
    void updateErrorOrdersRemark(Integer status,Long id);

    @Modifying
    @Query(value = "update error_order c set c.status = ?1,c.remark = ?2 where  c.id = ?3 and c.status=0",nativeQuery = true)
    Integer updateErrorStatusRemark(Integer status,String remark,Long id);
}

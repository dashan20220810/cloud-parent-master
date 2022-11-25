package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ConsumerError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ConsumerErrorRepository extends JpaRepository<ConsumerError,Long>, JpaSpecificationExecutor<ConsumerError> {

    List<ConsumerError> findByMainIdAndConsumerTypeAndRepairStatus(Long userId,String type,int status);

    List<ConsumerError> findByConsumerTypeAndRepairStatus(String type,int status);
}

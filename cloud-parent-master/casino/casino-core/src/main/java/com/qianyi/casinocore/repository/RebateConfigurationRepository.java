package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.RebateConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RebateConfigurationRepository extends JpaRepository<RebateConfiguration,Long>, JpaSpecificationExecutor<RebateConfiguration> {
    RebateConfiguration findByUserIdAndType(Long userId, Integer type);

    RebateConfiguration findByUserId(Long userId);
}

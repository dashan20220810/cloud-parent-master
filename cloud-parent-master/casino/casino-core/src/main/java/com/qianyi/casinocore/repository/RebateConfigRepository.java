package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.RebateConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RebateConfigRepository extends JpaRepository<RebateConfig,Long>, JpaSpecificationExecutor<RebateConfig> {

     RebateConfig findByGameType(Integer gameType);
}

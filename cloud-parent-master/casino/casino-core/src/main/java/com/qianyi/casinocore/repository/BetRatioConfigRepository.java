package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.BetRatioConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BetRatioConfigRepository extends JpaRepository<BetRatioConfig, Long>, JpaSpecificationExecutor<BetRatioConfig> {
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfig,Long>, JpaSpecificationExecutor<PlatformConfig> {
}

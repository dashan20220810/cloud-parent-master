package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.DomainConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainConfigRepository extends JpaRepository<DomainConfig,Long> {

    DomainConfig findByDomainUrlAndDomainStatus(String domainUrl, Integer domainStatus);
}

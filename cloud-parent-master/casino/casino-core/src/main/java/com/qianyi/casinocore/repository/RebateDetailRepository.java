package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.RebateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RebateDetailRepository extends JpaRepository<RebateDetail, Long>, JpaSpecificationExecutor<RebateDetail> {
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordObdjDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordObdjDetailRepository extends JpaRepository<GameRecordObdjDetail, Long>, JpaSpecificationExecutor<GameRecordObdjDetail> {

    GameRecordObdjDetail findByBetDetailId(Long betDetailId);

}

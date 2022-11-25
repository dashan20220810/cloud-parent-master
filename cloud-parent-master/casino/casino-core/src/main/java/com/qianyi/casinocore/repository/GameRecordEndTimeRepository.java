package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordEndTimeRepository extends JpaRepository<GameRecordEndTime, Long>, JpaSpecificationExecutor<GameRecordEndTime> {

    GameRecordEndTime findFirstByOrderByEndTimeDesc();
}

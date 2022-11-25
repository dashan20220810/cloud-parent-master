package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordObEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordObEndTimeRepository extends JpaRepository<GameRecordObEndTime, Long>, JpaSpecificationExecutor<GameRecordObEndTime> {

    GameRecordObEndTime findFirstByVendorCodeAndStatusOrderByEndTimeDesc(String vendor, Integer Status);
}

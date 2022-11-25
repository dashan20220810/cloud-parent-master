package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.model.GameRecordGoldenfEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordGoldenfEndTimeRepository extends JpaRepository<GameRecordGoldenfEndTime,Long> , JpaSpecificationExecutor<GameRecordGoldenfEndTime> {

//    GameRecordGoldenfEndTime findFirstByOrderByEndTimeDesc();
    GameRecordGoldenfEndTime findFirstByOrderByEndTimeDesc();

    GameRecordGoldenfEndTime findFirstByVendorCodeOrderByEndTimeDesc(String vendor);
}

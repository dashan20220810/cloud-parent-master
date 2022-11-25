package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordEndIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface GameRecordEndIndexRepository  extends JpaRepository<GameRecordEndIndex,Long>,
    JpaSpecificationExecutor<GameRecordEndIndex> {

    @Query(value = "select * from game_record_end_index limit 1 for update",nativeQuery = true)
    GameRecordEndIndex findUGameRecordEndIndexUseLock();
}

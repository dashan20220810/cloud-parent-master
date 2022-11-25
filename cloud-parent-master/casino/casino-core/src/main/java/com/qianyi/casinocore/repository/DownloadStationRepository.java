package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.DownloadStation;
import com.qianyi.casinocore.model.GameRecordEndTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DownloadStationRepository extends JpaRepository<DownloadStation,Long>, JpaSpecificationExecutor<DownloadStation> {

    List<DownloadStation> findByterminalTypeAndVersionNumberGreaterThan(Integer terminalType, String versionNumber);

    DownloadStation findFirstByTerminalTypeOrderByCreateTimeDesc(Integer terminalType);

}

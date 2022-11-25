package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.PoxyExtractPointsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PoxyExtractPointsConfigRepository extends JpaRepository<PoxyExtractPointsConfig, Long>, JpaSpecificationExecutor<PoxyExtractPointsConfig> {

    List<PoxyExtractPointsConfig> findAllByPoxyId(Long poxyId);

    // 根据游戏id和代理id查询抽点配置
    // UNIQUE INDEX `uq_game`(`game_id`, `poxy_id`) USING BTREE COMMENT '游戏抽点配置唯一索引',
    PoxyExtractPointsConfig findFirstByGameIdAndPoxyId(String gameId, Long poxyId);

}

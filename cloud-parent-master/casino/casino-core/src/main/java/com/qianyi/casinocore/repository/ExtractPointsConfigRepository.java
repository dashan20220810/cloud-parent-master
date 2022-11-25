package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ExtractPointsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ExtractPointsConfigRepository extends JpaRepository<ExtractPointsConfig,Long>, JpaSpecificationExecutor<ExtractPointsConfig> {

    // 根据游戏id查询默认抽点配置
    // UNIQUE INDEX `uq_game`(`game_id`) USING BTREE COMMENT '游戏抽点配置唯一索引',
    ExtractPointsConfig findFirstByGameId(String id);

    List<ExtractPointsConfig> findByPlatform(String platform);

}

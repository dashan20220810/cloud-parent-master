package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserExtractPointsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserExtractPointsConfigRepository extends JpaRepository<UserExtractPointsConfig,Long>, JpaSpecificationExecutor<UserExtractPointsConfig> {

    List<UserExtractPointsConfig> findAllByUserIdAndPoxyId(Long userId, Long poxyId);

    // 根据游戏id，代理id，用户id查询抽点配置
    // UNIQUE INDEX `uq_game`(`game_id`, `poxy_id`, `user_id`) USING BTREE COMMENT '游戏抽点配置唯一索引',
    UserExtractPointsConfig findFirstByGameIdAndPoxyIdAndUserId(String gameId, Long poxyId, Long userId);
}

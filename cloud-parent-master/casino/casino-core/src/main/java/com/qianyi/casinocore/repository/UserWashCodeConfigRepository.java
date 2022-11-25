package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserWashCodeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserWashCodeConfigRepository extends JpaRepository<UserWashCodeConfig,Long>, JpaSpecificationExecutor<UserWashCodeConfig> {

    List<UserWashCodeConfig> findByUserIdAndPlatformAndState(Long userId, String platform,Integer state);

    List<UserWashCodeConfig> findByUserIdAndPlatform(Long userId, String platform);

    List<UserWashCodeConfig> findByUserIdAndState(Long userId, Integer state);

    List<UserWashCodeConfig> findByUserId(Long userId);

    List<UserWashCodeConfig> findByUserIdAndPlatformIn(Long userId, List<String> platformList);
}

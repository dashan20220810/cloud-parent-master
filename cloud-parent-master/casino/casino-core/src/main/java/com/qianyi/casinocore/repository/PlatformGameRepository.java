package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.PlatformGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlatformGameRepository extends JpaRepository<PlatformGame,Long>, JpaSpecificationExecutor<PlatformGame> {

    PlatformGame findByGamePlatformName(String gamePlatformName);
}

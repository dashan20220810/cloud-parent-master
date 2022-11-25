package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.AdGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AdGameRepository extends JpaRepository<AdGame,Long>, JpaSpecificationExecutor<AdGame> {
    List<AdGame> findByGameCode(String gameCode);

    AdGame findByGamePlatformNameAndGameCode(String gamePlatformName,String gameCode);

    List<AdGame> findByGamePlatformNameAndGameNameLikeAndGamesStatusIn(String gamePlatformName,String gameName,List<Integer> gameStatus);

    List<AdGame> findByGamePlatformNameAndGamesStatusIn(String gamePlatformName, List<Integer> gameStatus);

    List<AdGame> findByGamesStatus(Integer gamesStatus);

    List<AdGame> findByGamePlatformNameAndGamesStatus(String gamePlatformName, Integer gamesStatus);
}

package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordObdjTournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRecordObdjTournamentRepository extends JpaRepository<GameRecordObdjTournament, Long>, JpaSpecificationExecutor<GameRecordObdjTournament> {

    GameRecordObdjTournament findByTournamentId(Long tournamentId);

}

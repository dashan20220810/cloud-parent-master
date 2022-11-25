package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordObdjTournament;
import com.qianyi.casinocore.repository.GameRecordObdjTournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordObdjTournamentService {

    @Autowired
    private GameRecordObdjTournamentRepository gameRecordObdjTournamentRepository;

    public GameRecordObdjTournament save(GameRecordObdjTournament gameRecordObdjTournament) {
        return gameRecordObdjTournamentRepository.save(gameRecordObdjTournament);
    }

    public GameRecordObdjTournament findByTournamentId(Long tournamentId){
        return gameRecordObdjTournamentRepository.findByTournamentId(tournamentId);
    }
}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordObdjDetail;
import com.qianyi.casinocore.repository.GameRecordObdjDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordObdjDetailService {

    @Autowired
    private GameRecordObdjDetailRepository gameRecordObdjDetailRepository;

    public GameRecordObdjDetail save(GameRecordObdjDetail gameRecordObdjDetail) {
        return gameRecordObdjDetailRepository.save(gameRecordObdjDetail);
    }

    public GameRecordObdjDetail findByBetDetailId(Long betDetailId){
        return gameRecordObdjDetailRepository.findByBetDetailId(betDetailId);
    }

}

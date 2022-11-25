package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordEndTime;
import com.qianyi.casinocore.repository.GameRecordEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordEndTimeService {

    @Autowired
    GameRecordEndTimeRepository gameRecordEndTimeRepository;

    public GameRecordEndTime findFirstByEndTimeDesc(){
        return gameRecordEndTimeRepository.findFirstByOrderByEndTimeDesc();
    }

    public void save(GameRecordEndTime po){
        gameRecordEndTimeRepository.save(po);
    }
}

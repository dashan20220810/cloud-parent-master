package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordEndTime;
import com.qianyi.casinocore.model.GameRecordGoldenfEndTime;
import com.qianyi.casinocore.repository.GameRecordGoldenfEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordGoldenfEndTimeService {

    @Autowired
    private GameRecordGoldenfEndTimeRepository gameRecordGoldenfEndTimeRepository;

    public GameRecordGoldenfEndTime findFirstByOrderByEndTimeDesc(){
        return gameRecordGoldenfEndTimeRepository.findFirstByOrderByEndTimeDesc();
    }

    public GameRecordGoldenfEndTime findFirstByVendorCodeOrderByEndTimeDesc(String vendor){
        return gameRecordGoldenfEndTimeRepository.findFirstByVendorCodeOrderByEndTimeDesc(vendor);
    }

    public GameRecordGoldenfEndTime save(GameRecordGoldenfEndTime gameRecordGoldenfEndTime){
        return gameRecordGoldenfEndTimeRepository.save(gameRecordGoldenfEndTime);
    }
}

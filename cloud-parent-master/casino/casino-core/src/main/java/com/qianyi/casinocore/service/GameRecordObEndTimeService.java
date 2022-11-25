package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordObEndTime;
import com.qianyi.casinocore.repository.GameRecordObEndTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordObEndTimeService {

    @Autowired
    private GameRecordObEndTimeRepository gameRecordObEndTimeRepository;

    public GameRecordObEndTime findFirstByVendorCodeAndStatusOrderByEndTimeDesc(String vendorCode, Integer status) {
        return gameRecordObEndTimeRepository.findFirstByVendorCodeAndStatusOrderByEndTimeDesc(vendorCode, status);
    }

    public GameRecordObEndTime save(GameRecordObEndTime gameRecordObEndTime) {
        return gameRecordObEndTimeRepository.save(gameRecordObEndTime);
    }
}

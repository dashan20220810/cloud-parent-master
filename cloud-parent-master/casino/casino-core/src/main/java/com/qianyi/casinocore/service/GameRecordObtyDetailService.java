package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordObtyDetail;
import com.qianyi.casinocore.repository.GameRecordObtyDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameRecordObtyDetailService {

    @Autowired
    private GameRecordObtyDetailRepository gameRecordObtyDetailRepository;

    public GameRecordObtyDetail save(GameRecordObtyDetail gameRecordObtyDetail) {
        return gameRecordObtyDetailRepository.save(gameRecordObtyDetail);
    }
}

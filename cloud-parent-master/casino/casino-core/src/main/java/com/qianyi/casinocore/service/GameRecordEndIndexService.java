package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecordEndIndex;
import com.qianyi.casinocore.repository.GameRecordEndIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class GameRecordEndIndexService {

    @Autowired
    private GameRecordEndIndexRepository gameRecordEndIndexRepository;

//    @Cacheable(cacheNames = "gameRecordEndIndex")
    public GameRecordEndIndex findFirst() {
        List<GameRecordEndIndex> gameRecordEndIndexList = gameRecordEndIndexRepository.findAll();
        if (!CollectionUtils.isEmpty(gameRecordEndIndexList)) {
            return gameRecordEndIndexList.get(0);
        }
        return null;
    }

    public GameRecordEndIndex findUGameRecordEndIndexUseLock(){
        return gameRecordEndIndexRepository.findUGameRecordEndIndexUseLock();
    }

//    @CacheEvict(cacheNames = "gameRecordEndIndex", allEntries = true)
    public void save(GameRecordEndIndex gameRecordEndIndex) {
        gameRecordEndIndexRepository.save(gameRecordEndIndex);
    }
}

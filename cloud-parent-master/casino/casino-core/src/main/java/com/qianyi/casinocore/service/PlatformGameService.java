package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.PlatformGame;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.PlatformGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"platformGame"})
public class PlatformGameService {

    @Autowired
    private PlatformGameRepository platformGameRepository;

    public List<PlatformGame> findAll() {
        return platformGameRepository.findAll();
    }

    public PlatformGame findById(Long id) {
        Optional<PlatformGame> optional = platformGameRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public PlatformGame findByGamePlatformName(String gamePlatformName){
        return platformGameRepository.findByGamePlatformName(gamePlatformName);
    }

    @CacheEvict(allEntries = true)
    public void save(PlatformGame platformGame) {
        platformGameRepository.save(platformGame);
    }

    @CacheEvict(allEntries = true)
    public void saveAll(List<PlatformGame> platformGames) {
        platformGameRepository.saveAll(platformGames);
    }
}

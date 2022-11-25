package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.repository.WashCodeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = {"washCodeConfig"})
public class WashCodeConfigService {

    @Autowired
    private WashCodeConfigRepository washCodeConfigRepository;

    public List<WashCodeConfig> findAll(){
        return washCodeConfigRepository.findAll();
    }

    @Cacheable(key = "#platform+'::'+#state")
    public List<WashCodeConfig> findByPlatformAndState(String platform,Integer state){
        return washCodeConfigRepository.findByPlatformAndState(platform,state);
    }

    @Cacheable(key = "'findByState::'+#p0")
    public List<WashCodeConfig> findByState(Integer state){
        return washCodeConfigRepository.findByState(state);
    }

    @Cacheable(key = "#platform")
    public List<WashCodeConfig> findByPlatform(String platform){
        return washCodeConfigRepository.findByPlatform(platform);
    }

    @CacheEvict(allEntries = true)
    public List<WashCodeConfig> saveAll(List<WashCodeConfig> list){
        return washCodeConfigRepository.saveAll(list);
    }

    public List<List<WashCodeConfig>> findWashCodeConfigAll(WashCodeConfig washCodeConfig) {
        return null;
    }

    public List<WashCodeConfig> findByStateAndPlatformIn(Integer state, List<String> platformList) {
        return washCodeConfigRepository.findByStateAndPlatformIn(state,platformList);
    }
}

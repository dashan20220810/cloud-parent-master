package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.repository.ProxyRebateConfigRepository;
import com.qianyi.casinocore.repository.RebateConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class RebateConfigService {

    @Autowired
    private RebateConfigRepository rebateConfigRepository;
    public List<RebateConfig> findAll(){
        return rebateConfigRepository.findAll();
    }

    @Cacheable(cacheNames = "rebateConfig")
    public RebateConfig findFirst() {
        List<RebateConfig> configList = rebateConfigRepository.findAll();
        if (!CollectionUtils.isEmpty(configList)) {
            return configList.get(0);
        }
        return null;
    }
    @Cacheable(cacheNames = "rebateConfig")
    public RebateConfig findGameType(Integer gameType){
        return rebateConfigRepository.findByGameType(gameType);
    }

    @CacheEvict(cacheNames = "rebateConfig", allEntries = true)
    public RebateConfig save(RebateConfig rebateConfig) {
        return rebateConfigRepository.save(rebateConfig);
    }
}

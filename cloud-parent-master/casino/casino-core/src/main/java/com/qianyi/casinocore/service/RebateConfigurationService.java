package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.RebateConfiguration;
import com.qianyi.casinocore.repository.RebateConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = {"rebateConfiguration"})
public class RebateConfigurationService {

    @Autowired
    private RebateConfigurationRepository rebateConfigurationRepository;

    @Cacheable(key = "#userId+'::'+#type")
    public RebateConfiguration findByUserIdAndType(Long userId,Integer type){
        return rebateConfigurationRepository.findByUserIdAndType(userId,type);
    }
    public RebateConfiguration findByUserId(Long userId){
        return rebateConfigurationRepository.findByUserId(userId);
    }

    @CachePut(key="#result.userId+'::'+#result.type",condition = "#result != null")
    public RebateConfiguration save(RebateConfiguration rebateConfiguration) {
        return rebateConfigurationRepository.save(rebateConfiguration);
    }

    @CacheEvict(key = "#userId+'::'+#rebateConfiguration.type")
    public void delete(Long userId,RebateConfiguration rebateConfiguration) {
        rebateConfigurationRepository.delete(rebateConfiguration);
    }

}

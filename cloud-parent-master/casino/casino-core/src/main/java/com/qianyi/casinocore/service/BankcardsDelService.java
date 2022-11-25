package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.BankcardsDel;
import com.qianyi.casinocore.repository.BankcardsDelRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = {"bankcardsDel"})
public class BankcardsDelService {
    @Autowired
    private BankcardsDelRepository bankcardsDelRepository;
    @Caching(
            evict = @CacheEvict(key = "'" + Constants.REDIS_USERID + "'+#p0.userId"),
            put = @CachePut(key="#result.id")
    )
    public BankcardsDel save(BankcardsDel bankcardsDel){
        return bankcardsDelRepository.save(bankcardsDel);
    }
    @Cacheable(key = "'" + Constants.REDIS_USERID + "'+#p0")
    public List<BankcardsDel> findByUserId(Long userId){
        return bankcardsDelRepository.findByUserId(userId);
    }
}

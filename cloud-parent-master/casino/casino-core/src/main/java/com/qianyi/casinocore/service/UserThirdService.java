package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserThirdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@CacheConfig(cacheNames = {"userThird"})
public class UserThirdService {
    @Autowired
    UserThirdRepository userThirdRepository;

    @Cacheable(key = "#p0")
    public UserThird findByUserId(Long userId) {
        return userThirdRepository.findByUserId(userId);
    }
    @Caching(
        put = {@CachePut(key = "#result.userId"), @CachePut(key = "'findByAccount::'+#result.account",condition = "#result.account != null"),
            @CachePut(key = "'findByGoldenfAccount::'+#result.goldenfAccount",condition = "#result.goldenfAccount != null"),
            @CachePut(key = "'findByObdjAccount::'+#result.obdjAccount",condition = "#result.obdjAccount != null"),
            @CachePut(key = "'findByObtyAccount::'+#result.obtyAccount",condition = "#result.obtyAccount != null"),
        }
    )
    public UserThird save(UserThird third) {
        return userThirdRepository.save(third);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByAccount(String account) {
        return userThirdRepository.findByAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByGoldenfAccount(String account) {
        return userThirdRepository.findByGoldenfAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByObdjAccount(String account) {
        return userThirdRepository.findByObdjAccount(account);
    }

    @Cacheable(key = "#root.methodName+'::'+#p0")
    public UserThird findByObtyAccount(String account) {
        return userThirdRepository.findByObtyAccount(account);
    }

    public List<UserThird> findAllAcount(){
        return userThirdRepository.findAllAcount();
    }

    public List<UserThird> findAllGoldenfAccount(){
        return userThirdRepository.findAllGoldenfAccount();
    }

    public List<UserThird> findAllOBDJAccount() {
        return userThirdRepository.findAllOBDJAccount();
    }

    public List<UserThird> findAllOBTYAccount() {
        return userThirdRepository.findAllOBTYAccount();
    }
}

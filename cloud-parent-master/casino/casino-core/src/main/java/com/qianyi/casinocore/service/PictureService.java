package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.LunboPic;
import com.qianyi.casinocore.repository.PictureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@CacheConfig(cacheNames = {"picture"})
public class PictureService {
    @Autowired
    private PictureRepository pictureRepository;

//    @Cacheable(cacheNames = {"lunbo"},key = "#root.targetClass.simpleName+'_'+#root.methodName")
    public List<LunboPic> findAll() {
        return pictureRepository.findAll();
    }

    public List<LunboPic> findAll(Specification<LunboPic> condition) {
        return pictureRepository.findAll(condition);
    }
    public List<LunboPic> findByLunboPicList(Specification<LunboPic> condition, Sort sort){
        return pictureRepository.findAll(condition,sort);
    }

//    public LunboPic findLunboPicbyId(Long id){
//        return pictureRepository.findAllById(id);
//    }
//    public void deleteById(Long id){
//        pictureRepository.deleteById(id);
//    }

//    @CacheEvict(cacheNames = {"lunbo"}, allEntries = true)
    @CacheEvict(key="#p0.theShowEnd",allEntries = true)
    public void save(LunboPic lunboPic){
        pictureRepository.save(lunboPic);
    }

//    @Caching(evict = {@CacheEvict(value = "lunbo", key = "#args[0]", beforeInvocation = true, allEntries = true)}, cacheable = @Cacheable(value = "lunbo"))
//    @Cacheable(value = "lunbo",keyGenerator = "cusKeyGenerator")
//    @Cacheable(value = "lunbo",key = "#root.targetClass.simpleName+'_'+#root.methodName+'_'+#root.args[0]")
    @Cacheable(key = "#p0")
    public List<LunboPic> findByTheShowEnd(Integer theShowEnd) {
        return pictureRepository.findByTheShowEndAndUrlNotNull(theShowEnd);
    }

    public LunboPic findByNo(Integer no){
        return pictureRepository.findByNo(no);
    }
}

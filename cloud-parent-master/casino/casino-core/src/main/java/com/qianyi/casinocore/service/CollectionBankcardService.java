package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.repository.CollectionBankCardRepository;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"collectionBankcard"})
public class CollectionBankcardService {

    @Autowired
    private CollectionBankCardRepository collectionBankCardRepository;

    public List<CollectionBankcard> getCollectionBandcards(){
        return collectionBankCardRepository.findAll();
    }

    public List<CollectionBankcard> findAll(List<Long> ids){
        Specification<CollectionBankcard> condition = this.getCondition(ids);
        return collectionBankCardRepository.findAll(condition);
    }

    @Caching(evict = {
            @CacheEvict(key = "#p0.id"),
            @CacheEvict(key = "'disable::'+#p0.disable")
    })
    public void delete(CollectionBankcard collectionBankcard){
        collectionBankCardRepository.delete(collectionBankcard);
    }

    public Page<CollectionBankcard> getCollectionBandPage(CollectionBankcard collectionBankcard , Pageable pageable){
        Specification<CollectionBankcard> condition = this.getCondition(collectionBankcard);
        return collectionBankCardRepository.findAll(condition,pageable);
    }
    public List<CollectionBankcard> findAll(CollectionBankcard collectionBankcard ){
        Specification<CollectionBankcard> condition = this.getCondition(collectionBankcard);
        return collectionBankCardRepository.findAll(condition);
    }

    public List<CollectionBankcard> findByBankNo(String bankNo) {
        return collectionBankCardRepository.findByBankNo(bankNo);
    }

    @Caching(
            evict = @CacheEvict(key = "disable",allEntries = true),
            put = @CachePut(key="#result.id")
    )
    public CollectionBankcard save(CollectionBankcard bankcard) {
        return collectionBankCardRepository.save(bankcard);
    }

    @Cacheable(key="#p0")
    public CollectionBankcard findById(Long id) {
        Optional<CollectionBankcard> optional = collectionBankCardRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @CacheEvict(allEntries = true)
    public void saveAll(List<CollectionBankcard> collectionBankcardList) {
        collectionBankCardRepository.saveAll(collectionBankcardList);
    }

    public List<CollectionBankcard> findAll() {
        return collectionBankCardRepository.findAll();
    }


    public List<CollectionBankcard> findAllSort(Sort sort) {
        return collectionBankCardRepository.findAll(sort);
    }

    @Cacheable(key="'disable::' + #p0")
    public List<CollectionBankcard> findByDisableOrderBySortIdAsc(Integer disable){
        return collectionBankCardRepository.findByDisableOrderBySortIdAsc(disable);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<CollectionBankcard> getCondition(CollectionBankcard collectionBankcard ) {
        Specification<CollectionBankcard> specification = new Specification<CollectionBankcard>() {
            @Override
            public Predicate toPredicate(Root<CollectionBankcard> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (collectionBankcard.getDisable() != null) {
                    list.add(cb.equal(root.get("disable").as(Integer.class), collectionBankcard.getDisable()));
                }
                if (!CommonUtil.checkNull(collectionBankcard.getBankId())) {
                    list.add(cb.equal(root.get("bankId").as(String.class), (collectionBankcard.getBankId())));
                }
//                if (!CommonUtil.checkNull(loginLog.getAccount())) {
//                    list.add(cb.equal(root.get("account").as(String.class), loginLog.getAccount()));
//                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    private Specification<CollectionBankcard> getCondition(List<Long> ids) {
        Specification<CollectionBankcard> specification = new Specification<CollectionBankcard>() {
            @Override
            public Predicate toPredicate(Root<CollectionBankcard> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (ids != null && ids.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : ids) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

}

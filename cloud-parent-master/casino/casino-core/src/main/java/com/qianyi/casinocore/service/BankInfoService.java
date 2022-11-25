package com.qianyi.casinocore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.repository.BankInfoRepository;

import javax.persistence.criteria.*;

@Service
@CacheConfig(cacheNames = {"bankInfo"})
public class BankInfoService {
	
    @Autowired
    BankInfoRepository bankInfoRepository;

    public List<BankInfo> findAll() {
        return bankInfoRepository.findAll();
    }

    public List<BankInfo> findAll(BankInfo bankInfo) {
        Sort sort = Sort.by("id").descending();
        Specification<BankInfo> condition = getCondition(bankInfo);
        return bankInfoRepository.findAll(condition,sort);
    }

    public BankInfo findByBankName(String bankName){
        return bankInfoRepository.findByBankName(bankName);
    }

    @CachePut(key="#result.id",condition = "#result != null")
    public BankInfo saveBankInfo(BankInfo bankInfo){
        return bankInfoRepository.save(bankInfo);
    }

    @CacheEvict(key="#id")
    public void deleteBankInfo(Long id){
        bankInfoRepository.deleteById(id);
    }

    public void deleteBankInfoAll(){
        bankInfoRepository.deleteAllInBatch();
    }

    @Cacheable(key = "#id")
    public BankInfo findById(Long id) {
        Optional<BankInfo> info = bankInfoRepository.findById(id);
        if (info != null && info.isPresent()) {
            return info.get();
        }
        return null;
    }
    public List<BankInfo> findAll(List<String> ids){
        Specification<BankInfo> condition = this.getCondition(ids);
        return bankInfoRepository.findAll(condition);
    }

    private Specification<BankInfo> getCondition(BankInfo bankInfo) {
        Specification<BankInfo> specification = new Specification<BankInfo>() {
            @Override
            public Predicate toPredicate(Root<BankInfo> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (bankInfo.getDisable() != null) {
                    list.add(cb.equal(root.get("disable").as(Integer.class), bankInfo.getDisable()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    private Specification<BankInfo> getCondition(List<String> ids) {
        Specification<BankInfo> specification = new Specification<BankInfo>() {
            @Override
            public Predicate toPredicate(Root<BankInfo> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (ids != null && ids.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (String id : ids) {
                        try {
                            in.value(Long.valueOf(id));
                        }catch (Exception e){
                            continue;
                        }
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}

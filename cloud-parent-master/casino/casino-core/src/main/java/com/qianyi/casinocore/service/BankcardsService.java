package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.repository.BankcardsRepository;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"bankcards"})
@Transactional
public class BankcardsService {

    @Autowired
    BankcardsRepository bankcardsRepository;

    @Cacheable(key = "'" + Constants.REDIS_USERID + "'+#p0")
    public List<Bankcards> findBankcardsByUserId(Long userId){
        return bankcardsRepository.findBankcardsByUserIdOrderByDefaultCardDesc(userId);
    }

    @Caching(
            evict = @CacheEvict(key = "'" + Constants.REDIS_USERID + "'+#p0.userId"),
            put = @CachePut(key="#result.id")
    )
    public Bankcards boundCard(Bankcards bankcards){
        return bankcardsRepository.save(bankcards);
    }

    @Caching(evict = {
            @CacheEvict(key = "#p0.id"),
            @CacheEvict(key = "'" + Constants.REDIS_USERID + "'+#p0.userId")
    })
    public void delBankcards(Bankcards bankcards){
        bankcardsRepository.delete(bankcards);
    }

    public Bankcards findBankCardsInByUserId(Long userId) {
        return bankcardsRepository.findFirstByUserId(userId);
    }

    public int countByUserId(Long userId){
        return bankcardsRepository.countByUserId(userId);
    }

    public List<Bankcards> findAll(List<String> ids){
        Specification<Bankcards> condition = this.getCondition(ids);
        return bankcardsRepository.findAll(condition);
    }
    /**
     * 查询用户银行卡
     *
     * @param bankcards
     * @return
     */
    public List<Bankcards> findUserBank(Bankcards bankcards) {
        Specification<Bankcards> condition = this.getCondition(bankcards);
        List<Bankcards> bankcardsList = bankcardsRepository.findAll(condition);
        return bankcardsList;
    }

    private Specification<Bankcards> getCondition(Bankcards bankcards) {
        Specification<Bankcards> specification = new Specification<Bankcards>(){
            @Override
            public Predicate toPredicate(Root<Bankcards> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if(bankcards.getUserId() != null){
                    list.add(cb.equal(root.get("userId").as(Long.class), bankcards.getUserId()));
                }
                if(!CommonUtil.checkNull(bankcards.getBankId())){
                    list.add(cb.equal(root.get("bankId").as(String.class), bankcards.getBankId()));
                }
                if(!CommonUtil.checkNull(bankcards.getRealName())){
                    list.add(cb.equal(root.get("realName").as(String.class), bankcards.getRealName()));
                }
                if(!CommonUtil.checkNull(bankcards.getBankAccount())){
                    list.add(cb.equal(root.get("bankAccount").as(String.class), bankcards.getBankAccount()));
                }
                if (bankcards.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), bankcards.getFirstProxy()));
                }
                if (bankcards.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), bankcards.getSecondProxy()));
                }
                if (bankcards.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), bankcards.getThirdProxy()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    private Specification<Bankcards> getCondition(List<String> ids) {
        Specification<Bankcards> specification = new Specification<Bankcards>() {
            @Override
            public Predicate toPredicate(Root<Bankcards> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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
    @Cacheable(key = "#p0")
    public Bankcards findById(Long id) {
        Optional<Bankcards> optional = bankcardsRepository.findById(id);
        if (optional != null && optional.isPresent()) {
            Bankcards bankcards = optional.get();
            return bankcards;
        }
        return null;
    }

    @Caching(evict = {
            @CacheEvict(key = "#p0.id"),
            @CacheEvict(key = "'" + Constants.REDIS_USERID + "'+#p0.userId")
    })
    public void delete(Bankcards bankcards) {
        bankcardsRepository.delete(bankcards);
    }

    public Bankcards findByUserIdAndBankAccount(Long userId, String bankAccount) {
        return bankcardsRepository.findByUserIdAndBankAccount(userId,bankAccount);
    }

    public List<Bankcards> findByRealName(String realName) {
        return bankcardsRepository.findByRealName(realName);
    }

    public List<Bankcards> findByBankAccount(String bankAccount) {
        return bankcardsRepository.findByBankAccount(bankAccount);
    }

    public List<Bankcards> findByBankId(String bankId) {
        return bankcardsRepository.findByBankId(bankId);
    }
}


package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.repository.ProxyCommissionRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames = {"proxyCommission"})
public class ProxyCommissionService {
    @Autowired
    private ProxyCommissionRepository proxyCommissionRepository;



    @CachePut(key="#proxyCommission.proxyUserId")
    public ProxyCommission save(ProxyCommission proxyCommission){
        return proxyCommissionRepository.save(proxyCommission);
    }
    @Cacheable(key = "#proxyUserId")
    public ProxyCommission findByProxyUserId(Long proxyUserId){
        return proxyCommissionRepository.findByProxyUserId(proxyUserId);
    }

    public List<ProxyCommission> findBySecondProxy(Long secondProxy){
        return proxyCommissionRepository.findBySecondProxy(secondProxy);
    }
    public List<ProxyCommission> findProxyUser(List<Long> proxyUserIds) {
        Specification<ProxyCommission> condition = getCondition(proxyUserIds);
        List<ProxyCommission> proxyCommissionList = proxyCommissionRepository.findAll(condition);
        return proxyCommissionList;
    }
    private Specification<ProxyCommission> getCondition(List<Long> proxyUserIds) {
        Specification<ProxyCommission> specification = new Specification<ProxyCommission>() {
            @Override
            public Predicate toPredicate(Root<ProxyCommission> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (proxyUserIds != null && proxyUserIds.size() > 0) {
                    Path<Object> userId = root.get("proxyUserId");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : proxyUserIds) {
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

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.IpBlack;
import com.qianyi.casinocore.repository.IpBlackRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"ipBlackList"})
public class IpBlackService {
	
    @Autowired
    private IpBlackRepository ipBlackRepository;

    @Transactional
    @CachePut(key="#po.ip")
    public IpBlack save(IpBlack po){
        return ipBlackRepository.save(po);
    }

    @Cacheable(key = "#ip")
    public IpBlack findByIp(String ip) {
        return ipBlackRepository.findByIp(ip);
    }

    @CacheEvict(key="#po.ip")
    public void delete(IpBlack po){
        ipBlackRepository.delete(po);
    }

    public Page<IpBlack> findIpBlackPag(IpBlack po, Pageable pageable){
        Specification<IpBlack> condition = this.getCondition(po);
        Page<IpBlack> all = ipBlackRepository.findAll(condition, pageable);
        return all;
    }

    public IpBlack findById(Long id){
        Optional<IpBlack> byId = ipBlackRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
             */
    private Specification<IpBlack> getCondition(IpBlack po) {
        Specification<IpBlack> specification = new Specification<IpBlack>() {
            @Override
            public Predicate toPredicate(Root<IpBlack> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (po.getStatus() != null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), po.getStatus()));
                }
                if (!CommonUtil.checkNull(po.getIp())) {
                    list.add(cb.equal(root.get("ip").as(String.class), po.getIp()));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}

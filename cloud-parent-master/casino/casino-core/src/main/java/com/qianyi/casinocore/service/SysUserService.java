package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.SysUserRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@CacheConfig(cacheNames = {"sysUser"})
public class SysUserService {

    @Autowired
    private SysUserRepository sysUserRepository;

    public List<SysUser> findAll() {
        return sysUserRepository.findAll();
    }

    public SysUser findByUserName(String userName) {
        return sysUserRepository.findByUserName(userName);
    }
    @CachePut(key="#sysUser.id",condition = "#sysUser != null")
    public SysUser save(SysUser sysUser) {
        return sysUserRepository.save(sysUser);
    }
    @Cacheable(key = "#id")
    public SysUser findAllById(Long id){
        return sysUserRepository.findAllById(id);
    }
    @Cacheable(key = "#id")
    public SysUser findById(Long id){
        Optional<SysUser> optional = sysUserRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
    public List<SysUser> findAll(List<String> sysUserIds) {
        Specification<SysUser> condition = getCondition(sysUserIds);
        List<SysUser> userList = sysUserRepository.findAll(condition);
        return userList;
    }

    public List<SysUser> findAllLong(List<Long> sysUserIds) {
        Specification<SysUser> condition = getConditionLong(sysUserIds);
        List<SysUser> userList = sysUserRepository.findAll(condition);
        return userList;
    }

    private Specification<SysUser> getCondition(List<String> sysUserIds) {
        Specification<SysUser> specification = new Specification<SysUser>() {
            @Override
            public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (sysUserIds != null && sysUserIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (String id : sysUserIds) {
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

    private Specification<SysUser> getConditionLong(List<Long> sysUserIds) {
        Specification<SysUser> specification = new Specification<SysUser>() {
            @Override
            public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (sysUserIds != null && sysUserIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : sysUserIds) {
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

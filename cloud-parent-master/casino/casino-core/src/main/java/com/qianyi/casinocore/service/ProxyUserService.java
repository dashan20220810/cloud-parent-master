package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.repository.ProxyUserRepository;
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

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"proxyUser"})
public class ProxyUserService {

    @Autowired
    private ProxyUserRepository proxyUserRepository;

    public ProxyUser findProxyUserById(Long id){
        return proxyUserRepository.findProxyUserById(id);
    }

    public List<ProxyUser> findAll() {
        return proxyUserRepository.findAll();
    }

    public ProxyUser findByUserName(String userName) {
        return proxyUserRepository.findByUserName(userName);
    }
    @CachePut(key="#result.id",condition = "#result != null")
    public ProxyUser save(ProxyUser proxyUser) {
        return proxyUserRepository.save(proxyUser);
    }

    @Cacheable(key = "#id")
    public ProxyUser findById(Long id){
        Optional<ProxyUser> optional = proxyUserRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
    @CacheEvict(key="#id")
    public void deleteById(Long id){
        proxyUserRepository.deleteById(id);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void subProxyUsersNum(Long id) {
        synchronized (id.toString().intern()) {
            proxyUserRepository.subProxyUsersNum(id);
        }
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void addProxyUsersNum(Long id) {
        synchronized (id.toString().intern()) {
            proxyUserRepository.addProxyUsersNum(id);
        }
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void makeZero(Long id) {
        synchronized (id.toString().intern()) {
            proxyUserRepository.makeZero(id);
        }
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void addProxyUsersNum(Long id,Integer num) {
        synchronized (id.toString().intern()) {
            proxyUserRepository.addProxyUsersNum(id,num);
        }
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void subProxyUsersNum(Long id,Integer num) {
        synchronized (id.toString().intern()) {
            proxyUserRepository.subProxyUsersNum(id,num);
        }
    }
    /**
     * 用户列表查询
     *
     * @param proxyUser
     * @return
     */
    public Page<ProxyUser> findProxyUserPage(Pageable pageable, ProxyUser proxyUser, Date startDate, Date endDate) {
        Specification<ProxyUser> condition = this.getCondition(proxyUser,startDate,endDate);
        return proxyUserRepository.findAll(condition, pageable);
    }
    public List<ProxyUser> findProxyUserList(ProxyUser proxyUser) {
        Specification<ProxyUser> condition = this.getCondition(proxyUser,null,null);
        return proxyUserRepository.findAll(condition);
    }
    public List<ProxyUser> findProxyUserList(ProxyUser proxyUser,Date startDate, Date endDate) {
        Specification<ProxyUser> condition = this.getCondition(proxyUser,startDate,endDate);
        return proxyUserRepository.findAll(condition);
    }

    public Long findProxyUserCount(ProxyUser proxyUser,Date startDate, Date endDate) {
        Specification<ProxyUser> condition = this.getCondition(proxyUser,startDate,endDate);
        return proxyUserRepository.count(condition);
    }

    public List<ProxyUser> findProxyUser(List<Long> proxyUserIds) {
        Specification<ProxyUser> condition = getCondition(proxyUserIds);
        List<ProxyUser> proxyUserList = proxyUserRepository.findAll(condition);
        return proxyUserList;
    }
    public List<ProxyUser> findProxyUsers(List<String> proxyUserIds) {
        Specification<ProxyUser> condition = getConditions(proxyUserIds);
        List<ProxyUser> proxyUserList = proxyUserRepository.findAll(condition);
        return proxyUserList;
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param proxyUser
     * @return
     */
    private Specification<ProxyUser> getCondition(ProxyUser proxyUser, Date startDate, Date endDate) {
        Specification<ProxyUser> specification = new Specification<ProxyUser>() {
            @Override
            public Predicate toPredicate(Root<ProxyUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(proxyUser.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), proxyUser.getUserName()));
                }
                if (proxyUser.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Integer.class), proxyUser.getProxyRole()));
                }
                if (proxyUser.getUserFlag() != null) {
                    list.add(cb.equal(root.get("userFlag").as(Integer.class), proxyUser.getUserFlag()));
                }
                if (proxyUser.getIsDelete() != null) {
                    list.add(cb.equal(root.get("isDelete").as(Integer.class), proxyUser.getIsDelete()));
                }
                if (proxyUser.getId() != null) {
                    list.add(cb.equal(root.get("id").as(Long.class), proxyUser.getId()));
                }
                if (proxyUser.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), proxyUser.getFirstProxy()));
                }
                if (proxyUser.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), proxyUser.getSecondProxy()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    private Specification<ProxyUser> getCondition(List<Long> userIds) {
        Specification<ProxyUser> specification = new Specification<ProxyUser>() {
            @Override
            public Predicate toPredicate(Root<ProxyUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    private Specification<ProxyUser> getConditions(List<String> userIds) {
        Specification<ProxyUser> specification = new Specification<ProxyUser>() {
            @Override
            public Predicate toPredicate(Root<ProxyUser> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (String id : userIds) {
                        try {
                            in.value(Long.valueOf(id));
                        }catch (Exception ex){
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

    public ProxyUser findByProxyCode(String inviteCode) {
        return proxyUserRepository.findByProxyCode(inviteCode);
    }
}

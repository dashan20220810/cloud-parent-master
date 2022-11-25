package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.ProxyReportRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProxyReportService {
    @Autowired
    private ProxyReportRepository proxyReportRepository;
    @Autowired
    private UserService userService;

    public ProxyReport findByUserId(Long userId){
        return proxyReportRepository.findProxyReportByUserId(userId);
    }

    public ProxyReport findByUserIdWithLock(Long userId){
        return proxyReportRepository.findByUserId(userId);
    }

    public ProxyReport save(ProxyReport proxyReport){
        return proxyReportRepository.save(proxyReport);
    }

    public List<ProxyReport> saveAll(List<ProxyReport> proxyReportList){
        return proxyReportRepository.saveAll(proxyReportList);
    }

    public Page<ProxyReport> findAchievementPage(Pageable pageable, List<User> users, String account) {
        Specification<ProxyReport> condition = this.getCondition(users,account);
        return proxyReportRepository.findAll(condition, pageable);
    }


    private Specification<ProxyReport> getCondition(List<User> users, String account) {
        Specification<ProxyReport> specification = new Specification<ProxyReport>() {
            @Override
            public Predicate toPredicate(Root<ProxyReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                CriteriaBuilder.In<Object> in = cb.in(root.get("userId"));
                for (User user : users) {
                    in.value(user.getId());
                }
                list.add(cb.and(cb.and(in)));
                if (!ObjectUtils.isEmpty(account)) {
                    list.add(cb.like(root.get("account").as(String.class), "%" + account + "%"));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}

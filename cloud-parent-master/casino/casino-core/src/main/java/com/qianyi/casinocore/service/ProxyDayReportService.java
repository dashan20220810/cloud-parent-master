package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.repository.ProxyDayReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
/*@Transactional*/
public class ProxyDayReportService {
    @Autowired
    private ProxyDayReportRepository proxyDayReportRepository;

    public ProxyDayReport findByUserIdAndDay(Long userId,String day){
        return proxyDayReportRepository.findProxyDayReportByUserIdAndDayTime(userId,day);
    }

    public ProxyDayReport findByUserIdAndDayWithLock(Long userId,String day){
        return proxyDayReportRepository.findByUserIdAndDayTime(userId,day);
    }

    public ProxyDayReport save(ProxyDayReport proxyDayReport){
        return proxyDayReportRepository.save(proxyDayReport);
    }

    public List<ProxyDayReport> saveAll(List<ProxyDayReport> proxyDayReportList){
        return proxyDayReportRepository.saveAll(proxyDayReportList);
    }

    public List<ProxyDayReport> getCommission(Long userId, String startTime, String endTime) {
        Specification<ProxyDayReport> condition = this.getCondition(userId, startTime, endTime);
        return proxyDayReportRepository.findAll(condition);
    }
    //@CacheEvict(key = "#userId")
/*    @Transactional*/
    public void updateProxyDayReport(Long userId,BigDecimal profitAmount, BigDecimal betAmount,String dayTime){
        proxyDayReportRepository.updateProxyDayReport(profitAmount,betAmount,userId,dayTime);
    }



    private Specification<ProxyDayReport> getCondition(Long userId, String startTime, String endTime) {
        Specification<ProxyDayReport> specification = new Specification<ProxyDayReport>() {
            @Override
            public Predicate toPredicate(Root<ProxyDayReport> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userId != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                if (!ObjectUtils.isEmpty(startTime) && ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.equal(root.get("dayTime").as(String.class), startTime));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("dayTime").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}

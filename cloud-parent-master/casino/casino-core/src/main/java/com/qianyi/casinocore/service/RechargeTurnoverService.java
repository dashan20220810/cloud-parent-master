package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.RechargeTurnover;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.RechargeTurnoverRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RechargeTurnoverService {

    @Autowired
    private RechargeTurnoverRepository rechargeTurnoverRepository;

    public void save(RechargeTurnover turnover) {
        rechargeTurnoverRepository.save(turnover);
    }

    public Page<RechargeTurnover> findUserPage(Pageable pageable, RechargeTurnover rechargeTurnover) {
        Specification<RechargeTurnover> condition = this.getCondition(rechargeTurnover);
        Page<RechargeTurnover> rechargeTurnoverPage = rechargeTurnoverRepository.findAll(condition, pageable);
        return rechargeTurnoverPage;
    }

    private Specification<RechargeTurnover> getCondition(RechargeTurnover rechargeTurnover) {
        Specification<RechargeTurnover> specification = new Specification<RechargeTurnover>() {
            @Override
            public Predicate toPredicate(Root<RechargeTurnover> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (rechargeTurnover.getUserId() != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), rechargeTurnover.getUserId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }


    public Page<RechargeTurnover> findUserPage(Pageable pageable, Long userId,String startTime,String endTime) {
        Specification<RechargeTurnover> condition = this.getCondition(userId,startTime,endTime);
        Page<RechargeTurnover> rechargeTurnoverPage = rechargeTurnoverRepository.findAll(condition, pageable);
        return rechargeTurnoverPage;
    }

    private Specification<RechargeTurnover> getCondition(Long userId,String startTime,String endTime) {
        Specification<RechargeTurnover> specification = new Specification<RechargeTurnover>() {
            @Override
            public Predicate toPredicate(Root<RechargeTurnover> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userId != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                if(!ObjectUtils.isEmpty(startTime)&&!ObjectUtils.isEmpty(endTime)){
                    list.add(cb.between(root.get("createTime").as(String.class), startTime,endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}

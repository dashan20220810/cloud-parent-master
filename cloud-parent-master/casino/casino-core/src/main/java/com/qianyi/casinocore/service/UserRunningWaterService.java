package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.repository.UserRunningWaterRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UserRunningWaterService {
    @Autowired
    private UserRunningWaterRepository userRunningWaterRepository;

    @Autowired
    private EntityManager entityManager;

    public void updateKey(Long userId, String staticsTimes , BigDecimal amount, BigDecimal commission,Long firstProxy,Long secondProxy,Long thirdProxy){
        userRunningWaterRepository.updateKey(userId,staticsTimes,amount,commission,firstProxy,secondProxy,thirdProxy);
    }

    public List<UserRunningWater> findByStaticsTimes(String staticsTimes){
        return userRunningWaterRepository.findByStaticsTimes(staticsTimes);
    }

    @Transactional
    public void updateFirstProxy(Long userId, Long firstProxy){
        userRunningWaterRepository.updateFirstProxy(userId,firstProxy);
    }

    @Transactional
    public void updateSecondProxy(Long userId, Long firstProxy){
        userRunningWaterRepository.updateSecondProxy(userId,firstProxy);
    }

    @Transactional
    public void updatetThirdProxy(Long userId, Long firstProxy){
        userRunningWaterRepository.updatetThirdProxy(userId,firstProxy);
    }

    public Page<UserRunningWater> findUserPage(Pageable pageable, UserRunningWater userRunningWater, String startTime, String endTime){
        Specification<UserRunningWater> condition = this.getCondition(userRunningWater,startTime,endTime);
        return userRunningWaterRepository.findAll(condition,pageable);
    }

    public List<UserRunningWater> findUserRunningWaters(Sort sort,UserRunningWater userRunningWater, String startTime, String endTime){
        Specification<UserRunningWater> condition = this.getCondition(userRunningWater,startTime,endTime);
        return userRunningWaterRepository.findAll(condition,sort);
    }

    private Specification<UserRunningWater> getCondition(UserRunningWater userRunningWater, String startTime, String endTime) {
        Specification<UserRunningWater> specification = new Specification<UserRunningWater>() {
            @Override
            public Predicate toPredicate(Root<UserRunningWater> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (userRunningWater.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userRunningWater.getUserId()));
                }
//                if (startDate != null) {
//                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
//                }
//                if (endDate != null) {
//                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
//                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("staticsTimes").as(String.class), startTime, endTime)
                    );
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    public List<UserRunningWater> findUserRunningWaterList(UserRunningWater userRunningWater, String startTime, String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserRunningWater> query = builder.createQuery(UserRunningWater.class);
        Root<UserRunningWater> root = query.from(UserRunningWater.class);
        query.multiselect(
                root.get("userId").as(Long.class)
        );

        List<Predicate> predicates = new ArrayList();

        if (userRunningWater.getUserId() != null) {
            predicates.add(builder.equal(root.get("userId").as(Long.class), userRunningWater.getUserId()));
        }
        if (userRunningWater.getFirstProxy() != null) {
            predicates.add(builder.equal(root.get("firstProxy").as(Long.class), userRunningWater.getFirstProxy()));
        }
        if (userRunningWater.getSecondProxy() != null) {
            predicates.add(builder.equal(root.get("secondProxy").as(Long.class), userRunningWater.getSecondProxy()));
        }
        if (userRunningWater.getThirdProxy() != null) {
            predicates.add(builder.equal(root.get("thirdProxy").as(Long.class), userRunningWater.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(builder.between(root.get("staticsTimes").as(String.class), startTime, endTime));
        }
        query.where(predicates.toArray(new Predicate[predicates.size()])).groupBy(root.get("userId"));

        List<UserRunningWater> list = entityManager.createQuery(query).getResultList();
        return list;
    }
}

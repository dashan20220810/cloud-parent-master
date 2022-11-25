package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.WashCodeChange;
import com.qianyi.casinocore.repository.ShareProfitChangeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ShareProfitChangeService {

    @Autowired
    private ShareProfitChangeRepository shareProfitChangeRepository;
    @Autowired
    private EntityManager entityManager;

    public ShareProfitChange save(ShareProfitChange shareProfitChange){
        return shareProfitChangeRepository.save(shareProfitChange);
    }

    public List<ShareProfitChange> saveAll(List<ShareProfitChange> shareProfitChangeList){
        return shareProfitChangeRepository.saveAll(shareProfitChangeList);
    }

    public List<Map<String, Object>> findSumAmount(String startTime, String endTime){
        return shareProfitChangeRepository.findSumAmount(startTime,endTime);
    }

    public List<ShareProfitChange> findAll(Long fromUserId,Long userId,Date startDate, Date endDate){
        Specification<ShareProfitChange> condition = getCondition(fromUserId,userId,startDate,endDate);
        return shareProfitChangeRepository.findAll(condition);
    }
    public ShareProfitChange findByUserIdAndOrderNo(Long userId,String orderNo){
        return shareProfitChangeRepository.findByUserIdAndOrderNo(userId,orderNo);
    }

    public BigDecimal sumAmount(String startTime, String endTime){
        return shareProfitChangeRepository.sumAmount(startTime,endTime);
    }

    public BigDecimal sumAmount(){
        return shareProfitChangeRepository.sumAmount();
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param fromUserId
     * @return
     */
    private Specification<ShareProfitChange> getCondition(Long fromUserId,Long userId, Date startDate, Date endDate) {
        Specification<ShareProfitChange> specification = new Specification<ShareProfitChange>() {
            @Override
            public Predicate toPredicate(Root<ShareProfitChange> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (fromUserId != null) {
                    list.add(cb.equal(root.get("fromUserId").as(Long.class), fromUserId));
                }
                if (userId != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("betTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("betTime").as(Date.class),endDate));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<ShareProfitChange> getShareProfitList(Long userId, Integer parentLevel, Long directUserId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ShareProfitChange> query = builder.createQuery(ShareProfitChange.class);
        Root<ShareProfitChange> root = query.from(ShareProfitChange.class);
        query.multiselect(
            root.get("userId").as(Long.class),
            root.get("fromUserId").as(Long.class),
            builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
            builder.sum(root.get("validbet").as(BigDecimal.class)).alias("validbet")
        );
        List<Predicate> predicates = new ArrayList();
        if(directUserId!=null){
            predicates.add(builder.equal(root.get("fromUserId").as(Long.class), directUserId));
        }
        predicates.add(builder.equal(root.get("userId").as(Long.class), userId));
        predicates.add(builder.equal(root.get("type").as(Integer.class), 1));
        predicates.add(builder.equal(root.get("parentLevel").as(Integer.class), parentLevel));
        query.where(predicates.toArray(new Predicate[predicates.size()])).groupBy(root.get("fromUserId"));
        List<ShareProfitChange> list = entityManager.createQuery(query).getResultList();
        return list;
    }

    public List<Map<String, Object>> getMapSumAmount(String startTime, String endTime){
        return shareProfitChangeRepository.getMapSumAmount(startTime,endTime);
    }
}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.co.charge.ChargeOrderCo;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.repository.ChargeOrderRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ChargeOrderService {

    @Autowired
    private ChargeOrderRepository chargeOrderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public static final List<Integer> statusList = new ArrayList<>();

    static {
        statusList.add(1);
        statusList.add(4);
    }

    public ChargeOrder saveOrder(ChargeOrder entity){
        return chargeOrderRepository.save(entity);
    }

    public Page<ChargeOrder> findChargeOrderPage(ChargeOrder chargeOrder, Pageable pageable,Date startDate,Date endDate){
        Specification<ChargeOrder> condition = getCondition(chargeOrder,startDate,endDate);
        return chargeOrderRepository.findAll(condition,pageable);
    }

    public List<ChargeOrder> findListByUpdate(ChargeOrder chargeOrder,Date startDate,Date endDate){
        Specification<ChargeOrder> condition = getConditionByUpdate(chargeOrder,startDate,endDate);
        return chargeOrderRepository.findAll(condition);
    }

    /**
     * 查询所有成功的充值订单
     * @param co
     * @return
     */
    public List<ChargeOrder> findSuccessedListByUpdate(ChargeOrderCo co){
        Specification<ChargeOrder> condition = (root, q, cb) -> {
            Predicate predicate = cb.conjunction();
            List<Predicate> list = new ArrayList<>();
            list.add(
                    cb.or(
                            // 1.成功
                            cb.equal(root.get("status").as(Integer.class), 1),
                            // 4.总控上分
                            cb.equal(root.get("status").as(Integer.class), 4)
                    )
            );
            if (co.getStartDate() != null) {
                list.add(cb.greaterThanOrEqualTo(root.get("updateTime").as(Date.class), co.getStartDate()));
            }
            if (co.getEndDate() != null) {
                list.add(cb.lessThanOrEqualTo(root.get("updateTime").as(Date.class), co.getEndDate()));
            }

            if (co.getFirstProxy() != null) {
                list.add(cb.equal(root.get("firstProxy").as(Long.class), co.getFirstProxy()));
            }
            if (co.getSecondProxy() != null) {
                list.add(cb.equal(root.get("secondProxy").as(Long.class), co.getSecondProxy()));
            }
            if (co.getThirdProxy() != null) {
                list.add(cb.equal(root.get("thirdProxy").as(Long.class), co.getThirdProxy()));
            }

            predicate = cb.and(list.toArray(new Predicate[list.size()]));

            return predicate;
        };
        return chargeOrderRepository.findAll(condition);
    }

    public Page<ChargeOrder> findChargeOrderPage(Specification<ChargeOrder> condition, Pageable pageable){
        return chargeOrderRepository.findAll(condition,pageable);
    }
    public ChargeOrder findChargeOrderByIdUseLock(Long id){
        ChargeOrder chargeOrder = chargeOrderRepository.findChargeOrderByIdUseLock(id);
        return chargeOrder;
    }

    public ChargeOrder findById(Long id){
        Optional<ChargeOrder> optional = chargeOrderRepository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Transactional
    public void updateChargeOrders(Integer status,String time){
        chargeOrderRepository.updateChargeOrders(status,time);
    }

    @Transactional
    public void updateChargeOrdersRemark(String remark,Long id){
        chargeOrderRepository.updateChargeOrdersRemark(remark,id);
    }

    public Integer countByUserIdAndStatus(Long userId,int status) {
        return chargeOrderRepository.countByUserIdAndStatus(userId,status);
    }

    public List<ChargeOrder> findAll(List<Long> orderIds) {
        Specification<ChargeOrder> condition = getCondition(orderIds);
        List<ChargeOrder> userList = chargeOrderRepository.findAll(condition);
        return userList;
    }

    private Specification<ChargeOrder> getCondition(List<Long> userIds) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
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

    private Specification<ChargeOrder> getConditionByUpdate(ChargeOrder chargeOrder,Date startDate,Date endDate) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (chargeOrder.getStatus() !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), chargeOrder.getStatus()));
                }
                if(chargeOrder.getType() != null){
                    list.add(cb.equal(root.get("type").as(Integer.class), chargeOrder.getType()));
                }
                if (!CommonUtil.checkNull(chargeOrder.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), chargeOrder.getOrderNo()));
                }
                if (chargeOrder.getUserId() != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), chargeOrder.getUserId()));
                }
                if (chargeOrder.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), chargeOrder.getFirstProxy()));
                }
                if (chargeOrder.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), chargeOrder.getSecondProxy()));
                }
                if (chargeOrder.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), chargeOrder.getThirdProxy()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("updateTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("updateTime").as(Date.class),endDate));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));

                return predicate;
            }
        };
        return specification;
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ChargeOrder> getCondition(ChargeOrder chargeOrder,Date startDate,Date endDate) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (chargeOrder.getStatus() !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), chargeOrder.getStatus()));
                }
                if(chargeOrder.getType() != null){
                    list.add(cb.equal(root.get("type").as(Integer.class), chargeOrder.getType()));
                }
                if (!CommonUtil.checkNull(chargeOrder.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), chargeOrder.getOrderNo()));
                }
                if (chargeOrder.getUserId() != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), chargeOrder.getUserId()));
                }
                if (chargeOrder.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), chargeOrder.getFirstProxy()));
                }
                if (chargeOrder.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), chargeOrder.getSecondProxy()));
                }
                if (chargeOrder.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), chargeOrder.getThirdProxy()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));

                return predicate;
            }
        };
        return specification;
    }

    public  ChargeOrder  findChargeOrderSum(ChargeOrder chargeOrder,Date startDate,Date endDatee) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChargeOrder> query = builder.createQuery(ChargeOrder.class);
        Root<ChargeOrder> root = query.from(ChargeOrder.class);

        query.multiselect(
                builder.sum(root.get("chargeAmount").as(BigDecimal.class)).alias("chargeAmount")
        );
        List<Predicate> predicates = new ArrayList();

        if (chargeOrder.getStatus() != null) {
            predicates.add(
                    builder.equal(root.get("status").as(Integer.class), chargeOrder.getStatus())
            );
        }
        if (chargeOrder.getType() != null) {
            predicates.add(
                    builder.equal(root.get("type").as(Integer.class), chargeOrder.getType())
            );
        }
        if (chargeOrder.getUserId() != null) {
            predicates.add(
                    builder.equal(root.get("userId").as(Long.class), chargeOrder.getUserId())
            );
        }
        if (!CommonUtil.checkNull(chargeOrder.getOrderNo())) {
            predicates.add(
                    builder.equal(root.get("orderNo").as(String.class), chargeOrder.getOrderNo())
            );
        }
        if (chargeOrder.getFirstProxy() != null) {
            predicates.add(
                    builder.equal(root.get("firstProxy").as(Long.class), chargeOrder.getFirstProxy())
            );
        }
        if (chargeOrder.getSecondProxy() != null) {
            predicates.add(
                    builder.equal(root.get("secondProxy").as(Long.class), chargeOrder.getSecondProxy())
            );
        }
        if (chargeOrder.getThirdProxy() != null) {
            predicates.add(
                    builder.equal(root.get("thirdProxy").as(Long.class), chargeOrder.getThirdProxy())
            );
        }
        if (startDate != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
        }
        if (endDatee != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDatee));
        }
        query
                .where(predicates.toArray(new Predicate[predicates.size()]));
//                .groupBy(root.get("conversionStepCode"))
//                .orderBy(builder.desc(root.get("contactUserNums")));
        ChargeOrder singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public  ChargeOrder  sumChargeOrder(ChargeOrder chargeOrder,Date startDate,Date endDatee) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChargeOrder> query = builder.createQuery(ChargeOrder.class);
        Root<ChargeOrder> root = query.from(ChargeOrder.class);

        query.multiselect(
            builder.sum(root.get("chargeAmount").as(BigDecimal.class)).alias("chargeAmount")
        );
        List<Predicate> predicates = new ArrayList();

        if (chargeOrder.getStatus() != null) {
            predicates.add(
                builder.equal(root.get("status").as(Integer.class), chargeOrder.getStatus())
            );
        }
        if (chargeOrder.getFirstProxy() != null) {
            predicates.add(
                builder.equal(root.get("firstProxy").as(Long.class), chargeOrder.getFirstProxy())
            );
        }
        if (chargeOrder.getSecondProxy() != null) {
            predicates.add(
                builder.equal(root.get("secondProxy").as(Long.class), chargeOrder.getSecondProxy())
            );
        }
        if (chargeOrder.getThirdProxy() != null) {
            predicates.add(
                builder.equal(root.get("thirdProxy").as(Long.class), chargeOrder.getThirdProxy())
            );
        }
        if (startDate != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("updateTime").as(Date.class), startDate));
        }
        if (endDatee != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("updateTime").as(Date.class),endDatee));
        }
        query
            .where(predicates.toArray(new Predicate[predicates.size()]));
        ChargeOrder singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public BigDecimal sumChargeAmount(){
        return chargeOrderRepository.sumChargeAmount();
    }

    public List<ChargeOrder> getChargeNums(Set<Long> userIds){
        if (userIds == null || userIds.size() == 0) {
            return null;
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChargeOrder> criteriaQuery = criteriaBuilder.createQuery(ChargeOrder.class);
        Root<ChargeOrder> root = criteriaQuery.from(ChargeOrder.class);
        List<Predicate> predicates = new ArrayList();

        Path<Object> userId = root.get("userId");
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(userId);
        for (Long id : userIds) {
            in.value(id);
        }
        predicates.add(criteriaBuilder.and(criteriaBuilder.and(in)));

        Path<Object> status = root.get("status");
        CriteriaBuilder.In<Object> statusin = criteriaBuilder.in(status);
        for (Integer id : statusList) {
            statusin.value(id);
        }
        predicates.add(criteriaBuilder.and(criteriaBuilder.and(statusin)));
        criteriaQuery.groupBy(root.get("userId"));
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        List<ChargeOrder> counts = entityManager.createQuery(criteriaQuery).getResultList();
        return counts;
    }
}

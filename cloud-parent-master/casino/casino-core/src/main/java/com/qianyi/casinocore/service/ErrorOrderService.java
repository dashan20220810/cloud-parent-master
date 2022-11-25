package com.qianyi.casinocore.service;

import com.qianyi.casinocore.business.SupplementBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.repository.ChargeOrderRepository;
import com.qianyi.casinocore.repository.ErrorOrderRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
@Slf4j
@Transactional
public class ErrorOrderService {


    @Autowired
    private ErrorOrderRepository errorOrderRepository;
    @Autowired
    private SupplementBusiness supplementBusiness;

    public Page<ErrorOrder> findErrorOrderPage(ErrorOrder order,Pageable pageable, Date startDate, Date endDate){
        Specification<ErrorOrder> condition = getCondition(order,startDate,endDate);
        return errorOrderRepository.findAll(condition,pageable);
    }

    public ErrorOrder findErrorOrderByIdUseLock(Long id){
        ErrorOrder errorOrder = errorOrderRepository.findErrorOrderByIdUseLock(id);
        return errorOrder;
    }

    @Transactional
    public void updateErrorOrdersRemark(Integer status,Long id){
        errorOrderRepository.updateErrorOrdersRemark(status,id);
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ErrorOrder> getCondition(ErrorOrder order, Date startDate, Date endDate) {
        Specification<ErrorOrder> specification = new Specification<ErrorOrder>() {
            @Override
            public Predicate toPredicate(Root<ErrorOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(order.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), order.getUserName()));
                }
                if(!CommonUtil.checkNull(order.getPlatform())){
                    list.add(cb.equal(root.get("platform").as(String.class), order.getPlatform()));
                }

                if (order.getStatus() !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), order.getStatus()));
                }
                if(order.getType() != null){
                    list.add(cb.equal(root.get("type").as(Integer.class), order.getType()));
                }
                if (!CommonUtil.checkNull(order.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), order.getOrderNo()));
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


    @Async("asyncExecutor")
    public void syncSaveErrorOrder(String thirdAccount, Long userId, String account, String orderNo, BigDecimal money, AccountChangeEnum ChangeEnum, String platform) {
        log.info("开始记录异常订单，userId:{},account:{},money:{}",userId,account,money);
        ErrorOrder errorOrder = new ErrorOrder();
        errorOrder.setUserId(userId);
        errorOrder.setUserName(account);
        errorOrder.setStatus(0);
        errorOrder.setOrderNo(orderNo);
        errorOrder.setMoney(money.abs());
        errorOrder.setType(ChangeEnum.getType());
        errorOrder.setPlatform(platform);
        ErrorOrder order = errorOrderRepository.save(errorOrder);
        log.info("异常订单保存成功，errorOrder:{}",errorOrder.toString());
        //尝试3次补单
//        supplementBusiness.trySupplement(order, thirdAccount);
    }

    public void save(ErrorOrder errorOrder) {
        errorOrderRepository.save(errorOrder);
    }

}

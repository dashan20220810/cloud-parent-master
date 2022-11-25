package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * 充值申请
 */
@Slf4j
@RestController
@RequestMapping("/chargeOrder")
@Api(tags = "资金中心")
public class ChargeOrderController {
    @Autowired
    private ChargeOrderService chargeOrderService;
    /**
     * 用户充值列表
     *
     * @param status 状态:1.入款审核中,2.入款成功，3.入款已取消
     * @return
     */
    @ApiOperation("用户充值列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "status", value = "状态:全部.不传值，0.入款审核中,1.入款成功，2.入款已取消,4.总控上分 5.代理上分", required = false),
            @ApiImplicitParam(name = "date", value = "时间：全部：不传值，0：今天，1：昨天，2：一个月内", required = false)
    })
    @GetMapping("/chargeOrderList")
    public ResponseEntity<Page<ChargeOrder>> chargeOrderList(Integer pageSize, Integer pageCode, Integer status, String date){
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoWebUtil.setPageable(pageCode, pageSize, sort);
        String startTime = null;
        String endTime = null;
        if ("0".equals(date)) {
            startTime = DateUtil.getStartTime(0);
            endTime = DateUtil.getEndTime(0);
        } else if ("1".equals(date)) {
            startTime = DateUtil.getStartTime(-1);
            endTime = DateUtil.getEndTime(-1);
        } else if ("2".equals(date)) {
            startTime = DateUtil.getMonthAgoStartTime(-1);
            endTime = DateUtil.getEndTime(0);
        }
        Specification<ChargeOrder> condition = this.getCondition(userId,status,startTime,endTime);
        Page<ChargeOrder> chargeOrderPage = chargeOrderService.findChargeOrderPage(condition, pageable);
//        List<ChargeOrder> content = chargeOrderPage.getContent();
//        if (!CollectionUtils.isEmpty(content)) {
//            for (ChargeOrder chargeOrder : content) {
//                Integer orderStatus = chargeOrder.getStatus();
//                //总控和代理的操作为人工操作
//                if (orderStatus != null && (orderStatus == 4 || orderStatus == 5)) {
//                    chargeOrder.setRemitType(4);
//                }
//            }
//        }
        return ResponseUtil.success(chargeOrderPage);
    }

    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<ChargeOrder> getCondition(Long userId,Integer status,String startTime,String endTime) {
        Specification<ChargeOrder> specification = new Specification<ChargeOrder>() {
            @Override
            public Predicate toPredicate(Root<ChargeOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (status !=null) {
                    list.add(cb.equal(root.get("status").as(Integer.class), status));
                }
                if (userId != null ) {
                    list.add(cb.equal(root.get("userId").as(Long.class), userId));
                }
                if(!ObjectUtils.isEmpty(startTime)&&!ObjectUtils.isEmpty(endTime)){
                    list.add(cb.between(root.get("createTime").as(String.class), startTime,endTime));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));

                return predicate;
            }
        };
        return specification;
    }
}

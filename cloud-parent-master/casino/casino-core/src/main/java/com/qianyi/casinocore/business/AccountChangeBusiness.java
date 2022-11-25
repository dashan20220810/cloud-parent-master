package com.qianyi.casinocore.business;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;

/**
 * 账变中心
 */
@Slf4j
@Service
public class AccountChangeBusiness {

    @Autowired
    private AccountChangeService accountChangeService;
    @Autowired
    private UserService userService;

    public AccountChange save(AccountChangeVo vo){
        if (ObjectUtils.isEmpty(vo.getOrderNo())){
            String orderNo = getOrderNo(vo.getChangeEnum());
            vo.setOrderNo(orderNo);
        }
        AccountChange change=new AccountChange();
        BeanUtils.copyProperties(vo,change);
        change.setType(vo.getChangeEnum().getType());
        User user = userService.findById(vo.getUserId());
        if (user != null) {
            change.setFirstProxy(user.getFirstProxy());
            change.setSecondProxy(user.getSecondProxy());
            change.setThirdProxy(user.getThirdProxy());
        }
        AccountChange accountChange = accountChangeService.save(change);
        log.info("账变中心数据记录成功,AccountChange={}",accountChange.toString());
        return accountChange;
    }

    //生成订单 号
    public String getOrderNo(AccountChangeEnum changeEnum) {
        String orderNo = changeEnum.getCode();
        String today = DateUtil.today("yyyyMMddHHmmssSSS");
        String randNum = CommonUtil.random(3);
        orderNo = orderNo + today + randNum;
        return orderNo;
    }

}

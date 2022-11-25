package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.DepositSendActivity;
import com.qianyi.casinocore.service.DepositSendActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class DepositSendActivityBusiness {


    @Autowired
    private DepositSendActivityService depositSendActivityService;

    public DepositSendActivity save(String actName,Integer actType, BigDecimal depAmount, BigDecimal sendAmount, int amountTimes){
        DepositSendActivity depositSendActivity = new DepositSendActivity();
        depositSendActivity.setActivityName(actName);
        depositSendActivity.setActivityType(actType);
        depositSendActivity.setDepositAmount(depAmount);
        depositSendActivity.setSendAmount(sendAmount);
        depositSendActivity.setAmountTimes(amountTimes);
        depositSendActivity.setDel(false);
        depositSendActivity.setActivityStatus(1);
        return depositSendActivityService.save(depositSendActivity);
    }

    public DepositSendActivity updateActivity(Long id,String actName, Integer actType, BigDecimal depAmount, BigDecimal sendAmount, int amountTimes){
        DepositSendActivity depositSendActivity = depositSendActivityService.findById(id);
        depositSendActivity.setActivityName(actName);
        depositSendActivity.setActivityType(actType);
        depositSendActivity.setDepositAmount(depAmount);
        depositSendActivity.setSendAmount(sendAmount);
        depositSendActivity.setAmountTimes(amountTimes);
        return depositSendActivityService.save(depositSendActivity);
    }

}

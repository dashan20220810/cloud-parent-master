package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.DepositSendActivity;
import com.qianyi.casinocore.repository.DepositSendActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class DepositSendActivityService {

    @Autowired
    private DepositSendActivityRepository depositSendActivityRepository;

    public DepositSendActivity findById(Long id){
        return depositSendActivityRepository.findById(id).orElse(null);
    }

    public List<DepositSendActivity> findAllAct(String actName){
        if(StringUtils.hasLength(actName)){
            return depositSendActivityRepository.findAllByActivityNameAndDel(actName,false);
        }
        return depositSendActivityRepository.findAllByDelFalse();
    }

    public DepositSendActivity save(DepositSendActivity depositSendActivity){

        return depositSendActivityRepository.save(depositSendActivity);
    }

    public void deleteAct(Long id){
        DepositSendActivity depositSendActivity = depositSendActivityRepository.findById(id).orElse(null);
        if(depositSendActivity != null){
            depositSendActivity.setDel(true);
            depositSendActivityRepository.save(depositSendActivity);
        }else {
            log.error("活动删除错误，使用了不存在的ID {}",id);
        }
    }

    public void startActivity(Long actId){
        DepositSendActivity depositSendActivity = depositSendActivityRepository.findById(actId).orElse(null);
        if(depositSendActivity != null){
            depositSendActivity.setActivityStatus(1);
            depositSendActivityRepository.save(depositSendActivity);
        }else {
            log.error("活动删除错误，使用了不存在的ID {}",actId);
        }
    }

    public void stopActivity(Long actId){
        DepositSendActivity depositSendActivity = depositSendActivityRepository.findById(actId).orElse(null);
        if(depositSendActivity != null){
            depositSendActivity.setActivityStatus(0);
            depositSendActivityRepository.save(depositSendActivity);
        }else {
            log.error("活动删除错误，使用了不存在的ID {}",actId);
        }
    }
}

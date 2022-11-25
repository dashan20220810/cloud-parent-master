package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.repository.ConsumerErrorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ConsumerErrorService {

    @Autowired
    private ConsumerErrorRepository consumerErrorRepository;

    public ConsumerError save(ConsumerError consumerError){
        return consumerErrorRepository.save(consumerError);
    }

    public List<ConsumerError> findUsersByUserId(Long userId,String type){
        return consumerErrorRepository.findByMainIdAndConsumerTypeAndRepairStatus(userId,type,0);
    }

    public List<ConsumerError> findAllToRepair(String type){
        return consumerErrorRepository.findByConsumerTypeAndRepairStatus(type,0);
    }
}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.RebateConfiguration;
import com.qianyi.casinocore.model.RebateDetail;
import com.qianyi.casinocore.repository.RebateDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RebateDetailService {

    @Autowired
    private RebateDetailRepository rebateDetailRepository;

    public RebateDetail save(RebateDetail rebateDetail) {
        return rebateDetailRepository.save(rebateDetail);
    }

}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Visits;
import com.qianyi.casinocore.repository.VisitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VisitsService {

    @Autowired
    private VisitsRepository visitsRepository;

    public void save(Visits visits) {
        visitsRepository.save(visits);
    }

    public List<Map<String,Object>> findListSum(String domainName, String ip, String StartTime, String endTime) {
        return visitsRepository.findListGroupBy(StartTime, endTime, domainName, ip);
    }
}

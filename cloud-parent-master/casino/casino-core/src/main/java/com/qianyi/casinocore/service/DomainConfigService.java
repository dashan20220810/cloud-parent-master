package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.DomainConfig;
import com.qianyi.casinocore.repository.DomainConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DomainConfigService {

    @Autowired
    private DomainConfigRepository domainConfigRepository;

    public List<DomainConfig> findList() {
        return domainConfigRepository.findAll();
    }

    public DomainConfig findById(Long id) {
        Optional<DomainConfig> optional = domainConfigRepository.findById(id);
        if (optional != null && optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public void save(DomainConfig domainConfig) {
        domainConfigRepository.save(domainConfig);
    }

    public void deleteId(Long id) {
        domainConfigRepository.deleteById(id);
    }

    public DomainConfig findByDomainUrlAndDomainStatus(String origin, Integer domainStatus) {
        return domainConfigRepository.findByDomainUrlAndDomainStatus(origin,domainStatus);
    }
}

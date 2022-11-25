package com.qianyi.casinocore.service;

import com.qianyi.casinocore.CoreConstants;
import com.qianyi.casinocore.model.BetRatioConfig;
import com.qianyi.casinocore.model.SysConfig;
import com.qianyi.casinocore.repository.SysConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SysConfigService {
    @Autowired
    private SysConfigRepository sysConfigRepository;

    public SysConfig findBySysGroupAndName(Integer groupBet, String name) {
        return sysConfigRepository.findBySysGroupAndName(groupBet,name);
    }

    public List<SysConfig> findAll(){
        return sysConfigRepository.findAll();
    }

    public SysConfig save(SysConfig sysConfig){
        return sysConfigRepository.save(sysConfig);
    }

    public SysConfig findById(Long id) {
        Optional<SysConfig> optional = sysConfigRepository.findById(id);
        if (optional != null && optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public List<SysConfig> findByGroup(int groupBet) {
        return sysConfigRepository.findBySysGroup(groupBet);
    }

    public List<SysConfig> saveAll(List<SysConfig> sysConfigList) {
        return sysConfigRepository.saveAll(sysConfigList);
    }

    public SysConfig findByName(String name){
        return sysConfigRepository.findByName(name);
    }
}

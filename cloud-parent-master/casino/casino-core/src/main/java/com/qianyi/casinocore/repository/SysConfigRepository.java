package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SysConfigRepository extends JpaRepository<SysConfig,Long>, JpaSpecificationExecutor<SysConfig> {

    List<SysConfig> findBySysGroup(int group);

    SysConfig findBySysGroupAndName(Integer groupBet, String name);

    SysConfig findByName(String name);
}

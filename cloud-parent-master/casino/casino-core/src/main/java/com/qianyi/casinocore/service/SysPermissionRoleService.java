package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysPermissionRole;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.SysPermissionRoleRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysPermissionRoleService {

    @Autowired
    private SysPermissionRoleRepository sysPermissionRoleRepository;

    public List<SysPermissionRole> findByRoleId(Long id) {
        //        return sysPermissionRoleRepository.findByRoleIdOrderByNameDesc(id);
        return sysPermissionRoleRepository.findByRoleId(id);
    }

    public void saveAll(List<SysPermissionRole> sysPermissionRoleList) {
        sysPermissionRoleRepository.saveAll(sysPermissionRoleList);
    }

    public void delete(Long id) {
        sysPermissionRoleRepository.deleteByRoleId(id);
    }

    public void deleteAllIds(List<SysPermissionRole> byRoleId) {
        for (SysPermissionRole sysPermissionRole : byRoleId) {
            sysPermissionRoleRepository.delete(sysPermissionRole);
        }


    }
}

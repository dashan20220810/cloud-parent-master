package com.qianyi.casinocore.service;


import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.repository.SysUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SysUserRoleService {

    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    public SysUserRole findbySysUserId(Long userid) {

        return sysUserRoleRepository.findBySysUserId(userid);
    }

    public SysUserRole save(SysUserRole sysUserRole) {
        return sysUserRoleRepository.save(sysUserRole);
    }

    public void deleteById(Long roleId) {
        sysUserRoleRepository.deleteBySysRoleId(roleId);
    }

    public List<SysUserRole> findAllIds(List<Long> userIds) {
        return sysUserRoleRepository.findAll(getConditionFirstPid(userIds));
    }

    private Specification<SysUserRole> getConditionFirstPid(List<Long> userIds) {
        Specification<SysUserRole> specification = new Specification<SysUserRole>() {
            @Override
            public Predicate toPredicate(Root<SysUserRole> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("sysUserId");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public SysUserRole findByRoleUserId(Long id) {
        return sysUserRoleRepository.findBySysUserId(id);
    }
}

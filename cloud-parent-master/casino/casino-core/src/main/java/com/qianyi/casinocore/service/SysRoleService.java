package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.SysRole;
import com.qianyi.casinocore.model.SysUserRole;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.SysRoleRepository;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SysRoleService {

    @Autowired
    private SysRoleRepository sysRoleRepository;

    public SysRole findById(Long sysRoleId) {
        Optional<SysRole> optional = sysRoleRepository.findById(sysRoleId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public SysRole save(SysRole sysRole) {
        return sysRoleRepository.save(sysRole);
    }

    public List<SysRole> findAll() {
        return sysRoleRepository.findAll();
    }

    public void deleteById(Long roleId) {
        sysRoleRepository.deleteById(roleId);
    }

    public List<SysRole> findAllIds(List<Long> roleIds) {
        return sysRoleRepository.findAllById(roleIds);
    }



    /**
     * 查询条件拼接，灵活添加条件
     *
     * sysRole
     * @return
     */
    private Specification<SysRole> getCondition(SysRole sysRole) {
        Specification<SysRole> specification = new Specification<SysRole>() {
            @Override
            public Predicate toPredicate(Root<SysRole> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (sysRole.getRoleName() != null) {
                    list.add(cb.equal(root.get("roleName").as(String.class), sysRole.getRoleName()));
                }

                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<SysRole> findbyRoleName(SysRole sysRole) {
        List<SysRole> sysRoleList = sysRoleRepository.findAll(getCondition(sysRole));

        return sysRoleList;
    }
}

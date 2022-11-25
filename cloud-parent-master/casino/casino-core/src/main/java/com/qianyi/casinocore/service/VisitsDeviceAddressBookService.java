package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.VisitsDeviceAddressBook;
import com.qianyi.casinocore.repository.VisitsDeviceAddressBookRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class VisitsDeviceAddressBookService {

    @Autowired
    private VisitsDeviceAddressBookRepository visitsDeviceAddressBookRepository;

    public void saveAll(List<VisitsDeviceAddressBook> addressBook) {
        visitsDeviceAddressBookRepository.saveAll(addressBook);
    }

    public void deleteByVisitsDeviceId(Long visitsDeviceId) {
        visitsDeviceAddressBookRepository.deleteByVisitsDeviceId(visitsDeviceId);
    }

    public Page<VisitsDeviceAddressBook> findPage(Pageable pageable, VisitsDeviceAddressBook visitsDeviceAddressBook) {
        Specification<VisitsDeviceAddressBook> condition = this.getCondition(visitsDeviceAddressBook);
        return visitsDeviceAddressBookRepository.findAll(condition, pageable);
    }

    private Specification<VisitsDeviceAddressBook> getCondition(VisitsDeviceAddressBook visitsDeviceAddressBook) {
        Specification<VisitsDeviceAddressBook> specification = new Specification<VisitsDeviceAddressBook>() {
            List<Predicate> list = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<VisitsDeviceAddressBook> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                if (!CommonUtil.checkNull(visitsDeviceAddressBook.getName())) {
                    list.add(cb.equal(root.get("name").as(String.class), visitsDeviceAddressBook.getName()));
                }
                if(!CommonUtil.checkNull(visitsDeviceAddressBook.getPhone())){
                    list.add(cb.equal(root.get("phone").as(String.class), visitsDeviceAddressBook.getPhone()));
                }
                if (visitsDeviceAddressBook.getVisitsDeviceId() != null) {
                    list.add(cb.equal(root.get("visitsDeviceId").as(Long.class), visitsDeviceAddressBook.getVisitsDeviceId()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}

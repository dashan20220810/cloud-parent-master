package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.VisitsDevice;
import com.qianyi.casinocore.repository.VisitsDeviceRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VisitsDeviceService {

    @Autowired
    private VisitsDeviceRepository visitsDeviceRepository;

    public VisitsDevice save(VisitsDevice device) {
        return visitsDeviceRepository.save(device);
    }
    public void deleteById(Long id) {
        visitsDeviceRepository.deleteById(id);
    }

    public VisitsDevice findByManufacturerAndUdid(String manufacturer,String udid) {
        return visitsDeviceRepository.findByManufacturerAndUdid(manufacturer,udid);
    }

    public Page<VisitsDevice> findPage(Pageable pageable, VisitsDevice visitsDevice, Date startDate,Date endDate) {
        Specification<VisitsDevice> condition = this.getCondition(visitsDevice,startDate,endDate);
        return visitsDeviceRepository.findAll(condition, pageable);
    }

    private Specification<VisitsDevice> getCondition(VisitsDevice visitsDevice,Date startDate,Date endDate) {
        Specification<VisitsDevice> specification = new Specification<VisitsDevice>() {
            List<Predicate> list = new ArrayList<Predicate>();
            @Override
            public Predicate toPredicate(Root<VisitsDevice> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                if (!CommonUtil.checkNull(visitsDevice.getIp())) {
                    list.add(cb.equal(root.get("ip").as(String.class), visitsDevice.getIp()));
                }
                if(!CommonUtil.checkNull(visitsDevice.getModel())){
                    list.add(cb.equal(root.get("model").as(String.class), visitsDevice.getModel()));
                }
                if(!CommonUtil.checkNull(visitsDevice.getUdid())){
                    list.add(cb.equal(root.get("udid").as(String.class), visitsDevice.getUdid()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

}

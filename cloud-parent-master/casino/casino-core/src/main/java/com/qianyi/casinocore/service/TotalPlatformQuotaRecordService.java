package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.TotalPlatformQuotaRecord;
import com.qianyi.casinocore.repository.TotalPlatformQuotaRecordRepository;
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
public class TotalPlatformQuotaRecordService {

    @Autowired
    private TotalPlatformQuotaRecordRepository totalPlatformQuotaRecordRepository;

    public void save(TotalPlatformQuotaRecord totalPlatformQuotaRecord) {
        totalPlatformQuotaRecordRepository.save(totalPlatformQuotaRecord);
    }

    public Page<TotalPlatformQuotaRecord> findTotalPlatformQuotaRecordPage(Pageable pageable, Date startDate, Date endDate){
        Specification<TotalPlatformQuotaRecord> condition = getCondition(startDate,endDate);
        return totalPlatformQuotaRecordRepository.findAll(condition,pageable);
    }

    private Specification<TotalPlatformQuotaRecord> getCondition(Date startDate,Date endDate) {
        Specification<TotalPlatformQuotaRecord> specification = new Specification<TotalPlatformQuotaRecord>() {
            @Override
            public Predicate toPredicate(Root<TotalPlatformQuotaRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
}

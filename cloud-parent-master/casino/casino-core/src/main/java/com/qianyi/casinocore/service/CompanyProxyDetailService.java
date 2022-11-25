package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.LoginLog;
import com.qianyi.casinocore.repository.CompanyProxyDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CompanyProxyDetailService {

    @Autowired
    private CompanyProxyDetailRepository companyProxyDetailRepository;

    public CompanyProxyDetail save(CompanyProxyDetail companyProxyDetail){
        return companyProxyDetailRepository.save(companyProxyDetail);
    }

    public List<CompanyProxyDetail> saveAll(List<CompanyProxyDetail> companyProxyDetailList){
        return companyProxyDetailRepository.saveAll(companyProxyDetailList);
    }

    public void deleteByDayTime(String dayTime){
        companyProxyDetailRepository.deleteByStaticsTimes(dayTime);
    }

    public CompanyProxyDetail getCompanyProxyDetailByUidAndTime(Long uid,String staticsTime){
        return companyProxyDetailRepository.getCompanyProxyDetailByUserIdAndStaticsTimes(uid,staticsTime);
    }
    public List<CompanyProxyDetail> findCompanyProxyDetails(CompanyProxyDetail companyProxyDetail, Date startDate, Date endDate,Sort sort) {
        Specification<CompanyProxyDetail> condition = this.getCondition(companyProxyDetail,startDate,endDate);
        if (sort == null){
            return companyProxyDetailRepository.findAll(condition);
        }
        return companyProxyDetailRepository.findAll(condition,sort);
    }

    public List<CompanyProxyDetail> findCompanyProxyDetails(CompanyProxyDetail companyProxyDetail,String startTime, String endTime) {
        Specification<CompanyProxyDetail> condition = this.getCondition(companyProxyDetail,startTime,endTime);
        return companyProxyDetailRepository.findAll(condition);
    }

    private Specification<CompanyProxyDetail> getCondition(CompanyProxyDetail companyProxyDetail,String startTime, String endTime) {
        Specification<CompanyProxyDetail> specification = new Specification<CompanyProxyDetail>() {
            @Override
            public Predicate toPredicate(Root<CompanyProxyDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (companyProxyDetail.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), companyProxyDetail.getUserId()));
                }

                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("staticsTimes").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param companyProxyDetail
     * @return
     */
    private Specification<CompanyProxyDetail> getCondition(CompanyProxyDetail companyProxyDetail, Date startDate, Date endDate) {
        Specification<CompanyProxyDetail> specification = new Specification<CompanyProxyDetail>() {
            @Override
            public Predicate toPredicate(Root<CompanyProxyDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
//                if (!CommonUtil.checkNull(companyProxyDetail.getProfitRate())) {
//                    list.add(cb.equal(root.get("userName").as(String.class), proxyUser.getUserName()));
//                }
//                if (proxyUser.getProxyRole() != null) {
//                    list.add(cb.equal(root.get("proxyRole").as(Integer.class), proxyUser.getProxyRole()));
//                }
                if (companyProxyDetail.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), companyProxyDetail.getUserId()));
                }
                if (companyProxyDetail.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Integer.class), companyProxyDetail.getProxyRole()));
                }
                if (companyProxyDetail.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), companyProxyDetail.getFirstProxy()));
                }
                if (companyProxyDetail.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), companyProxyDetail.getSecondProxy()));
                }
                if (companyProxyDetail.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), companyProxyDetail.getThirdProxy()));
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

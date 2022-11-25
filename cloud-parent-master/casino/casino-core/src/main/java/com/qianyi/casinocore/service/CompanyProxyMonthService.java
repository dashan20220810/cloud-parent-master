package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CompanyProxyMonthService {

    @Autowired
    private CompanyProxyMonthRepository companyProxyMonthRepository;

    public void deleteAllMonth(String monthTime){
        companyProxyMonthRepository.deleteByStaticsTimes(monthTime);
    }

    public List<CompanyProxyMonth> findAllByStaticsTimesAndProxyRole(String staticsTimes, Integer proxyRole) {
        return companyProxyMonthRepository.findAllByStaticsTimesAndProxyRole(staticsTimes, proxyRole);
    }

    public List<CompanyProxyMonth> saveAll(List<CompanyProxyMonth> companyProxyMonthList){
        return companyProxyMonthRepository.saveAll(companyProxyMonthList);
    }

    public CompanyProxyMonth save(CompanyProxyMonth companyProxyMonth){
        return companyProxyMonthRepository.save(companyProxyMonth);
    }

    public List<CompanyProxyMonth> queryMonthByDay(String startDate,String endDate){
        List<Map<String,Object>> mapList = companyProxyMonthRepository.queryMonthData(startDate,endDate);
        /*String jsonString = JSON.toJSONString(mapList);
        return JSON.parseArray(jsonString,CompanyProxyMonth.class);*/
        return DTOUtil.map2DTO(mapList, CompanyProxyMonth.class);
    }
    public CompanyProxyMonth findById(Long id){
        Optional<CompanyProxyMonth> byId = companyProxyMonthRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }
    public List<CompanyProxyMonth> findAll(CompanyProxyMonth companyProxyMonth, Sort sort){
        Specification<CompanyProxyMonth> condition = this.getCondition(companyProxyMonth);
        if (sort == null){
            return companyProxyMonthRepository.findAll(condition);
        }
        return companyProxyMonthRepository.findAll(condition,sort);
    }

    public List<CompanyProxyMonth> findCompanyProxyMonths(CompanyProxyMonth companyProxyMonth,String startTime, String endTime) {
        Specification<CompanyProxyMonth> condition = this.getCondition(companyProxyMonth,startTime,endTime);
        return companyProxyMonthRepository.findAll(condition);
    }

    public List<CompanyProxyMonth> findCompanyProxyMonths(List<Long> proxyUserId,CompanyProxyMonth companyProxyMonth,String startTime, String endTime) {
        Specification<CompanyProxyMonth> condition = this.getCondition(proxyUserId,companyProxyMonth,startTime,endTime);
        return companyProxyMonthRepository.findAll(condition);
    }

    private Specification<CompanyProxyMonth> getCondition(CompanyProxyMonth companyProxyMonth,String startTime, String endTime) {
        Specification<CompanyProxyMonth> specification = new Specification<CompanyProxyMonth>() {
            @Override
            public Predicate toPredicate(Root<CompanyProxyMonth> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (companyProxyMonth.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), companyProxyMonth.getUserId()));
                }

                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(cb.between(root.get("staticsTimes").as(String.class), startTime, endTime));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    private Specification<CompanyProxyMonth> getCondition(List<Long> proxyUserId,CompanyProxyMonth companyProxyMonth,String startTime, String endTime) {
        Specification<CompanyProxyMonth> specification = new Specification<CompanyProxyMonth>() {
            @Override
            public Predicate toPredicate(Root<CompanyProxyMonth> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                CriteriaBuilder.In<Object> in = cb.in(root.get("userId"));
                for (Long id : proxyUserId) {
                    in.value(id);
                }
                list.add(cb.and(cb.and(in)));

                if (companyProxyMonth.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), companyProxyMonth.getUserId()));
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
     * @param companyProxyMonth
     * @return
     */
    private Specification<CompanyProxyMonth> getCondition(CompanyProxyMonth companyProxyMonth) {
        Specification<CompanyProxyMonth> specification = new Specification<CompanyProxyMonth>() {
            @Override
            public Predicate toPredicate(Root<CompanyProxyMonth> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (companyProxyMonth.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), companyProxyMonth.getUserId()));
                }
                if (companyProxyMonth.getProxyRole() != null) {
                    list.add(cb.equal(root.get("proxyRole").as(Integer.class), companyProxyMonth.getProxyRole()));
                }
                if (companyProxyMonth.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), companyProxyMonth.getFirstProxy()));
                }
                if (companyProxyMonth.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), companyProxyMonth.getSecondProxy()));
                }
                if (companyProxyMonth.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), companyProxyMonth.getThirdProxy()));
                }
                if (!CommonUtil.checkNull(companyProxyMonth.getStaticsTimes())) {
                    list.add(cb.equal(root.get("staticsTimes").as(String.class), companyProxyMonth.getStaticsTimes()));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }
}

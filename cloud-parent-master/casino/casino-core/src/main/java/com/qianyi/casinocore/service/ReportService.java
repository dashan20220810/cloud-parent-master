package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    CompanyProxyMonthRepository companyProxyMonthRepository;

    public List<Map<String,Object>> queryAllPersonReport(String startTime,String endTime,int page,int count){
        return companyProxyMonthRepository.queryAllPersonReport(startTime,endTime,page,count);
    }

    public int queryTotalElement(String startTime,String endTime){
        return companyProxyMonthRepository.queryAllTotalElement(startTime,endTime);
    }

    public int queryAllByFirst(Long first){
        return companyProxyMonthRepository.queryAllByFirst(first);
    }

    public int queryAllBySecond(Long second){
        return companyProxyMonthRepository.queryAllBySecond(second);
    }

    public int queryAllByThird(Long third){
        return companyProxyMonthRepository.queryAllByThird(third);
    }

    public List<Map<String,Object>> queryPersonReport(long user_id ,String startTime,String endTime){
        return companyProxyMonthRepository.queryPersonReport(user_id, startTime, endTime);
    }

    public Map<String,Object> queryAllTotal(String startTime,String endTime){
        return companyProxyMonthRepository.queryAllTotal(startTime, endTime);
    }

    public Map<String,Object> queryReportByThird(long proxyUserId ,String startTime,String endTime){
        return companyProxyMonthRepository.queryReportByThird(proxyUserId, startTime, endTime);
    }

    public Map<String,Object> queryReportByCompany(String startTime,String endTime){
        return companyProxyMonthRepository.queryReportByCompany(startTime, endTime);
    }

    public Map<String,Object> queryReportBySecond(long proxyUserId ,String startTime,String endTime){
        return companyProxyMonthRepository.queryReportBySecond(proxyUserId, startTime, endTime);
    }

    public Map<String,Object> queryReportByFirst(long proxyUserId ,String startTime,String endTime){
        return companyProxyMonthRepository.queryReportByFirst(proxyUserId, startTime, endTime);
    }

    public Map<String,Object> queryReportAll(String startTime,String endTime){
        return companyProxyMonthRepository.queryReportAll(startTime, endTime);
    }
}

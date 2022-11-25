package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyGameRecordReport;
import com.qianyi.casinocore.repository.ProxyGameRecordReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ProxyGameRecordReportService {

    @Autowired
    private ProxyGameRecordReportRepository proxyGameRecordReportRepository;

    public List<ProxyGameRecordReport> findAll(){
        return proxyGameRecordReportRepository.findAll();
    }

    @Transactional
    public void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,Long firstProxy,Long secondProxy,Long thirdProxy,BigDecimal betAmount){
        proxyGameRecordReportRepository.updateKey(gameRecordReportId,userId,orderTimes,validAmount,winLoss,firstProxy,secondProxy,thirdProxy,betAmount);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossByFirst(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long secondProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossBySecond(startTime,endTime,secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long thirdProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossByThird(startTime,endTime,thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossByFirst(firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy){
        return proxyGameRecordReportRepository.findSumBetAndWinLossBySecond(secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy) {
        return proxyGameRecordReportRepository.findSumBetAndWinLossByThird(thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime){
        return proxyGameRecordReportRepository.findSumBetAndWinLoss(startTime,endTime);
    }

    public Map<String, Object> findSumBetAndWinLoss(){
        return proxyGameRecordReportRepository.findSumBetAndWinLoss();
    }

    public List<Map<String, Object>> findBetAndWinLoss(String startTime,String endTime){
        return proxyGameRecordReportRepository.findBetAndWinLoss(startTime,endTime);
    }
}

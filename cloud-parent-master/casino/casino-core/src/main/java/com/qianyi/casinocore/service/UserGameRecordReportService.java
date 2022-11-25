package com.qianyi.casinocore.service;

import com.qianyi.casinocore.repository.UserGameRecordReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class UserGameRecordReportService {

    @Autowired
    private UserGameRecordReportRepository userGameRecordReportRepository;

    @Transactional
    public void updateKey(Long gameRecordReportId,Long userId,String orderTimes, BigDecimal validAmount,BigDecimal winLoss,BigDecimal betAmount,String platform){
        userGameRecordReportRepository.updateKey(gameRecordReportId,userId,orderTimes,validAmount,winLoss,betAmount,platform);
    }

    public List<Map<String, Object>> sumUserRunningWater(String startTime,String endTime){
        return userGameRecordReportRepository.sumUserRunningWater(startTime,endTime);
    }

    public BigDecimal sumUserRunningWaterByUserId(String startTime,String endTime,Long userId){
        return userGameRecordReportRepository.sumUserRunningWaterByUserId(startTime,endTime,userId);
    }
}

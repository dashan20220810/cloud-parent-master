package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.vo.HistoryTotal;
import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ReportControllerTest {

    @Autowired
    CompanyProxyMonthRepository companyProxyMonthRepository;

    @Test
    public void should_query_data(){
        List<Map<String,Object>> result = companyProxyMonthRepository.queryAllPersonReport("2021-11-01 00:00:00","2021-11-30 00:00:00",10,10);
        result.forEach(item ->{
            log.info("{}",item);
        });
    }

    @Test
    public void should_query_total_element_data(){
        int result = companyProxyMonthRepository.queryAllTotalElement("2021-11-01 00:00:00","2021-11-30 00:00:00");
        log.info("total : {}",result);
    }

    @Test
    public void should_query_single_person_data(){
        List<Map<String,Object>> result = companyProxyMonthRepository.queryPersonReport(3,"2021-11-01 00:00:00","2021-11-30 00:00:00");
        result.forEach(item ->{
            log.info("{}",item);
        });
    }

    @Test
    public void should_query_all_total_data(){
        Map<String,Object> result = companyProxyMonthRepository.queryAllTotal("2021-12-19 00:00:00","2021-12-21 23:59:59");
        result.keySet().forEach(item ->{
            log.info("{}:{}",item,result.get(item).toString());
        });

        HistoryTotal historyTotal = new HistoryTotal();
        historyTotal.setAll_profit_amount(new BigDecimal(result.get("all_profit_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setService_charge(new BigDecimal(result.get("service_charge").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setTotal_amount(new BigDecimal(result.get("total_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setNum(Integer.parseInt(result.get("num").toString()));
        historyTotal.setBet_amount(new BigDecimal(result.get("bet_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWin_loss(new BigDecimal(result.get("win_loss").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setValidbet(new BigDecimal(result.get("validbet").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWash_amount(new BigDecimal(result.get("wash_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setAvg_benefit(new BigDecimal(result.get("avg_benefit").toString()).setScale(2, RoundingMode.HALF_UP));

        log.info("{}",historyTotal);
    }
}
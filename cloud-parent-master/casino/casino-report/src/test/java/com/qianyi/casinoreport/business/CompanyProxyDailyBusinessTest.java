package com.qianyi.casinoreport.business;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.repository.GameRecordRepository;
import com.qianyi.casinocore.service.CompanyProxyMonthService;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.casinoreport.business.company.CompanyProxyDailyBusiness;
import com.qianyi.casinoreport.business.company.CompanyProxyMonthBusiness;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class CompanyProxyDailyBusinessTest {

    @Autowired
    private CompanyProxyDailyBusiness companyProxyDailyBusiness;

    @Autowired
    private CompanyProxyMonthBusiness companyProxyMonthBusiness;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void report_correct(){
        companyProxyDailyBusiness.processDailyReport("2021-11-06");
    }

    @Test
    public void report_month_correct(){
        companyProxyMonthBusiness.processMonthReport("2021-12-01");
    }

    @Test
    public void query_sql(){
        List<Map<String,Object>> gameRecords = gameRecordRepository.getStatisticsResult("2021-11-03 00:00:00","2021-11-03 23:59:59");
        String json = JSON.toJSONString(gameRecords);


        log.info("{}", JSON.parseArray(json,CompanyOrderAmountVo.class));
    }

    @Test
    public void query_static_sql(){
        String sql = "select max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,third_proxy third_proxy,count(distinct user_id) player_num ,max(bet_time) bet_time, sum(validbet) validbet \n" +
                "from game_record gr\n" +
                "where\n" +
                "bet_time between ?1 and ?2\n" +
                "and third_proxy is not null \n" +
                "group by third_proxy ";
        List<Tuple> gameRecords = (List<Tuple>) entityManager.createNativeQuery(sql,CompanyOrderAmountVo.class);

        log.info("{}",gameRecords);
    }
}
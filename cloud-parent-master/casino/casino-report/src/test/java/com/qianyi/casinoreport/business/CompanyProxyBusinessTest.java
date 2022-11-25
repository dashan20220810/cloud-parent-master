package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.util.BTimeUtil;
import com.qianyi.casinoreport.business.company.CompanyLevelProcessBusiness;
import com.qianyi.casinoreport.business.company.CompanyProxyDailyBusiness;
import com.qianyi.casinoreport.business.company.CompanyProxyMonthBusiness;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CompanyProxyBusinessTest {

    @Test
    public void should_get_correct_int_val(){
        CompanyLevelProcessBusiness companyProxyBusiness = new CompanyLevelProcessBusiness();
        Map<Integer,Integer> valList =   new TreeMap<Integer, Integer>();
        valList.put(10,100);
        valList.put(20,200);
        valList.put(30,300);
        Map<Integer,BigDecimal> bigDecimalMap = new HashMap<>();
        bigDecimalMap.put(1,BigDecimal.valueOf(1));
        bigDecimalMap.put(5,BigDecimal.valueOf(2));
        bigDecimalMap.put(10,BigDecimal.valueOf(3));
        bigDecimalMap.put(20,BigDecimal.valueOf(4));
        bigDecimalMap.put(30,BigDecimal.valueOf(5));
        CompanyLevelBO rs = companyProxyBusiness.getProfitLevel(BigDecimal.valueOf(210293),valList,bigDecimalMap);
        System.out.println(rs);
    }

    @Test
    public void should_parseDateTime(){
        String time = "2021-11-03 20:37:08".replace(' ','T');
        LocalDateTime localDateTime = LocalDateTime.parse(time);
        log.info("{}",localDateTime);
    }

    @Test
    public void should_get_correct_start_time() {
        CompanyProxyDailyBusiness companyProxyBusiness = new CompanyProxyDailyBusiness();
        String dayTime = "2021-10-21";
        String startTime = companyProxyBusiness.getStartTime("2021-10-21");
        System.out.println(dayTime+":"+startTime);
    }

    @Test
    public void should_get_correct_end_time() {
        CompanyProxyDailyBusiness companyProxyBusiness = new CompanyProxyDailyBusiness();
        String dayTime = "2021-10-21";
        String endTime = companyProxyBusiness.getEndTime("2021-10-21");
        System.out.println(dayTime+":"+endTime);
    }

    @Test
    public void grouping_by_convert_result_test(){
//        List<BlogPost>
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = df.format(LocalDateTime.now());
        System.out.println(dateTime);
        System.out.println(dateTime.substring(0,10));
    }

    @Test
    public void should_get_month_correct_local_time() {
        String dayTime = "2021-10-28";
        String startTime = BTimeUtil.getMonthTime(dayTime);
        System.out.println(dayTime+":"+startTime);
    }

    @Test
    public void should_get_month_correct_start_time() {
        String dayTime = "2021-10-28";
        String startTime = BTimeUtil.getStartTime(dayTime);
        System.out.println(dayTime+":"+startTime);
    }

    @Test
    public void should_get_month_correct_end_time() {
        String dayTime = "2021-10-28";
        String endTime = BTimeUtil.getEndTime(dayTime);
        System.out.println(dayTime+":"+endTime);
    }

    @Test
    public void should_compare(){
        User user = new User();
        user.setIsFirstBet(0);
        boolean result = (user.getIsFirstBet()!=null && user.getIsFirstBet() == Constants.yes);
        System.out.println(result);
    }
}
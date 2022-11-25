package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.CompanyProxyMonthRepository;
import com.qianyi.casinocore.repository.GameRecordGoldenFRepository;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PictureControllerTest {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private GameRecordGoldenFRepository gameRecordGoldenFRepository;

    @Autowired
    CompanyProxyMonthRepository companyProxyMonthRepository;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;
    
    @Autowired
    private ReportService reportService;

    @Test
    public void enddIndex(){
        GameRecordEndIndex uGameRecordEndIndexUseLock = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        System.out.println(uGameRecordEndIndexUseLock);
    }
    
    @Test
    public void queryTotal(){
        Map<String, Object> stringObjectMap = reportService.queryAllTotal("2022-02-18 00:00:00", "2022-02-18 23:59:59");
        System.out.println(stringObjectMap);
    }

    @Test
    public void findMap(){
//        List<Map<String, Object>> map = userService.findMap("WM", "2021-11-01 00:00:00", "2022-11-01 00:00:00", 60844L);
//        System.out.println(map.size());
//        Map<String, Object> wm = userService.findMap("WM", "2022-02-18 00:00:00", "2022-02-18 23:59:59");
//        System.out.println(wm);
    }

    @Test
    public void findSumbetAmount(){
        List<Map<String, Object>> gameRecords =
            gameRecordGoldenFService.findSumBetAmount("2022-02-01 23:00:00", "2022-02-12 23:59:59");
        BigDecimal sumBetAmount =
            gameRecordGoldenFService.findSumBetAmount(60736L, "2022-02-18 00:00:00", "2022-02-18 23:59:59");
        System.out.println(gameRecords.size());
    }
    @Test
    public void findSumAmount(){
        List<Map<String, Object>> sumAmount =
            shareProfitChangeService.findSumAmount("2021-02-01 23:00:00", "2022-02-12 23:59:59");
        System.out.println(sumAmount);
    }

    @Test
    public void findGameRecords(){
//        List<Map<String, Object>> gameRecords =
//            gameRecordService.findGameRecords("2022-02-12 00:00:00", "2022-02-12 23:59:59");
//        for(Map<String, Object> map : gameRecords){
//            String s = map.get("userId").toString();
//            System.out.println(s);
//            Long userId = Long.valueOf(s);
//            System.out.println(userId);
//        }
//        System.out.println(gameRecords.size());
//        BigDecimal gameRecords =
//            gameRecordService.findGameRecords(6085L,"2022-02-12 00:00:00", "2022-02-12 23:59:59");
//        System.out.println(gameRecords);
//        Map<String, Object> sumBetAndWinLoss =
//            gameRecordService.findSumBetAndWinLoss("2022-02-12 23:00:00", "2022-02-12 23:59:59");
//        Set<Long> groupByUser = gameRecordService.findGroupByUser("2022-02-12 23:00:00", "2022-02-12 23:59:59");
//        System.out.println(sumBetAndWinLoss);
//        System.out.println(groupByUser);
        Set<Long> groupBySecond =
            gameRecordService.findGroupBySecond("2022-02-12 23:00:00", "2022-02-12 23:59:59", 193L);

        Map<String, Object> sumBetAndWinLossBySecond =
            gameRecordService.findSumBetAndWinLossBySecond("2022-02-12 23:00:00", "2022-02-12 23:59:59", 193L);
        System.out.println(groupBySecond);
        System.out.println(sumBetAndWinLossBySecond);
    }
//    @Test
//    public void getMap() throws Exception {
//        List<Map<String,Object>> map = userService.findMap("2021-11-01 00:00:00", "2022-11-01 00:00:00", 1, 10,"ORDER BY bet_amount DESC");
//        System.out.println(map.size());
//    }
    @Test
    public void queryAllPersonReport(){
//        Sort.Order order = new Sort.Order(Sort.Direction.DESC,"proxy.bet_amount");
//        List<Sort.Order> orders = new ArrayList<>();
//        orders.add(order);
//        Sort sort  = Sort.by(order);
        List<Map<String, Object>> maps = companyProxyMonthRepository.queryAllPersonReport("2021-11-01 00:00:00", "2022-11-01 00:00:00", 1, 10);
        System.out.println(maps.size());
    }

    @Test
    public void findSumBetAmount(){
        BigDecimal sumBetAmount = gameRecordGoldenFRepository.findSumBetAmount(3L, "2022-02-10 22:06:59", "2022-02-10 22:07:00");
        System.out.println(sumBetAmount);
    }
    @Test
    public void findproxy(){
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        System.out.println(proxyUserList.size());
    }

    @Test
    public void find() throws ParseException {
        User user = new User();
        String startTime = "2021-11-24 00:00:00";
        String endTime = "2021-11-24 23:59:59";
        Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
        Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
        List<User> userList = userService.findUserList(user, startDate, endDate);
        System.out.println(userList);
        Long userCount = userService.findUserCount(user, startDate, endDate);
        Integer count = Math.toIntExact(userCount);
        System.out.println(count);

    }

    @Test
    public void should_cache_all_pic(){
        pictureService.findAll();
    }

    @Test
    public void should_cache_conidtion_pic(){
        List<LunboPic> lunboPicList = pictureService.findByTheShowEnd(2);
        System.out.println(lunboPicList.size());
    }

    @Test
    public void should_create_pic(){
        LunboPic lunboPic = new LunboPic();
        lunboPic.setNo(123);
        lunboPic.setRemark("123");
        lunboPic.setUrl("//123//123");
        pictureService.save(lunboPic);
    }

}
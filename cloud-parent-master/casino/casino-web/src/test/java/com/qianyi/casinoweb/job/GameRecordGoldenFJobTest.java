package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecordGoldenfEndTime;
import com.qianyi.casinocore.service.GameRecordGoldenfEndTimeService;
import com.qianyi.casinoweb.util.DateUtil;
import com.qianyi.casinoweb.vo.GameRecordObj;
import com.qianyi.casinoweb.vo.GoldenFTimeVO;
import com.qianyi.livegoldenf.api.PublicGoldenFApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GameRecordGoldenFJobTest {

    @Autowired
    PublicGoldenFApi publicGoldenFApi;

    @Autowired
    GameRecordGoldenFJob gameRecordGoldenFJob;

    @Autowired
    GameRecordGoldenfEndTimeService gameRecordGoldenfEndTimeService;


    //{secret_key=16e4ef534cec559430e07e05eb71c719, start_time=1645104330000, vendor_code=PG, operator_token=7970f61d512b7b681aa149fad927eee8, end_time=1645104630000, page=24179, page_size=1000}
    //1644588300000l, 1644588600000l
    @Test
    public void should_request_back() {
        Long startTime = 0l;
        Long endTime = 0l;
        PublicGoldenFApi.ResponseEntity responseEntity = publicGoldenFApi.getPlayerGameRecord(1645179900000l, 1645180200000l, "PG", 1, 1000);

        GameRecordObj gameRecordObj =  JSON.parseObject(responseEntity.getData(), GameRecordObj.class);

        log.info("{}", responseEntity);

        log.info("{}", gameRecordObj);
    }

    @Test
    public void should_request_error(){
        gameRecordGoldenFJob.pullGoldenF();
    }

    @Test
    public void should_check_time(){
        Long time = DateUtil.next5MinuteTime();
        log.info("{}",time);
    }

    @Test
    public void should_request_data(){
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime = gameRecordGoldenfEndTimeService.findFirstByVendorCodeOrderByEndTimeDesc("PG");
        log.info("{}",gameRecordGoldenfEndTime);
        if(gameRecordGoldenfEndTime == null){
            GameRecordGoldenfEndTime goldenfEndTime = new GameRecordGoldenfEndTime();
            goldenfEndTime.setEndTime(DateUtil.next5MinuteTime());
            goldenfEndTime.setStartTime(0l);
            goldenfEndTime.setVendorCode("PG");
//            gameRecordGoldenfEndTimeService.save(goldenfEndTime);
        }

    }

    @Test
    public void should_time_vos(){
        List<GoldenFTimeVO> timeVOS = gameRecordGoldenFJob.getTimes("PG");

        timeVOS.forEach(item ->{
            log.info("{}",item);
        });
    }



    @Test
    public void should_validate_time(){
        GameRecordGoldenfEndTime gameRecordGoldenfEndTime = gameRecordGoldenfEndTimeService.findFirstByVendorCodeOrderByEndTimeDesc("PG");
        log.info("{}",gameRecordGoldenfEndTime);

        Long startTime = gameRecordGoldenfEndTime.getEndTime()*1000;
        Long endTime = System.currentTimeMillis();
        log.info("{},{}",startTime,endTime);
        Long range = endTime-startTime;
        log.info("{}",range);
        Long num = range/(5*60*1000);
        log.info("num is {}",num);
        List<GoldenFTimeVO> timeVOS = new ArrayList<>();
        num=num+1;
        for(int i=0;i<=num;i++){
            GoldenFTimeVO goldenFTimeVO = new GoldenFTimeVO();
            Long tempEndTime = startTime+(5*60*1000);
            goldenFTimeVO.setStartTime(startTime);
            goldenFTimeVO.setEndTime(tempEndTime);
            timeVOS.add(goldenFTimeVO);
            startTime = tempEndTime;
        }
        log.info("{}",timeVOS);
        Long lastTime = 1645090230000l;

    }


}
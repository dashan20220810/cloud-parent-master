package com.qianyi.consumer;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.service.UserMoneyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BasicDataInsert {
    @Autowired
    PlatformConfigService platformConfigService;

    @Autowired
    GameRecordService gameRecordService;

    @Autowired
    UserMoneyService userMoneyService;

    @Test
    public void should_insert_into_basic_data(){
        PlatformConfig platformConfig = new PlatformConfig();
        platformConfig.setBetRate(BigDecimal.valueOf(1.2));
        platformConfig.setFirstCommission(BigDecimal.valueOf(0.0014));
        platformConfig.setSecondCommission(BigDecimal.valueOf(0.0002));
        platformConfig.setThirdCommission(BigDecimal.valueOf(0.00005));
        platformConfigService.save(platformConfig);
    }

    @Test
    public void should_insert_into_user_money_data(){
        UserMoney userMoney1 = new UserMoney();
        userMoney1.setUserId(1l);
        userMoneyService.save(userMoney1);

        UserMoney userMoney2 = new UserMoney();
        userMoney2.setUserId(2l);
        userMoneyService.save(userMoney2);

        UserMoney userMoney3 = new UserMoney();
        userMoney3.setUserId(3l);
        userMoneyService.save(userMoney3);

        UserMoney userMoney4 = new UserMoney();
        userMoney4.setUserId(4l);
        userMoneyService.save(userMoney4);
    }

    @Test
    public void should_insert_into_game_record(){
        GameRecord gameRecord = new GameRecord();
        gameRecord.setUserId(4l);
        gameRecord.setValidbet("600");
        gameRecord.setCreateTime(new Date());
        gameRecord.setBetId("13457l");
        gameRecordService.save(gameRecord);
    }
}

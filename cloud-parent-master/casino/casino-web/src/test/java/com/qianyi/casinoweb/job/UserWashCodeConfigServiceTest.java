package com.qianyi.casinoweb.job;


import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.UserWashCodeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserWashCodeConfigServiceTest {

    @Autowired
    UserWashCodeConfigService userWashCodeConfigService;
//GameRecord(userId=60850, user=null, betId=1494565898596917248, betTime=2022-02-18 14:54:12, beforeCash=null,
// bet=null, validbet=3, water=null, result=null, betCode=null, betResult=null, waterbet=null, winLoss=null, ip=null,
// gid=null, eventAndRound=null, eventChildAndSubround=null, tableId=null, gameResult=null, gname=假面嘉年华, commission=null,
// reset=null, settime=null, slotGameId=null, washCodeStatus=null, codeNumStatus=null,
// shareProfitStatus=0, firstProxy=null, secondProxy=null, thirdProxy=null, gameCode=mask-carnival)
    @Test
    public void should_check_config_data(){
        WashCodeConfig config = userWashCodeConfigService.getWashCodeConfigByUserIdAndGameId("PG",60850l,"PG");
        log.info("{}",config);
    }
}

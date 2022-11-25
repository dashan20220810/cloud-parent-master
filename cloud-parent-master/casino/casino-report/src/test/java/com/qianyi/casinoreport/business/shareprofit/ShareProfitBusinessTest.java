package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.GameRecordRepository;
import com.qianyi.casinocore.service.ConsumerErrorService;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ShareProfitBusinessTest {

    @Autowired
    ShareProfitBusiness shareProfitBusiness;

    @Autowired
    GameRecordRepository gameRecordRepository;

    @Autowired
    ConsumerErrorService consumerErrorService;

    @Test
    public void should_correct_process_correct(){

        List<ConsumerError> consumerErrors = consumerErrorService.findAllToRepair("sharePoint");

        consumerErrors.forEach(item -> {
            Optional<GameRecord> gameRecord =gameRecordRepository.findById(item.getMainId());

            GameRecord gameRecord1 = gameRecord.get();

            int sucess = shareProfitBusiness.procerssShareProfit(getShareMqVo(gameRecord1));
            if(sucess==0){
                item.setRepairStatus(1);
                consumerErrorService.save(item);
            }
        });



    }

    private ShareProfitMqVo getShareMqVo(GameRecord gameRecord){
        ShareProfitMqVo shareProfitMqVo = new ShareProfitMqVo();
        shareProfitMqVo.setUserId(gameRecord.getUserId());
        shareProfitMqVo.setBetTime(gameRecord.getBetTime());
        shareProfitMqVo.setValidbet(new BigDecimal(gameRecord.getValidbet()));
        shareProfitMqVo.setGameRecordId(gameRecord.getId());
        return shareProfitMqVo;
    }
}
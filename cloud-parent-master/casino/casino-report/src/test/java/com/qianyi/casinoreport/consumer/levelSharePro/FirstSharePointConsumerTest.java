package com.qianyi.casinoreport.consumer.levelSharePro;

import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class FirstSharePointConsumerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void should_send_message_to_mq() {
        com.qianyi.casinocore.vo.ShareProfitBO ShareProfitBO = new ShareProfitBO();
        ShareProfitBO.setFromUserId(606533l);
        ShareProfitBO.setUserId(1693l);
        ShareProfitBO.setBetAmount(BigDecimal.valueOf(10));
        ShareProfitBO.setProfitAmount(BigDecimal.valueOf(0.300000));
        ShareProfitBO.setBetTime("2021-11-30 13:24:59");
        ShareProfitBO.setDirect(true);
        ShareProfitBO.setCommission(BigDecimal.valueOf(30));
        ShareProfitBO.setRecordId(88389l);
        ShareProfitBO.setUserId(606533l);
        ShareProfitBO.setRecordBetId("1637762011322");
        rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_SHAREPROFIT_DIRECTQUEUE_DIRECTEXCHANGE, RabbitMqConstants.ONE_SHAREPROFIT_DIRECT, ShareProfitBO, new CorrelationData(UUID.randomUUID().toString()));
        log.info("success");
    }

}
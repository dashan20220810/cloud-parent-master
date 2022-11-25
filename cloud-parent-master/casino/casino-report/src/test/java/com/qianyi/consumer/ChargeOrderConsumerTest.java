package com.qianyi.consumer;

import com.qianyi.casinocore.vo.RechargeRecordVo;
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
import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ChargeOrderConsumerTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void should_send_message_to_mq(){
        RechargeRecordVo rechargeRecordVo = new RechargeRecordVo();
        rechargeRecordVo.setChargeAmount(BigDecimal.ONE);
        rechargeRecordVo.setUserId(10240l);
        rechargeRecordVo.setFirstUserId(1467l);
        rechargeRecordVo.setSecondUserId(1449l);
        rechargeRecordVo.setThirdUserId(1448l);
        rechargeRecordVo.setIsFirst(0);
        rechargeRecordVo.setCreateTime(new Date());
        rabbitTemplate.convertAndSend(RabbitMqConstants.CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,RabbitMqConstants.INGCHARGEORDER_DIRECT,rechargeRecordVo,new CorrelationData(UUID.randomUUID().toString()));

        log.info("success");

    }
}

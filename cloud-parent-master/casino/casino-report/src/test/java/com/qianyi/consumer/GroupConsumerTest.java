package com.qianyi.consumer;

import com.qianyi.casinocore.model.User;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GroupConsumerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void should_send_message_to_mq(){
        User user = new User();
        user.setId(10240l);
        user.setFirstPid(1467l);
        user.setSecondPid(1449l);
        user.setThirdPid(1448l);
        user.setCreateTime(new Date());
        rabbitTemplate.convertAndSend(RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE,RabbitMqConstants.ADDUSERTOTEAM_DIRECT,user,new CorrelationData(UUID.randomUUID().toString()));

        log.info("success");

    }

}

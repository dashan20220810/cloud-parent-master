package com.qianyi.casinoreport.consumer;

import com.qianyi.casinoreport.business.usergroup.LevelUserGroupNumBusiness;
import com.qianyi.casinoreport.business.usergroup.UserGroupNumBusiness;
import com.qianyi.casinocore.model.User;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.ADDUSERTOTEAM_DIRECTQUEUE)
@Component
public class GroupConsumer {

    @Autowired
    private UserGroupNumBusiness userGroupNumBusiness;
    @Autowired
    private LevelUserGroupNumBusiness levelUserGroupNumBusiness;


/*    @RabbitHandler
    public void process(User user, Channel channel, Message message) throws IOException {
        log.info("消费者接受到的消息是：{}",user);
        userGroupNumBusiness.processUser(user);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        log.info("消费者处理完当前消息：{}",user);
    }*/

    @RabbitHandler
    public void process(User user, Channel channel, Message message) throws IOException {
        log.info("消费者接受到的消息是：{}",user);
        levelUserGroupNumBusiness.processUser(user);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        log.info("消费者处理完当前消息：{}",user);
    }
}

package com.qianyi.casinoreport.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

//@Slf4j
//@RabbitListener(queues = "TestDirectQueue")
//@Component
//public class DirectConsumer {
//
//    @RabbitHandler
//    public void process(Map map, Channel channel, Message message) throws IOException {
//        log.info("消费者接受到的消息是：{}",map.toString());
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//    }
//}

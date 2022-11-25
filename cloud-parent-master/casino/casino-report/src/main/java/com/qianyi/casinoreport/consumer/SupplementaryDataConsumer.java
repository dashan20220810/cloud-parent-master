package com.qianyi.casinoreport.consumer;

import com.qianyi.casinoreport.business.company.CompanyProxyMonthBusiness;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.SUPPLEMENTARY_DATA_DIRECTQUEUE)
@Component
public class SupplementaryDataConsumer {

    @Autowired
    private CompanyProxyMonthBusiness companyProxyMonthBusiness;


    @RabbitHandler
    public void process(Map<String,Object> map, Channel channel, Message message) throws IOException {
        String dayTime = map.get("dayTime").toString();
        log.info("推广贷补充数据消费者接受到消息：{}",dayTime);
        try {
            companyProxyMonthBusiness.processMonthReport(dayTime);
        } catch (Exception e) {
            log.error("推广贷补充数据消息队列执行异常:"+ e);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        log.info("推广贷补充数据消费者处理完当前消息：{}",dayTime);
    }
}

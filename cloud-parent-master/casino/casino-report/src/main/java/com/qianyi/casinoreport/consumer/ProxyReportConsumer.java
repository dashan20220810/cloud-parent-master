package com.qianyi.casinoreport.consumer;

import com.qianyi.casinocore.business.ProxyGameRecordReportBusiness;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = RabbitMqConstants.PROXYG_AMERECORD_REPORT_QUEUE)
@Component
public class ProxyReportConsumer {

    @Autowired
    private ProxyGameRecordReportBusiness proxyGameRecordReportBusiness;

    @RabbitHandler
    public void process(ProxyGameRecordReportVo proxyGameRecordReportVo, Channel channel, Message message) throws IOException {
        log.info("统计代理报表:{},消费者接受到的消息是：{}",proxyGameRecordReportVo.getOrderId(),proxyGameRecordReportVo);
        proxyGameRecordReportBusiness.saveOrUpdate(proxyGameRecordReportVo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        log.info("统计代理报表:{},消费者处理完当前消息：{}",proxyGameRecordReportVo.getOrderId(),proxyGameRecordReportVo);
    }
}

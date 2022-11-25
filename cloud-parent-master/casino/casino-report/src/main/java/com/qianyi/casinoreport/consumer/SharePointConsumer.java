package com.qianyi.casinoreport.consumer;

import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinoreport.business.shareprofit.LevelShareProfitBusiness;
import com.qianyi.casinoreport.business.shareprofit.ShareProfitBusiness;
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
@RabbitListener(queues = RabbitMqConstants.SHAREPROFIT_DIRECTQUEUE)
@Component
public class SharePointConsumer {

    @Autowired
    private ShareProfitBusiness shareProfitBusiness;

    @Autowired
    private LevelShareProfitBusiness levelShareProfitBusiness;

/*    @RabbitHandler
    public void process(ShareProfitMqVo shareProfitMqVo, Channel channel, Message message) throws IOException {
        log.info("消费者接受到的消息是：{}",shareProfitMqVo);
        shareProfitBusiness.procerssShareProfit(shareProfitMqVo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        log.info("消费者处理完当前消息：{}",shareProfitMqVo);
    }*/


    /**
     * 拆分各级代理分别发送分润消息
     * @param shareProfitMqVo
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitHandler
    public void process(ShareProfitMqVo shareProfitMqVo, Channel channel, Message message) throws IOException {
        log.info("游戏id:{},消费者接受到的消息是：{}",shareProfitMqVo.getGameRecordId(),shareProfitMqVo);
        try {
            levelShareProfitBusiness.procerssShareProfit(shareProfitMqVo);
        } catch (Exception e) {
            log.error("代理线分润消息队列执行异常:"+ e);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
        log.info("游戏id:{},消费者处理完当前消息：{}",shareProfitMqVo.getGameRecordId(),shareProfitMqVo);
    }
}

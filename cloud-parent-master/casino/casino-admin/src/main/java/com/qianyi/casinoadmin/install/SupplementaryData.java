package com.qianyi.casinoadmin.install;

import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@Order(value = 7)
public class SupplementaryData implements CommandLineRunner {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化推广贷佣金开始============================================》");
        this.supplementaryData();
        log.info("初始化推广贷佣金结束============================================》");
    }

    //补充推广贷佣金company_proxy_month
    public void supplementaryData(){
        boolean hasKey = redisUtil.hasKey(Constants.REDIS_SUPPLEMENTARYDATA);
        if (!hasKey){
            //发消息
            Map<String,Object> map= new HashMap<>();
            map.put("dayTime","2022-02-01");
            rabbitTemplate.convertAndSend(RabbitMqConstants.SUPPLEMENTARY_DATA_DIRECTEXCHANGE, RabbitMqConstants.SUPPLEMENTARY_DATA_DIRECT, map, new CorrelationData(UUID.randomUUID().toString()));

            Map<String,Object> map1= new HashMap<>();
            map1.put("dayTime","2022-03-01");
            rabbitTemplate.convertAndSend(RabbitMqConstants.SUPPLEMENTARY_DATA_DIRECTEXCHANGE, RabbitMqConstants.SUPPLEMENTARY_DATA_DIRECT, map1, new CorrelationData(UUID.randomUUID().toString()));
            redisUtil.lSet(Constants.REDIS_SUPPLEMENTARYDATA, 1);
        }
    }

}

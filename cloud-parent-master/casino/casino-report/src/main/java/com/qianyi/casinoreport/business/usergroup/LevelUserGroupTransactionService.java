package com.qianyi.casinoreport.business.usergroup;


import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinoreport.business.LevelProxyDayReportBusiness;
import com.qianyi.casinoreport.business.LevelProxyReportBusiness;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LevelUserGroupTransactionService {

    @Autowired
    private LevelProxyDayReportBusiness levelproxyDayReportBusiness;

    @Autowired
    private LevelProxyReportBusiness levelproxyReportBusiness;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void processProxyUserBOList(List<ProxyUserBO> proxyUserBOList) {
            if (proxyUserBOList!=null && proxyUserBOList.size()>0){
                String routingKey=null;
                for (int i = 0; i < proxyUserBOList.size(); i++) {
                    if (i==0){
                        routingKey= RabbitMqConstants.ONE_ADDUSERTOTEAM_DIRECT;
                    }
                    if (i==1){
                        routingKey= RabbitMqConstants.TWO_ADDUSERTOTEAM_DIRECT;
                    }
                    if (i==2){
                        routingKey= RabbitMqConstants.THREE_ADDUSERTOTEAM_DIRECT;
                    }
                    rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_ADDUSERTOTEAM_DIRECTQUEUE_DIRECTEXCHANGE,
                            routingKey,proxyUserBOList.get(i), new CorrelationData(UUID.randomUUID().toString()));
                }
            }
    }

    @Transactional(rollbackFor = Exception.class)
    public void processItem(ProxyUserBO proxyUserBO){
        log.info("process proxy user BO item");
        levelproxyReportBusiness.processUser(proxyUserBO);
        levelproxyDayReportBusiness.processUser(proxyUserBO);
    }
}

package com.qianyi.casinoreport.business.recharge;


import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.service.ConsumerErrorService;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import com.qianyi.casinoreport.business.LevelProxyDayReportBusiness;
import com.qianyi.casinoreport.business.LevelProxyReportBusiness;
import com.qianyi.casinoreport.business.ProxyDayReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import com.qianyi.casinoreport.util.ReportConstant;
import com.qianyi.casinoreport.util.ShareProfitUtils;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LevelRechargeRecordBussiness {

    @Autowired
    private LevelProxyDayReportBusiness levelproxyDayReportBusiness;

    @Autowired
    private LevelProxyReportBusiness levelproxyReportBusiness;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private ConsumerErrorService consumerErrorService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 内部调用，单线程
     * 统计新增金额，人数
     * @param rechargeRecordVo 充值消息对象
     */
    public void procerssShareProfit(RechargeRecordVo rechargeRecordVo){
        try {
            if(rechargeRecordVo == null || rechargeRecordVo.getUserId() == null || rechargeRecordVo.getFirstUserId() == null){
                return;
            }
            log.info("充值消息对象：{}", rechargeRecordVo);
            List<RechargeProxyBO> rechargeProxyList = getRechargeProxys(rechargeRecordVo);
            processList(rechargeProxyList);
        }catch (Exception e){
            log.error("recharge error consumer : {}",e);
            recordFailVo(rechargeRecordVo);
        }

    }

    private void recordFailVo(RechargeRecordVo rechargeRecordVo){
        ConsumerError consumerError = new ConsumerError();
        consumerError.setConsumerType(ReportConstant.RECHARGE);
        consumerError.setMainId(rechargeRecordVo.getChargeOrderId());
        consumerError.setRepairStatus(0);
        consumerErrorService.save(consumerError);
    }


    /**
     * 各级代理充值处理
     * @param rechargeProxyList
     */
    public void processList(List<RechargeProxyBO> rechargeProxyList) {
        if (rechargeProxyList!=null && rechargeProxyList.size()>0){
            String routingKey=null;
            for (int i = 0; i < rechargeProxyList.size(); i++) {
                if (i==0){
                    routingKey= RabbitMqConstants.ONE_INGCHARGEORDER_DIRECT;
                }
                if (i==1){
                    routingKey= RabbitMqConstants.TWO_INGCHARGEORDER_DIRECT;
                }
                if (i==2){
                    routingKey= RabbitMqConstants.THREE_INGCHARGEORDER_DIRECT;
                }
                rabbitTemplate.convertAndSend(RabbitMqConstants.LEVEL_CHARGEORDER_DIRECTQUEUE_DIRECTEXCHANGE,
                        routingKey,rechargeProxyList.get(i), new CorrelationData(UUID.randomUUID().toString()));
            }
        }
    }
    /**
     * 各代理充值相关数据入库
     * @param rechargeProxy
     */
    public void processItem(RechargeProxyBO rechargeProxy){
        levelproxyDayReportBusiness.processChargeAmount(rechargeProxy);
        levelproxyReportBusiness.processRechargeReport(rechargeProxy);
    }

    public List<RechargeProxyBO> getRechargeProxys(RechargeRecordVo rechargeRecordVo){
        List<RechargeProxyBO> rechargeProxyList = new ArrayList<>();
        if(ShareProfitUtils.compareIntegerNotNull(rechargeRecordVo.getFirstUserId()))
            rechargeProxyList.add(getItemByVo(rechargeRecordVo.getFirstUserId(),rechargeRecordVo,true));
        if(ShareProfitUtils.compareIntegerNotNull(rechargeRecordVo.getSecondUserId()))
            rechargeProxyList.add(getItemByVo(rechargeRecordVo.getSecondUserId(),rechargeRecordVo,false));
        if(ShareProfitUtils.compareIntegerNotNull(rechargeRecordVo.getThirdUserId()))
            rechargeProxyList.add(getItemByVo(rechargeRecordVo.getThirdUserId(),rechargeRecordVo,false));
        log.info("get list object is {}",rechargeProxyList);
        return rechargeProxyList;
    }

    private RechargeProxyBO getItemByVo(Long proxyUserId, RechargeRecordVo rechargeRecordVo, boolean isDrect){
        RechargeProxyBO rechargeProxy = new RechargeProxyBO();
        rechargeProxy.setProxyUserId(proxyUserId);
        rechargeProxy.setAmount(rechargeRecordVo.getChargeAmount());
        rechargeProxy.setIsFirst(rechargeRecordVo.getIsFirst());
        rechargeProxy.setDirect(isDrect);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        rechargeProxy.setDayTime(format.format(rechargeRecordVo.getCreateTime()));
        rechargeProxy.setChargeOrderId(rechargeRecordVo.getChargeOrderId());
        return rechargeProxy;
    }
}

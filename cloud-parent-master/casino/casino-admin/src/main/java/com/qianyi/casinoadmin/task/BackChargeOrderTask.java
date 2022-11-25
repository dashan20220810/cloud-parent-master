package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.service.ChargeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BackChargeOrderTask {
    @Autowired
    private ChargeOrderService chargeOrderService;
//    @Scheduled(cron = TaskConst.BACK_ORDER)
//    public void updateOrderChargeOrder(){
//        log.info("修改超时充值订单开始执行start=============================================》");
//        Calendar nowTime = Calendar.getInstance();
//        nowTime.add(Calendar.MINUTE, -30);
//        String format = DateUtil.getSimpleDateFormat().format(nowTime.getTime());
//        chargeOrderService.updateChargeOrders(CommonConst.NUMBER_0, format);
//    }
}

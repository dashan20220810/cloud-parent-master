package com.qianyi.casinocore.job;

import com.qianyi.casinocore.service.ProxyGameRecordReportService;
import com.qianyi.casinocore.service.UserGameRecordReportService;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class ProxyGameRecordReportJob implements AsyncService<ProxyGameRecordReportVo> {

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @Override
    public void executeAsync(ProxyGameRecordReportVo proxyGameRecordReportVo) {
        if (proxyGameRecordReportVo == null){
            log.error("代理报表异步处理异常参数为空");
            return;
        }
        try {
            Date date = DateUtil.getSimpleDateFormat().parse(proxyGameRecordReportVo.getOrderTimes());
            Date americaDate = cn.hutool.core.date.DateUtil.offsetHour(date, -12);//转为美东时间保存,代理报表全部用美东时间
            String orderTimes = DateUtil.dateToPatten1(americaDate);
            Long proxyGameRecordReportId = CommonUtil.toHash(orderTimes+proxyGameRecordReportVo.getUserId().toString());
            if (proxyGameRecordReportVo.getThirdProxy() == null){
                proxyGameRecordReportService.updateKey(proxyGameRecordReportId,proxyGameRecordReportVo.getUserId(),
                    orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),0L,
                    0L,0L,proxyGameRecordReportVo.getBetAmount());
            }else {
                proxyGameRecordReportService.updateKey(proxyGameRecordReportId,proxyGameRecordReportVo.getUserId(),
                    orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getFirstProxy(),
                    proxyGameRecordReportVo.getSecondProxy(),proxyGameRecordReportVo.getThirdProxy(),proxyGameRecordReportVo.getBetAmount());
            }

            Long userGameRecordReportId = CommonUtil.toHash(orderTimes+proxyGameRecordReportVo.getUserId().toString()+proxyGameRecordReportVo.getPlatform());
//            if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_WM)){//会员报表单单wm使用美东时间
//                userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),orderTimes,
//                    proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());
//            }else {
//                orderTimes = DateUtil.dateToPatten1(date);
//                userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),orderTimes,
//                    proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());
//            }
            orderTimes = DateUtil.dateToPatten1(date);
            userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),orderTimes,
                proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());
        }catch (Exception ex){
            log.error("代理报表异步处理异常需要人工补单,注单标识{}",proxyGameRecordReportVo.getOrderId());
        }
    }
}

package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinocore.vo.RechargeProxyBO;
import com.qianyi.casinocore.vo.ShareProfitBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ProxyReportBusiness {
    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private UserService userService;
    /**
     * 处理分润报表
     * @param shareProfitBO
     */
    public ProxyReport processReport(ShareProfitBO shareProfitBO){
        ProxyReport proxyReport = getProxyReport(shareProfitBO.getUserId());
        allProxy(proxyReport,shareProfitBO);
        if(shareProfitBO.isDirect())
            directProxy(proxyReport,shareProfitBO);
        else
            noDirectProxy(proxyReport,shareProfitBO);
        return proxyReport;
//        proxyReportService.save(proxyReport);
    }

    public void allProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setAllBetAmount(proxyReport.getAllBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setAllProfitAmount(proxyReport.getAllProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setAllBetNum(proxyReport.getAllBetNum()+ (shareProfitBO.isFirst()?1:0));
    }

    public void directProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setDirectBetAmount(proxyReport.getDirectBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setDirectProfitAmount(proxyReport.getDirectProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setDirectBetNum(proxyReport.getDirectBetNum()+ (shareProfitBO.isFirst()?1:0));
    }

    public void noDirectProxy(ProxyReport proxyReport,ShareProfitBO shareProfitBO){
        proxyReport.setOtherBetAmount(proxyReport.getOtherBetAmount().add(shareProfitBO.getBetAmount()));
        proxyReport.setOtherProfitAmount(proxyReport.getOtherProfitAmount().add(shareProfitBO.getProfitAmount()));
        proxyReport.setOtherBetNum(proxyReport.getOtherBetNum()+ (shareProfitBO.isFirst()?1:0));
    }

    public ProxyReport buildProxyReport(Long userId) {
        log.info("build proxy report user id is {}",userId);
        User user = userService.findById(userId);
        ProxyReport proxyReport = new ProxyReport();
        proxyReport.setUserId(userId);
        proxyReport.setAccount(user.getAccount());
        proxyReportService.save(proxyReport);
        return proxyReport;
    }


    /**
     * 处理充值报表
     */
    public ProxyReport processRechargeReport(RechargeProxyBO rechargeProxy){
        log.info("process recharge proxy bo {}",rechargeProxy);
        // 判断是否首充
        if(rechargeProxy.getIsFirst()==1){
            log.info("is not first charge");
            return null;
        }

        ProxyReport proxyReport = getProxyReport(rechargeProxy.getProxyUserId());
        proxyReport.setAllChargeNum(proxyReport.getAllChargeNum()+1);
        if(rechargeProxy.isDirect())
            proxyReport.setDirectChargeNum(proxyReport.getDirectChargeNum()+1);
        else
            proxyReport.setOtherChargeNum(proxyReport.getOtherChargeNum()+1);
        return proxyReport;
//        proxyReportService.save(proxyReport);
    }



    /**
     * 处理团队用户数量
     * @param proxyUserBO
     */
    public ProxyReport processUser(ProxyUserBO proxyUserBO){
        log.info("query proxy report from db");
        ProxyReport proxyReport = getProxyReport(proxyUserBO.getProxyUserId());
        log.info("set proxy report userId {} group num value {} {} {}", proxyReport.getUserId(),proxyReport.getAllGroupNum(), proxyReport.getDirectGroupNum(), proxyReport.getOtherGroupNum());
        proxyReport.setAllGroupNum(proxyReport.getAllGroupNum()+1);
        if(proxyUserBO.isDrect())
            proxyReport.setDirectGroupNum(proxyReport.getDirectGroupNum()+1);
        else
            proxyReport.setOtherGroupNum(proxyReport.getOtherGroupNum()+1);
        return proxyReport;
    }

    public ProxyReport getProxyReport(Long userId) {
        log.info("user id is {}",userId);
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if(proxyReport == null)
            buildProxyReport(userId);
        return proxyReportService.findByUserIdWithLock(userId);
    }
}

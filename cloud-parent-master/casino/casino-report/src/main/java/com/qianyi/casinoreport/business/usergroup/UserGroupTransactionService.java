package com.qianyi.casinoreport.business.usergroup;

import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinoreport.business.ProxyDayReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserGroupTransactionService {

    @Autowired
    private ProxyDayReportBusiness proxyDayReportBusiness;

    @Autowired
    private ProxyReportBusiness proxyReportBusiness;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private ProxyReportService proxyReportService;

    @Transactional(rollbackFor = Exception.class)
    public void processProxyUserBOList(List<ProxyUserBO> proxyUserBOList) {
        List<ProxyDayReport> proxyDayReportList = new ArrayList<>();
        List<ProxyReport> proxyReportList = new ArrayList<>();
        proxyUserBOList.forEach(item->processItem(item,proxyDayReportList,proxyReportList));
        log.info("proxyReportList:{}",proxyReportList);
        proxyDayReportService.saveAll(proxyDayReportList);
        proxyReportService.saveAll(proxyReportList);
    }

    private void processItem(ProxyUserBO proxyUserBO,List<ProxyDayReport> proxyDayReportList,List<ProxyReport> proxyReportList){
        log.info("process proxy user BO item");
        proxyReportList.add(proxyReportBusiness.processUser(proxyUserBO));
        proxyDayReportList.add(proxyDayReportBusiness.processUser(proxyUserBO));
    }
}

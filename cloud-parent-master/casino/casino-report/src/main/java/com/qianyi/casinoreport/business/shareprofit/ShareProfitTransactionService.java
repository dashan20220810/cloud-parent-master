package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ShareProfitTransactionService {

    @Autowired
    private ShareprofitItemService sharepointItemService;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameRecordService gameRecordService;

    @Transactional(rollbackFor = Exception.class)
    public void processShareProfitList(List<ShareProfitBO> shareProfitBOList, GameRecord record) {
        Long startTime = System.currentTimeMillis();
        List<ProxyDayReport> proxyDayReportList = new ArrayList<>();
        List<ProxyReport> proxyReportList = new ArrayList<>();
        List<UserMoney> userMoneyList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        List<ShareProfitChange> shareProfitChangeList = new ArrayList<>();


        shareProfitBOList.forEach(item-> sharepointItemService.processItem(item,record,proxyDayReportList,proxyReportList,userList,userMoneyList,shareProfitChangeList));
        log.info("shareProfitBOList processItem That took {} milliseconds",System.currentTimeMillis()-startTime);
        startTime = System.currentTimeMillis();
        proxyDayReportService.saveAll(proxyDayReportList);
        proxyReportService.saveAll(proxyReportList);
        userMoneyList.forEach(item->userMoneyService.changeProfit(item.getUserId(),item.getShareProfit()));
        shareProfitChangeService.saveAll(shareProfitChangeList);
        userService.saveAll(userList);
        log.info("all store That took {} milliseconds",System.currentTimeMillis()-startTime);
//        int i = 1/0;
        startTime = System.currentTimeMillis();
        updateShareProfitStatus(record);
        log.info("processShareProfitList That took {} milliseconds",System.currentTimeMillis()-startTime);
    }

    /**
     * 更新分润状态
     *
     * @param record
     */
    public void updateShareProfitStatus(GameRecord record) {
        gameRecordService.updateProfitStatus(record.getId(),Constants.yes);
    }
}

package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.constant.ShareProfitConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinoreport.business.LevelProxyReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import com.qianyi.casinoreport.business.LevelProxyDayReportBusiness;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringrabbitmq.config.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LevelShareprofitItemService {

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserReportService userReportService;


    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private LevelProxyDayReportBusiness levelProxyDayReportBusiness;
    @Autowired
    private LevelProxyReportBusiness levelproxyReportBusiness;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    /**
     *  处理各级代理分润入库
     * @param shareProfitBO
     */
    @Transactional(rollbackFor = Exception.class)
    public void levelProcessItem(ShareProfitBO shareProfitBO){
        Long startTime = System.currentTimeMillis();
        ShareProfitChange ShareProfitChangeInfo = shareProfitChangeService.findByUserIdAndOrderNo(shareProfitBO.getUserId(), shareProfitBO.getRecordBetId());
        if (ShareProfitChangeInfo==null){
            UserMoney userMoney = userMoneyService.findUserByUserIdUse(shareProfitBO.getUserId());
            User user = userReportService.findUserByUserIdUse(shareProfitBO.getRecordUserId());
            if(userMoney==null)return;
            log.info("shareProfitBOList processItem That took {} milliseconds",System.currentTimeMillis()-startTime);
            //明细入库
            levelProcessProfitDetail(shareProfitBO,userMoney);
            //进行分润
            //userMoney.setShareProfit(userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()));
            userMoneyService.changeProfit(userMoney.getUserId(),shareProfitBO.getProfitAmount());

            //进行日报表处理
            levelProxyDayReportBusiness.processReport(shareProfitBO);
            //进行总报表处理
            levelproxyReportBusiness.processReport(shareProfitBO);

            if(user.getIsFirstBet()==Constants.no){
                //设置第一次投注用户
                userService.updateIsFirstBet(user.getId(), Constants.yes);
            }
            log.info("all store That took {} milliseconds",System.currentTimeMillis()-startTime);

            //根据不同的游戏修改游戏分润状态
            if (shareProfitBO.getGameType()==1){
                gameRecordService.updateProfitStatus(shareProfitBO.getRecordId(), Constants.yes);
            }else if(shareProfitBO.getGameType()==4){
                gameRecordObdjService.updateProfitStatus(shareProfitBO.getRecordId(), Constants.yes);
            }else if(shareProfitBO.getGameType()==5){
                gameRecordObtyService.updateProfitStatus(shareProfitBO.getRecordId(), Constants.yes);
            }else {
                gameRecordGoldenFService.updateProfitStatus(shareProfitBO.getRecordId(), Constants.yes);
            }

            log.info("processShareProfitList That took {} milliseconds",System.currentTimeMillis()-startTime);
        }
    }

    private void levelProcessProfitDetail(ShareProfitBO shareProfitBO,UserMoney userMoney) {
        ShareProfitChange shareProfitChange = new ShareProfitChange();
        shareProfitChange.setAmount(shareProfitBO.getProfitAmount());
        shareProfitChange.setUserId(shareProfitBO.getUserId());
        shareProfitChange.setOrderNo(shareProfitBO.getRecordBetId());
        shareProfitChange.setAmountBefore(userMoney.getShareProfit());
        shareProfitChange.setAmountAfter(getAfterAmount(shareProfitBO,userMoney));
        shareProfitChange.setType(ShareProfitConstant.SHARE_PROFIT_TYPE);
        shareProfitChange.setFromUserId(shareProfitBO.getRecordUserId());
        shareProfitChange.setProfitRate(shareProfitBO.getCommission());
        shareProfitChange.setParentLevel(shareProfitBO.getParentLevel());
        shareProfitChange.setValidbet(shareProfitBO.getBetAmount());
        shareProfitChange.setBetTime(shareProfitBO.getBetDate());
        shareProfitChange.setGameType(shareProfitBO.getGameType());
        log.info("shareProfitBO:{}",shareProfitBO);
        shareProfitChangeService.save(shareProfitChange);
    }

    private BigDecimal getAfterAmount(ShareProfitBO shareProfitBO, UserMoney userMoney){
        return userMoney.getShareProfit().add(shareProfitBO.getProfitAmount()).setScale(6,BigDecimal.ROUND_HALF_UP);
    }
}

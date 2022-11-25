package com.qianyi.casinoreport.business.shareprofit;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.ShareProfitBO;
import com.qianyi.casinocore.vo.ShareProfitMqVo;
import com.qianyi.casinoreport.util.ReportConstant;
import com.qianyi.casinoreport.util.ShareProfitUtils;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ShareProfitBusiness {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private PlatformConfigService platformConfigService;

    @Autowired
    private ConsumerErrorService consumerErrorService;

    @Autowired
    private ShareProfitTransactionService shareProfitTransactionService;

    public int procerssShareProfit(ShareProfitMqVo shareProfitMqVo){
        try {
            PlatformConfig platformConfig = platformConfigService.findFirst();
            GameRecord record = gameRecordService.findGameRecordById(shareProfitMqVo.getGameRecordId());
            List<ShareProfitBO> shareProfitBOList = shareProfitOperator(platformConfig, shareProfitMqVo);
            shareProfitTransactionService.processShareProfitList(shareProfitBOList, record);
            return 0;
        }catch (Exception e){
            log.error("share profit error : {}",e);
            recordFailVo(shareProfitMqVo);
            return 1;
        }
    }

    private void recordFailVo(ShareProfitMqVo shareProfitMqVo){
        List<ConsumerError> consumerErrors = consumerErrorService.findUsersByUserId(shareProfitMqVo.getGameRecordId(),"sharePoint");
        if(consumerErrors.size()==0){
            ConsumerError consumerError = new ConsumerError();
            consumerError.setConsumerType(ReportConstant.SHAREPOINT);
            consumerError.setMainId(shareProfitMqVo.getGameRecordId());
            consumerError.setRepairStatus(0);
            consumerErrorService.save(consumerError);
        }
    }

    private List<ShareProfitBO> shareProfitOperator(PlatformConfig platformConfig, ShareProfitMqVo shareProfitMqVo) {
        Long startTime = System.currentTimeMillis();
        User user = userService.findById(shareProfitMqVo.getUserId());
        log.info("shareProfitOperator user:{}",user);
        String betTime = shareProfitMqVo.getBetTime().substring(0,10);
        List<ShareProfitBO> shareProfitBOList = new ArrayList<>();
        if(ShareProfitUtils.compareIntegerNotNull( user.getFirstPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getFirstPid(),shareProfitMqVo.getValidbet(),platformConfig.getFirstCommission(),getUserIsFirstBet(user),betTime,true,1));
        if(ShareProfitUtils.compareIntegerNotNull( user.getSecondPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getSecondPid(),shareProfitMqVo.getValidbet(),platformConfig.getSecondCommission(),getUserIsFirstBet(user),betTime,false,2));
        if(ShareProfitUtils.compareIntegerNotNull( user.getThirdPid()))
            shareProfitBOList.add(getShareProfitBO(user,user.getThirdPid(),shareProfitMqVo.getValidbet(),platformConfig.getThirdCommission(),getUserIsFirstBet(user),betTime,false,3));
        log.info("get list object is {}",shareProfitBOList);
        log.info("shareProfitOperator That took {} milliseconds",System.currentTimeMillis()-startTime);
        return shareProfitBOList;
    }

    private ShareProfitBO getShareProfitBO(User user,Long userId,BigDecimal betAmount,BigDecimal commission,Boolean isFirst,String betTime,boolean direct,Integer parentLevel){
        ShareProfitBO shareProfitBO = new ShareProfitBO();
        shareProfitBO.setFromUserId(user.getId());
        shareProfitBO.setUserId(userId);
        shareProfitBO.setBetAmount(betAmount);
        shareProfitBO.setProfitAmount(betAmount.multiply(commission.divide(BigDecimal.valueOf(100))));
        shareProfitBO.setFirst(isFirst);
        shareProfitBO.setBetTime(betTime);
        shareProfitBO.setDirect(direct);
        shareProfitBO.setCommission(commission);
        shareProfitBO.setParentLevel(parentLevel);
        log.info("user:{} \\n shareProfitBO{}",user,shareProfitBO);
        return shareProfitBO;
    }

    private boolean getUserIsFirstBet(User user){
        if (user.getIsFirstBet() != null && user.getIsFirstBet() == Constants.no)
            return true;
        return false;
    }
}

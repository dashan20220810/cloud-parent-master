package com.qianyi.casinoreport.business.usergroup;

import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.model.ProxyDayReport;
import com.qianyi.casinocore.model.ProxyReport;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ConsumerErrorService;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.vo.ProxyUserBO;
import com.qianyi.casinocore.vo.RechargeRecordVo;
import com.qianyi.casinoreport.business.ProxyDayReportBusiness;
import com.qianyi.casinoreport.business.ProxyReportBusiness;
import com.qianyi.casinoreport.util.ReportConstant;
import com.qianyi.casinoreport.util.ShareProfitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserGroupNumBusiness {

    @Autowired
    private UserGroupTransactionService userGroupTransactionService;

    @Autowired
    private ConsumerErrorService consumerErrorService;

    /**
     * 内部调用，单线程
     * 统计新增金额，人数
     */
    public void processUser(User user){
        try{
            log.info("新增的用户对象：{}", user);
            if(user == null){
                return;
            }
            List<ProxyUserBO> proxyUserBOList = getGroupUserNum(user);
            userGroupTransactionService.processProxyUserBOList(proxyUserBOList);
        }catch (Exception e){
            log.error("user consumer error is {}",e);
            recordFailVo(user);
        }
    }

    private void recordFailVo(User user){
        List<ConsumerError> consumerErrors = consumerErrorService.findUsersByUserId(user.getId(),"user");
        if(consumerErrors.size()==0){
            ConsumerError consumerError = new ConsumerError();
            consumerError.setConsumerType(ReportConstant.USER);
            consumerError.setMainId(user.getId());
            consumerError.setRepairStatus(0);
            consumerErrorService.save(consumerError);
        }
    }



    private List<ProxyUserBO> getGroupUserNum(User user) {
        List<ProxyUserBO> proxyUserBOList = new ArrayList<>();
        if(ShareProfitUtils.compareIntegerNotNull(user.getFirstPid()))
            proxyUserBOList.add(getProxyUser(user.getFirstPid(),user,true));
        if(ShareProfitUtils.compareIntegerNotNull(user.getSecondPid()))
            proxyUserBOList.add(getProxyUser(user.getSecondPid(),user,false));
        if(ShareProfitUtils.compareIntegerNotNull(user.getThirdPid()))
            proxyUserBOList.add(getProxyUser(user.getThirdPid(),user,false));
        log.info("get list BO {}",proxyUserBOList);
        return proxyUserBOList;
    }

    private ProxyUserBO getProxyUser(Long userId,User user, boolean isDirect) {;
        ProxyUserBO proxyUserBO = new ProxyUserBO();
        proxyUserBO.setProxyUserId(userId);
        proxyUserBO.setDrect(isDirect);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        proxyUserBO.setDayTime(format.format(user.getCreateTime()));
        log.info("generate bo is {}",proxyUserBO);
        return proxyUserBO;
    }
}

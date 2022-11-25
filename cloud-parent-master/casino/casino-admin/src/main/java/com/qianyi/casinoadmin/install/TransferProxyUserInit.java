package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyCommissionService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Order(value = 10)
public class TransferProxyUserInit implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    public static final String firstProxy1Name = "ror";
//    public static final String firstProxy1Name = "zhenhao01dai";

    public static final String secondProxy1Name = "zhenhao01dai04";

    public static final String firstProxy2Name = "chun888ZD";
    public static final String secondProxy2Name = "ajiaoQD";

    public static final String firstProxy3Name = "mrhope";
    public static final String secondProxy3Name = "simaQD";

    public static final String secondProxy4Name = "simaQD";
    public static final String thirdProxy4Name = "simaJD";

    public static final String secondProxy5Name = "ajiaoQD";
    public static final String thirdProxy5Name = "ajiaoJD";

//    public static final String secondProxy6Name = "zhenhao01dai03";
//    public static final String secondProxy6Name = "ror1";
//
//    public static final String thirdProxy6Name = "D018JD";
    @Override
    public void run(String... args) throws Exception {
//        ProxyUser firstProxy1 = proxyUserService.findByUserName(firstProxy1Name);
//        if (LoginUtil.checkNull(firstProxy1) || firstProxy1.getProxyRole() != CommonConst.NUMBER_1){
//            log.error("找不到总代{}",firstProxy1Name);
//        }else {
//            ProxyUser secondProxy1 = proxyUserService.findByUserName(secondProxy1Name);
//            if (!LoginUtil.checkNull(secondProxy1) && secondProxy1.getProxyRole() == CommonConst.NUMBER_2){
//                if (secondProxy1.getFirstProxy().longValue() == firstProxy1.getId().longValue()){
//                    log.info("已经是改总代下级不能转移");
//                }else {
//                    log.info("开始转移{}",secondProxy1Name);
//                    secondProxyToFirstProxy(secondProxy1,firstProxy1);
//                }
//            }
//        }

        this.secondProxyToFirstProxy();
        this.thirdProxyToSecondProxy();
    }

    private void thirdProxyToSecondProxy(){
//        ProxyUser secondProxy6 = proxyUserService.findByUserName(secondProxy6Name);
//        if (LoginUtil.checkNull(secondProxy6) || secondProxy6.getProxyRole() != CommonConst.NUMBER_2){
//            log.error("找不到区代{}",secondProxy6Name);
//        }else {
//            ProxyUser thirdProxy6 = proxyUserService.findByUserName(thirdProxy6Name);
//            if (!LoginUtil.checkNull(thirdProxy6) && thirdProxy6.getProxyRole() == CommonConst.NUMBER_3){
//                if (thirdProxy6.getSecondProxy().longValue() == secondProxy6.getId().longValue()){
//                    log.info("已经是该区域代下级不能转移");
//                }else {
//                    log.info("开始转移{}",thirdProxy6Name);
//                    thirdProxyToSecondProxy(thirdProxy6,secondProxy6);
//                }
//
//            }
//        }

//        ProxyUser secondProxy4 = proxyUserService.findByUserName(secondProxy4Name);
//        if (LoginUtil.checkNull(secondProxy4) || secondProxy4.getProxyRole() != CommonConst.NUMBER_2){
//            log.error("找不到区代{}",secondProxy4Name);
//        }else {
//            ProxyUser thirdProxy4 = proxyUserService.findByUserName(thirdProxy4Name);
//            if (!LoginUtil.checkNull(thirdProxy4) && thirdProxy4.getProxyRole() == CommonConst.NUMBER_3){
//                if (thirdProxy4.getSecondProxy().longValue() == secondProxy4.getId().longValue()){
//                    log.info("已经是该区域代下级不能转移");
//                }else {
//                    log.info("开始转移{}",thirdProxy4Name);
//                    thirdProxyToSecondProxy(thirdProxy4,secondProxy4);
//                }
//
//            }
//        }
//
//        ProxyUser secondProxy5 = proxyUserService.findByUserName(secondProxy5Name);
//        if (LoginUtil.checkNull(secondProxy5) || secondProxy5.getProxyRole() != CommonConst.NUMBER_2){
//            log.error("找不到区代{}",secondProxy5Name);
//        }else {
//            ProxyUser thirdProxy5 = proxyUserService.findByUserName(thirdProxy5Name);
//            if (!LoginUtil.checkNull(thirdProxy5) && thirdProxy5.getProxyRole() == CommonConst.NUMBER_3){
//                if (thirdProxy5.getSecondProxy().longValue() == secondProxy5.getId().longValue()){
//                    log.info("已经是该区域代下级不能转移");
//                }else {
//                    log.info("开始转移{}",thirdProxy5Name);
//                    thirdProxyToSecondProxy(thirdProxy5,secondProxy5);
//                }
//
//            }
//        }
    }

    private void thirdProxyToSecondProxy(ProxyUser thirdProxy,ProxyUser secondProxy){
        User user = new User();
        user.setThirdProxy(thirdProxy.getId());
        List<User> userList = userService.findUserList(user, null, null);
        if (userList == null || userList.size() == CommonConst.NUMBER_0){
            log.info("被转移基层代理id{}无会员",thirdProxy.getId());
        }else {
            log.info("被转移基层代理id{}会员数量{}",thirdProxy.getId(),userList.size());
            userList.forEach(user1 -> {
                user1.setFirstProxy(secondProxy.getFirstProxy());
                user1.setSecondProxy(secondProxy.getId());
                userService.save(user1);
            });
            userList.clear();
        }

        //修改两个区域代的下级人数
        proxyUserService.subProxyUsersNum(thirdProxy.getSecondProxy(),CommonConst.NUMBER_1);
        proxyUserService.addProxyUsersNum(secondProxy.getId(),CommonConst.NUMBER_1);
        //修改两个总代的下级人数
        if (thirdProxy.getFirstProxy().longValue() != secondProxy.getFirstProxy().longValue()){
            proxyUserService.subProxyUsersNum(thirdProxy.getFirstProxy(),CommonConst.NUMBER_1);
            proxyUserService.addProxyUsersNum(secondProxy.getFirstProxy(),CommonConst.NUMBER_1);
        }

        //修改被转移代理区域代理id
        thirdProxy.setSecondProxy(secondProxy.getId());
        thirdProxy.setFirstProxy(secondProxy.getFirstProxy());
        proxyUserService.save(thirdProxy);

        ProxyCommission byProxyUserId = proxyCommissionService.findByProxyUserId(thirdProxy.getId());
        if (byProxyUserId != null){
            byProxyUserId.setSecondProxy(secondProxy.getId());
            proxyCommissionService.save(byProxyUserId);
        }
    }

    private void secondProxyToFirstProxy(){
//        ProxyUser firstProxy2 = proxyUserService.findByUserName(firstProxy2Name);
//        if (LoginUtil.checkNull(firstProxy2) || firstProxy2.getProxyRole() != CommonConst.NUMBER_1){
//            log.error("找不到总代{}",firstProxy2Name);
//        }else {
//            ProxyUser secondProxy2 = proxyUserService.findByUserName(secondProxy2Name);
//            if (!LoginUtil.checkNull(secondProxy2) && secondProxy2.getProxyRole() == CommonConst.NUMBER_2){
//                if (secondProxy2.getFirstProxy().longValue() == firstProxy2.getId().longValue()){
//                    log.info("已经是改总代下级不能转移");
//                }else {
//                    log.info("开始转移{}",secondProxy2Name);
//                    secondProxyToFirstProxy(secondProxy2,firstProxy2);
//                }
//
//            }
//        }
//
//        ProxyUser firstProxy3 = proxyUserService.findByUserName(firstProxy3Name);
//        if (LoginUtil.checkNull(firstProxy3) || firstProxy3.getProxyRole() != CommonConst.NUMBER_1){
//            log.error("找不到总代{}",firstProxy3Name);
//        }else {
//            ProxyUser secondProxy3 = proxyUserService.findByUserName(secondProxy3Name);
//            if (!LoginUtil.checkNull(secondProxy3) && secondProxy3.getProxyRole() == CommonConst.NUMBER_2){
//                if (secondProxy3.getFirstProxy().longValue() == firstProxy3.getId().longValue()){
//                    log.info("已经是改总代下级不能转移");
//                }else {
//                    log.info("开始转移{}",secondProxy3Name);
//                    secondProxyToFirstProxy(secondProxy3,firstProxy3);
//                }
//
//            }
//        }

    }

    private void secondProxyToFirstProxy(ProxyUser secondProxy,ProxyUser firstProxy){
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setSecondProxy(secondProxy.getId());
        proxyUser.setProxyRole(CommonConst.NUMBER_3);
        List<ProxyUser> proxyUsers = proxyUserService.findProxyUserList(proxyUser);
        if(proxyUsers == null || proxyUsers.size() == CommonConst.NUMBER_0){
            log.info("转移代理被转移者id{}账号{}无下级",secondProxy.getId(),secondProxy.getUserName());
            return ;
        }
        log.info("转移代理被转移者id{} 下级数量{}",proxyUser.getSecondProxy(),proxyUsers.size());
        proxyUsers.forEach(proxyUser1 -> {
            proxyUser1.setFirstProxy(firstProxy.getId());
            proxyUserService.save(proxyUser1);
        });


        //查询会员并转移
        User user = new User();
        user.setSecondProxy(secondProxy.getId());
        List<User> userList = userService.findUserList(user, null, null);
        if (userList != null || userList.size() >= CommonConst.NUMBER_1){
            log.info("转移代理被转移者id{} 会员数量{}",proxyUser.getSecondProxy(),userList.size());
            userList.forEach(user1 -> {
                user1.setFirstProxy(firstProxy.getId());
                userService.save(user1);
            });
            userList.clear();
        }

        proxyUserService.subProxyUsersNum(secondProxy.getFirstProxy(),proxyUsers.size()+1);
        proxyUserService.addProxyUsersNum(firstProxy.getId(),proxyUsers.size()+1);
        secondProxy.setFirstProxy(firstProxy.getId());
        proxyUserService.save(secondProxy);

    }
}

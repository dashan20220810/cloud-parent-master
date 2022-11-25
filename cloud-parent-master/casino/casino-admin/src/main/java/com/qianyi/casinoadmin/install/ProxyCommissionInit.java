//package com.qianyi.casinoadmin.install;
//
//import com.qianyi.casinoadmin.util.LoginUtil;
//import com.qianyi.casinocore.model.ProxyCommission;
//import com.qianyi.casinocore.model.ProxyUser;
//import com.qianyi.casinocore.service.ProxyCommissionService;
//import com.qianyi.casinocore.service.ProxyUserService;
//import com.qianyi.casinocore.util.CommonConst;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Component
//@Slf4j
//@Order(value = 6)
//public class ProxyCommissionInit implements CommandLineRunner {
//    @Autowired
//    private ProxyUserService proxyUserService;
//
//    @Autowired
//    private ProxyCommissionService proxyCommissionService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("初始化代理返佣配置开始============================================》");
//        this.secondProxy();
//        this.thirdProxy();
//        log.info("初始化代理返佣配置结束============================================》");
//    }
//    private void secondProxy(){
//        ProxyUser proxyUser = new ProxyUser();
//        proxyUser.setProxyRole(CommonConst.NUMBER_2);
//        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
//        if (LoginUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
//            return;
//        }
//        proxyUserList.stream().forEach(proxy->{
//            ProxyCommission secondCommission = proxyCommissionService.findByProxyUserId(proxy.getId());
//            if (LoginUtil.checkNull(secondCommission)){
//                log.warn("区域代理{}返佣配置未初始化=================>id{}",proxy.getUserName(),proxy.getId());
//                ProxyCommission proxyCommission = new ProxyCommission();
//                proxyCommission.setProxyUserId(proxy.getId());
//                proxyCommissionService.save(proxyCommission);
//            }
//        });
//    }
//    private void thirdProxy(){
//        ProxyUser proxyUser = new ProxyUser();
//        proxyUser.setProxyRole(CommonConst.NUMBER_3);
//        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
//        if (LoginUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
//            return;
//        }
//        proxyUserList.stream().forEach(proxy->{
//            ProxyCommission thirdCommission = proxyCommissionService.findByProxyUserId(proxy.getId());
//            if (LoginUtil.checkNull(thirdCommission)){
//                log.warn("基层代理{}返佣配置未初始化=================>id{}",proxy.getUserName(),proxy.getId());
//                ProxyCommission proxyCommission = new ProxyCommission();
//                proxyCommission.setProxyUserId(proxy.getId());
//                proxyCommission.setSecondProxy(proxy.getSecondProxy());
//                ProxyCommission secondCommission = proxyCommissionService.findByProxyUserId(proxy.getSecondProxy());
//                proxyCommission.setFirstCommission((secondCommission == null || secondCommission.getFirstCommission() == null)?
//                    BigDecimal.ZERO:secondCommission.getFirstCommission());
//                proxyCommissionService.save(proxyCommission);
//            }
//        });
//    }
//}

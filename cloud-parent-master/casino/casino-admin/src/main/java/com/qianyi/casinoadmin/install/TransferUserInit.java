package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(value = 8)
public class TransferUserInit implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    private static List<String> users1 = new ArrayList<>();
    private static String proxyName1 = "haotiandl";

    static {
        users1.add("xinghe8866");
//        users1.add("SENGLY5555");
//        users1.add("Sarun5555");
//        users1.add("Samnang2022");
//        users1.add("ahyokh168");
//        users1.add("bongvina005");
    }

    private static List<String> users2 = new ArrayList<>();
    private static String proxyName2 = "taisendl";
    static {
        users2.add("jinli8866");
//        users2.add("SENG99");
//        users2.add("volvo888");
//        users2.add("coco168");
//        users2.add("ahyokh168");
//        users1.add("bongvina005");
    }

    private static List<String> proxy1 = new ArrayList<>();

    static {
        proxy1.add("L055JD");
        proxy1.add("X045JD");
        proxy1.add("X043JD");

//        proxy1.add("test01proxy11");
//        proxy1.add("tong78901");

    }
    private static String proxy1Name = "companyJD";

    @Override
    public void run(String... args) throws Exception {
        log.info("初始化转移会员开始============================================》");
        transferUser(users1,proxyName1);
        transferUser(users2,proxyName2);
//        proxyTransferUser(proxy1,proxy1Name);
        log.info("初始化转移会员结束============================================》");
    }

    private void proxyTransferUser(List<String> proxyUsers,String name){
        ProxyUser proxyUser = proxyUserService.findByUserName(name);
        if (LoginUtil.checkNull(proxyUser)){
            log.info("没有找到这个代理转移结束{}",name);
            return;
        }
        if (proxyUser.getProxyRole() != CommonConst.NUMBER_3){
            log.info("只有基层代理可以转移会员");
            return;
        }
        for (String str:proxyUsers){
            try {
                ProxyUser proxy = proxyUserService.findByUserName(str);
                if (LoginUtil.checkNull(proxy)){
                    log.info("没有找到这个代理转移结束{}",str);
                    continue;
                }
                if (proxy.getProxyRole() != CommonConst.NUMBER_3){
                    log.info("只有基层代理可以转移会员{}",str);
                    continue;
                }
                User user = new User();
                user.setThirdProxy(proxy.getId());
                List<User> userList = userService.findUserList(user, null, null);
                if (userList.isEmpty()){
                    log.info("该代理没有会员{}",str);
                    continue;
                }
                for (User u:userList){
                    log.info("被转移会员账号{}总代{}区代{}基代{}",u.getAccount(),u.getFirstProxy(),u.getSecondProxy(),u.getThirdProxy());
                    u.setFirstProxy(proxyUser.getFirstProxy());
                    u.setSecondProxy(proxyUser.getSecondProxy());
                    u.setThirdProxy(proxyUser.getId());
                    userService.save(u);
                    log.info("转移之后会员账号{}总代{}区代{}基代{}",u.getAccount(),u.getFirstProxy(),u.getSecondProxy(),u.getThirdProxy());
                }
            }catch (Exception ex){
                log.error("转移会员出现异常name{}ex:{}",str,ex);
            }
        }
    }

    private void transferUser(List<String> users,String proxyName){
        ProxyUser proxyUser = proxyUserService.findByUserName(proxyName);
        if (LoginUtil.checkNull(proxyUser)){
            log.info("没有找到这个代理转移结束{}",proxyName);
            return;
        }
        if (proxyUser.getProxyRole() != CommonConst.NUMBER_3){
            log.info("只有基层代理可以转移会员");
            return;
        }
        for (String str:users){
            User byAccount = userService.findByAccount(str);
            if (LoginUtil.checkNull(byAccount)){
                log.info("没有找到这个会员{}",str);
                continue;
            }
            log.info("被转移会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
            byAccount.setFirstProxy(proxyUser.getFirstProxy());
            byAccount.setSecondProxy(proxyUser.getSecondProxy());
            byAccount.setThirdProxy(proxyUser.getId());
            userService.save(byAccount);
            log.info("转移之后会员账号{}总代{}区代{}基代{}",byAccount.getAccount(),byAccount.getFirstProxy(),byAccount.getSecondProxy(),byAccount.getThirdProxy());
        }
    }
}

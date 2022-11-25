package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.service.WithdrawOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(4)
public class SqlInitialize  implements CommandLineRunner {
    //    @Autowired
    //    private SysPermissionService sysPermissionService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Override
    public void run(String... args) throws Exception {
        //        SysPermission sysPermission1 = sysPermissionService.findByName("历史盈亏报表");
        //        if (!LoginUtil.checkNull(sysPermission1)){
        //            sysPermission1.setName("会员报表");
        //            sysPermissionService.save(sysPermission1);
        //        }
        //        SysPermission sysPermission2 =sysPermissionService.findByName("代理实时报表");
        //        if (!LoginUtil.checkNull(sysPermission2)){
        //            sysPermission2.setName("代理报表");
        //            sysPermissionService.save(sysPermission2);
        //        }
        withdrawOrderService.updateWithdrawOrderAuditId(0L);
    }
}

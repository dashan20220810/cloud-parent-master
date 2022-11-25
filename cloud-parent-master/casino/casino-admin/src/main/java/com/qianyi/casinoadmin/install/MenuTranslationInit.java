package com.qianyi.casinoadmin.install;

import com.qianyi.casinoadmin.install.file.MenuTranslationFile;
import com.qianyi.casinoadmin.install.file.SysPermissionConfigFile;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.util.CommonConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 菜单翻译初始化
 */
@Component
@Slf4j
@Order(2)
public class MenuTranslationInit  implements CommandLineRunner {
    @Autowired
    private MenuTranslationFile menuTranslationFile;
    @Autowired
    private SysPermissionService sysPermissionService;
    @Override
    public void run(String... args) throws Exception {
        log.info("初始化菜单翻译开始============================================》");
        List<SysPermission> sysPermissions = sysPermissionService.findAll();
        if (LoginUtil.checkNull(sysPermissions) || sysPermissions.size() == CommonConst.NUMBER_0){
            log.info("菜单为空============================================》");
            return;
        }
        this.initializeEnglish(sysPermissions);
        this.initializeCambodian(sysPermissions);
        log.info("初始化菜单翻译结束============================================》");
    }

    private void initializeEnglish(List<SysPermission> sysPermissions){
        Map<String, String> englishNames = menuTranslationFile.getEnglishNames();
        if (LoginUtil.checkNull(englishNames) || englishNames.size() == CommonConst.NUMBER_0){
            return;
        }
        sysPermissions.stream().forEach(sysPermission -> {
            try {
                if ((LoginUtil.checkNull(sysPermission.getEnglishName()) && !LoginUtil.checkNull(sysPermission.getName())) ||
                    (!LoginUtil.checkNull(sysPermission.getName()) && !englishNames.get(sysPermission.getName()).equals(sysPermission.getEnglishName()))){
                    sysPermission.setEnglishName(englishNames.get(sysPermission.getName()));
                    sysPermissionService.save(sysPermission);
                }
            }catch (Exception ex){
                log.warn("翻译英文菜单异常====>菜单名称:{}",sysPermission.getName());
            }
        });
    }
    private void initializeCambodian(List<SysPermission> sysPermissions){
        Map<String, String> cambodianNames = menuTranslationFile.getCambodianNames();
        if (LoginUtil.checkNull(cambodianNames) || cambodianNames.size() == CommonConst.NUMBER_0){
            return;
        }
        sysPermissions.stream().forEach(sysPermission -> {

            try {
                if ((LoginUtil.checkNull(sysPermission.getCambodianName()) && !LoginUtil.checkNull(sysPermission.getName())) ||
                    (!LoginUtil.checkNull(sysPermission.getName()) && !cambodianNames.get(sysPermission.getName()).equals(sysPermission.getCambodianName()))){
                    sysPermission.setCambodianName(cambodianNames.get(sysPermission.getName()));
                    sysPermissionService.save(sysPermission);
                }
            }catch (Exception ex){
                log.warn("翻译柬埔寨语菜单异常====>菜单名称:{}",sysPermission.getName());
            }
        });
    }
}

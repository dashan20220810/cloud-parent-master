package com.qianyi.casinoadmin.install.file;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SysPermissionConfigFile {

    @Autowired
    private SysPermissionService sysPermissionService;

    public List<SysPermission> getPermissionConfig() {
        List<SysPermission> all = sysPermissionService.findAll();
        if(!LoginUtil.checkNull(all)){
            return null;
        }
        List<SysPermission> sysPermissionList = new ArrayList<>();
        //一级菜单栏位
        SysPermission memberPermission = new SysPermission("会员中心", "会员相关数据", "/memberCenter", 0l, 1, 0);
        SysPermission agentPermission = new SysPermission("代理中心", "代理后台相关数据", "/agentCenter", 0l, 1, 0);
        SysPermission orderPermission = new SysPermission("订单中心", "游戏注单相关数据", "/orderCenter", 0l, 1, 0);
        SysPermission financePermission = new SysPermission("财务中心", "财务数据", "/financeCenter", 0l, 1, 0);
        SysPermission operatePermission = new SysPermission("运营中心", "运营相关数据", "/operateCenter", 0l, 1, 0);
        SysPermission reportPermission = new SysPermission("报表管理", "报表统计相关数据", "/reportCenter", 0l, 1, 0);
        SysPermission systemPermission = new SysPermission("系统管理", "系统管理相关数据", "/systemCenter", 0l, 1, 0);
        sysPermissionList.add(memberPermission);
        sysPermissionList.add(agentPermission);
        sysPermissionList.add(orderPermission);
        sysPermissionList.add(financePermission);
        sysPermissionList.add(operatePermission);
        sysPermissionList.add(reportPermission);
        sysPermissionList.add(systemPermission);
        sysPermissionService.saveAllList(sysPermissionList);

        List<SysPermission> secordPermissions = new ArrayList<>();
        //会员中心菜单下
        SysPermission memberTranslate = new SysPermission("会员管理", "会员查询列表", "/user/findUserList", memberPermission.getId(), 2, 0);
        SysPermission memberCheck = new SysPermission("会员反查", "会员ip和地址查询", "/userPegging/findUserPegging", memberPermission.getId(), 2, 0);
        SysPermission thridMemberTranslate = new SysPermission("三方账户查询", "三方账户查询", "/userThird/findUserThird", memberPermission.getId(), 2, 0);
        secordPermissions.add(memberTranslate);
        secordPermissions.add(memberCheck);
        secordPermissions.add(thridMemberTranslate);

        //代理中心下菜单
        SysPermission agentSystem = new SysPermission("代理管理", "代理后台查询", "/proxyUser/findProxyUser", agentPermission.getId(), 2, 0);
        SysPermission agentReport = new SysPermission("综合报表", "代理后台日报表", "/companyProxyDetail/find", agentPermission.getId(), 2, 0);
        SysPermission agentMonthReport = new SysPermission("佣金月结报表", "代理后台月报表", "/companyProxyMonth/find", agentPermission.getId(), 2, 0);
        secordPermissions.add(agentSystem);
        secordPermissions.add(agentReport);
        secordPermissions.add(agentMonthReport);

        //订单中心
        SysPermission chargeOrder = new SysPermission("充值记录", "充值记录", "/chargeOrder/chargeOrderList", orderPermission.getId(), 2, 0);
        SysPermission withdrawalOrder = new SysPermission("提现记录", "提现记录", "/withdraw/withdrawList", orderPermission.getId(), 2, 0);
        secordPermissions.add(chargeOrder);
        secordPermissions.add(withdrawalOrder);

        SysPermission bankChannel = new SysPermission("银行渠道设置", "银行渠道设置", "/bankcard/banklist", financePermission.getId(), 2, 0);
        SysPermission collectionSet = new SysPermission("收款银行卡设置", "收款银行卡设置", "/collection/bankList", financePermission.getId(), 2, 0);
        secordPermissions.add(bankChannel);
        secordPermissions.add(collectionSet);

        SysPermission platformConfig = new SysPermission("平台配置", "平台配置", "/chargeConfig/findChargeConfig", operatePermission.getId(), 2, 0);
        SysPermission agentPromotion = new SysPermission("代理推广返佣配置", "代理推广返佣配置", "/proxyRebate/findAll", operatePermission.getId(), 2, 0);
        SysPermission pcBanner = new SysPermission("PC端轮播图配置", "PC端轮播图配置", "/picture/findByBannerList", operatePermission.getId(), 2, 0);
        SysPermission mobileBanner = new SysPermission("移动端轮播图配置", "移动端轮播图配置", "/picture/findByBannerListYD", operatePermission.getId(), 2, 0);
        SysPermission noticeConfig = new SysPermission("公告消息配置", "公告消息配置", "/notice/findNotice", operatePermission.getId(), 2, 0);
        SysPermission gameConfig = new SysPermission("游戏洗码配置", "游戏洗码配置", "/washCodeConfig/findAll", operatePermission.getId(), 2, 0);
        SysPermission customerConfig = new SysPermission("客服中心配置", "客服中心配置", "/customer/findCustomer", operatePermission.getId(), 2, 0);
        SysPermission IPConfig = new SysPermission("IP黑名单管理", "IP黑名单管理", "/ipBlack/findIpBlackPag", operatePermission.getId(), 2, 0);
        secordPermissions.add(platformConfig);
        secordPermissions.add(agentPromotion);
        secordPermissions.add(pcBanner);
        secordPermissions.add(mobileBanner);
        secordPermissions.add(noticeConfig);
        secordPermissions.add(gameConfig);
        secordPermissions.add(customerConfig);
        secordPermissions.add(IPConfig);

        SysPermission WMbanlance = new SysPermission("WM资金明细", "WM资金明细", "/order/findOrderList", reportPermission.getId(), 2, 0);
        SysPermission userCharge = new SysPermission("会员账变", "会员账变", "/accountChange/findAccountChangePage", reportPermission.getId(), 2, 0);
        SysPermission chargeLog = new SysPermission("充值订单流水", "充值订单流水", "/rechargeTurnover/findPage", reportPermission.getId(), 2, 0);
        SysPermission thridGameRecord = new SysPermission("第三方游戏注单", "第三方游戏注单", "/gameRecord/findGameRecordPage", reportPermission.getId(), 2, 0);
        SysPermission userLog = new SysPermission("用户登录日志", "用户登录日志", "/loginLog/findLoginLogPage", systemPermission.getId(), 2, 0);
        secordPermissions.add(WMbanlance);
        secordPermissions.add(userCharge);
        secordPermissions.add(chargeLog);
        secordPermissions.add(thridGameRecord);
        secordPermissions.add(userLog);

        SysPermission APPMeange = new SysPermission("APP升级管理", "APP升级管理", "/download/findDownloadStationPage", systemPermission.getId(), 2, 0);
        SysPermission sysUserList = new SysPermission("用户列表", "用户列表", "/sysUser/userList", systemPermission.getId(), 2, 0);
        SysPermission roleUserList = new SysPermission("角色列表", "角色列表", "/sysUser/roleList", systemPermission.getId(), 2, 0);
        secordPermissions.add(APPMeange);
        secordPermissions.add(sysUserList);
        secordPermissions.add(roleUserList);
        sysPermissionService.saveAllList(secordPermissions);

        //会员查询下菜单
        List<SysPermission> thridPermissions = new ArrayList<>();
        SysPermission saveCharge = new SysPermission("上分", "上分", "/user/saveChargeOrder", memberTranslate.getId(), 3, 0);
        SysPermission withdrawOrder = new SysPermission("下分", "下分", "/user/saveWithdrawOrder", memberTranslate.getId(), 3, 0);
        SysPermission resetPassword = new SysPermission("充值登录密码", "充值登录密码", "/user/resetPassword", memberTranslate.getId(), 3, 0);
        SysPermission withdrawPassword = new SysPermission("重置提现密码", "重置提现密码", "/user/withdrawPassword", memberTranslate.getId(), 3, 0);
        SysPermission saveUser = new SysPermission("新增客户", "新增用户", "/user/saveUser", memberTranslate.getId(), 3, 0);
        SysPermission refreshWM = new SysPermission("WM余额", "WM余额", "/user/refreshWM", memberTranslate.getId(), 3, 0);
        SysPermission updateUserStatus = new SysPermission("用户禁用/启用", "用户禁用/启用", "/user/updateUserStatus", memberTranslate.getId(), 3, 0);
        SysPermission updateUser = new SysPermission("编辑客户", "编辑客户", "/user/updateUser", memberTranslate.getId(), 3, 0);
        SysPermission boundList = new SysPermission("银行卡列表", "银行卡列表", "/bankcard/boundList", memberTranslate.getId(), 3, 0);
        SysPermission bound = new SysPermission("绑定银行卡", "绑定银行卡", "/bankcard/bound", memberTranslate.getId(), 3, 0);
        SysPermission userWashCodeList = new SysPermission("用户洗码查询", "用户洗码查询", "/UserWashCodeConfig/findAll", memberTranslate.getId(), 3, 0);
        SysPermission updateWashCodeConfigs = new SysPermission("编辑用户洗码设置", "编辑用户洗码设置", "/UserWashCodeConfig/updateWashCodeConfigs", memberTranslate.getId(), 3, 0);
        SysPermission findOrderList = new SysPermission("WM资金明细", "WM资金明细", "/order/findOrderList", memberTranslate.getId(), 3, 0);
        SysPermission findAccountChangePage = new SysPermission("会员账变", "会员账变", "/accountChange/findAccountChangePage", memberTranslate.getId(), 3, 0);
        SysPermission rechargeTurnover = new SysPermission("充值流水", "充值流水", "/rechargeTurnover/findPage", memberTranslate.getId(), 3, 0);
        SysPermission findCodeNumChangeList = new SysPermission("会员打码量", "会员打码量", "/codeNumChange/findCodeNumChangeList", memberTranslate.getId(), 3, 0);
        SysPermission findIp = new SysPermission("IP地址", "IP地址查询", "/user/findIp", memberTranslate.getId(), 3, 0);
        thridPermissions.add(saveCharge);
        thridPermissions.add(withdrawOrder);
        thridPermissions.add(resetPassword);
        thridPermissions.add(withdrawPassword);
        thridPermissions.add(saveUser);
        thridPermissions.add(refreshWM);
        thridPermissions.add(updateUserStatus);
        thridPermissions.add(updateUser);
        thridPermissions.add(boundList);
        thridPermissions.add(bound);
        thridPermissions.add(userWashCodeList);
        thridPermissions.add(updateWashCodeConfigs);
        thridPermissions.add(findOrderList);
        thridPermissions.add(findAccountChangePage);
        thridPermissions.add(rechargeTurnover);
        thridPermissions.add(findCodeNumChangeList);
        thridPermissions.add(findIp);

        //代理管理下菜单
        SysPermission resetPasswords = new SysPermission("重置密码", "重置密码", "/proxyUser/resetPasswords", agentSystem.getId(), 3, 0);
        SysPermission saveProxyUser = new SysPermission("新增代理", "新增代理", "/proxyUser/saveProxyUser", agentSystem.getId(), 3, 0);
        SysPermission updateUserFlag = new SysPermission("锁定", "锁定", "/proxyUser/updateUserFlag", agentSystem.getId(), 3, 0);
        SysPermission proxyRebateConfig = new SysPermission("全局返佣配置", "全局返佣配置", "/proxyRebateConfig/findAll", agentSystem.getId(), 3, 0);
        SysPermission findUsersNum = new SysPermission("直属下级玩家查询", "直属下级玩家查询", "/proxyUser/findUsersNum", agentSystem.getId(), 3, 0);
        SysPermission updateProxyRebate = new SysPermission("个人返佣设置", "个人返佣设置", "/proxyRebateConfig/updateProxyRebate", agentSystem.getId(), 3, 0);
        thridPermissions.add(resetPasswords);
        thridPermissions.add(saveProxyUser);
        thridPermissions.add(updateUserFlag);
        thridPermissions.add(proxyRebateConfig);
        thridPermissions.add(findUsersNum);
        thridPermissions.add(updateProxyRebate);

        SysPermission findSum = new SysPermission("代理日报表统计", "代理报表统计", "/companyProxyDetail/findSum", agentReport.getId(), 3, 0);
        SysPermission companyProxyDetail = new SysPermission("代理月报表统计", "代理月报表统计", "/companyProxyDetail/findSum", agentMonthReport.getId(), 3, 0);
        SysPermission findDailyDetails = new SysPermission("每日结算细节", "每日结算细节", "/companyProxyDetail/findDailyDetails", agentReport.getId(), 3, 0);
        SysPermission findDailyDetailsSum = new SysPermission("统计每日结算细节", "统计每日结算细节", "/companyProxyDetail/findDailyDetailsSum", agentReport.getId(), 3, 0);
        thridPermissions.add(findSum);
        thridPermissions.add(companyProxyDetail);
        thridPermissions.add(findDailyDetails);
        thridPermissions.add(findDailyDetailsSum);

        SysPermission updateChargeOrder = new SysPermission("充值审核", "充值审核", "/chargeOrder/updateChargeOrder", chargeOrder.getId(), 3, 0);
        SysPermission saveWithdraw = new SysPermission("提现审核", "提现审核", "/withdraw/saveWithdraw", withdrawalOrder.getId(), 3, 0);
        thridPermissions.add(updateChargeOrder);
        thridPermissions.add(saveWithdraw);

        SysPermission updateBankStatus = new SysPermission("禁用/启用", "禁用/启用", "/bankcard/updateBankStatus", bankChannel.getId(), 3, 0);
        SysPermission updateBankInfo = new SysPermission("编辑", "编辑", "/bankcard/updateBankInfo", bankChannel.getId(), 3, 0);
        thridPermissions.add(updateBankStatus);
        thridPermissions.add(updateBankInfo);

        SysPermission banklist = new SysPermission("银行渠道列表", "银行渠道列表", "/bankcard/banklist", collectionSet.getId(), 3, 0);
        SysPermission updateDisable = new SysPermission("上架", "上架", "/collection/updateDisable", collectionSet.getId(), 3, 0);
        SysPermission deleteBankInfo = new SysPermission("删除", "删除", "/collection/deleteBankInfo", collectionSet.getId(), 3, 0);
        SysPermission saveBankInfo = new SysPermission("新增", "新增", "/collection/saveBankInfo", collectionSet.getId(), 3, 0);
        thridPermissions.add(banklist);
        thridPermissions.add(updateDisable);
        thridPermissions.add(deleteBankInfo);
        thridPermissions.add(saveBankInfo);

        SysPermission findWithdrawConfig = new SysPermission("提款配置查询", "提款配置查询", "/withdrawConfig/findWithdrawConfig", platformConfig.getId(), 3, 0);
        SysPermission saveChargeConfig = new SysPermission("充值配置修改", "充值配置修改", "/chargeConfig/saveChargeConfig", platformConfig.getId(), 3, 0);
        SysPermission saveWithdrawConfig = new SysPermission("提现配置修改", "提现配置修改", "/withdrawConfig/saveWithdrawConfig", platformConfig.getId(), 3, 0);
        SysPermission betRatioConfig = new SysPermission("打码倍率查询", "打码倍率查询", "/betRatioConfig/findAll", platformConfig.getId(), 3, 0);
        SysPermission betRatioConfigUpdate = new SysPermission("打码倍率编辑", "打码倍率编辑", "/betRatioConfig/update", platformConfig.getId(), 3, 0);
        SysPermission findCommission = new SysPermission("推广配置查询", "推广配置查询", "/platformConfig/findCommission", platformConfig.getId(), 3, 0);
        SysPermission updateCommission = new SysPermission("推广配置编辑", "推广配置编辑", "/platformConfig/updateCommission", platformConfig.getId(), 3, 0);
        SysPermission findDomainName = new SysPermission("域名配置查询", "域名配置查询", "/platformConfig/findDomainName", platformConfig.getId(), 3, 0);
        SysPermission updateDomainName = new SysPermission("域名配置编辑", "域名配置编辑", "/platformConfig/updateDomainName", platformConfig.getId(), 3, 0);
        SysPermission findRegisterSwitch = new SysPermission("注册开关查询", "注册开关查询", "/platformConfig/findRegisterSwitch", platformConfig.getId(), 3, 0);
        SysPermission updateRegisterSwitch = new SysPermission("注册开关编辑", "注册开关编辑", "/platformConfig/updateRegisterSwitch", platformConfig.getId(), 3, 0);
        SysPermission findRiskConfig = new SysPermission("平台风险配置", "平台风险配置", "/managementRisk/findRiskConfig", platformConfig.getId(), 3, 0);
        SysPermission saveRiskConfig = new SysPermission("平台风险编辑", "平台风险编辑", "/managementRisk/saveRiskConfig", platformConfig.getId(), 3, 0);

        SysPermission findLogoPicture = new SysPermission("logo图查询(PC)", "logo图查询(PC)", "/platformConfig/findLogoPicturePc", platformConfig.getId(), 3, 0);
        SysPermission saveLogoPicture = new SysPermission("编辑logo图(PC)", "编辑logo图(PC)", "/platformConfig/saveLogoPicturePc", platformConfig.getId(), 3, 0);
        SysPermission findWebsiteIcon = new SysPermission("网站icon查看", "网站icon查看", "/platformConfig/findWebsiteIcon", platformConfig.getId(), 3, 0);
        SysPermission findMoneySymbol = new SysPermission("金钱符号查询", "金钱符号查询", "/platformConfig/findMoneySymbol", platformConfig.getId(), 3, 0);
        SysPermission saveWebsiteIconl = new SysPermission("编辑网站icon", "编辑网站icon", "/platformConfig/saveWebsiteIcon", platformConfig.getId(), 3, 0);
        SysPermission updateMoneySymbol = new SysPermission("编辑金钱符号", "编辑金钱符号", "/platformConfig/updateMoneySymbol", platformConfig.getId(), 3, 0);
        SysPermission findWebConfiguration = new SysPermission("logo图查询", "logo图查询", "/platformConfig/findLogoPictureApp", platformConfig.getId(), 3, 0);
        SysPermission updateWebConfiguration = new SysPermission("编辑logo图", "编辑logo图", "/platformConfig/savePCPictureApp", platformConfig.getId(), 3, 0);
        thridPermissions.add(findWithdrawConfig);
        thridPermissions.add(saveChargeConfig);
        thridPermissions.add(saveWithdrawConfig);
        thridPermissions.add(betRatioConfig);
        thridPermissions.add(betRatioConfigUpdate);
        thridPermissions.add(findCommission);
        thridPermissions.add(updateCommission);
        thridPermissions.add(findDomainName);
        thridPermissions.add(updateDomainName);
        thridPermissions.add(findRegisterSwitch);
        thridPermissions.add(updateRegisterSwitch);
        thridPermissions.add(findRiskConfig);
        thridPermissions.add(saveRiskConfig);

        thridPermissions.add(findLogoPicture);
        thridPermissions.add(saveLogoPicture);
        thridPermissions.add(findWebsiteIcon);
        thridPermissions.add(findMoneySymbol);
        thridPermissions.add(saveWebsiteIconl);
        thridPermissions.add(updateMoneySymbol);
        thridPermissions.add(findWebConfiguration);
        thridPermissions.add(updateWebConfiguration);

        SysPermission updateProxy = new SysPermission("保存", "保存", "/proxyRebate/updateProxyRebate", agentPromotion.getId(), 3, 0);
        thridPermissions.add(updateProxy);

        SysPermission washCodeConfigs = new SysPermission("编辑", "编辑", "/washCodeConfig/updateWashCodeConfigs", gameConfig.getId(), 3, 0);
        thridPermissions.add(washCodeConfigs);

        SysPermission savePCPicture = new SysPermission("编辑PC轮播图", "编辑轮播图", "/picture/savePCPicture", pcBanner.getId(), 3, 0);
        SysPermission savePicture = new SysPermission("编辑移动端轮播图", "编辑轮播图", "/picture/saveAppPicture", mobileBanner.getId(), 3, 0);
        thridPermissions.add(savePCPicture);
        thridPermissions.add(savePicture);

        SysPermission findByBanner = new SysPermission("轮播图查询", "移动端轮播图配置", "/picture/findByBannerList", mobileBanner.getId(), 2, 0);

        SysPermission deleteNotice = new SysPermission("上下架", "上下架", "/notice/deleteNotice", noticeConfig.getId(), 3, 0);
        SysPermission updateNotice = new SysPermission("编辑", "编辑", "/notice/updateNotice", noticeConfig.getId(), 3, 0);
        SysPermission saveNotice = new SysPermission("新增", "新增", "/notice/saveNotice", noticeConfig.getId(), 3, 0);
        thridPermissions.add(deleteNotice);
        thridPermissions.add(updateNotice);
        thridPermissions.add(saveNotice);
        thridPermissions.add(findByBanner);

        SysPermission updateKeyCustomer = new SysPermission("保存", "保存", "/customer/updateKeyCustomer", customerConfig.getId(), 3, 0);
        thridPermissions.add(updateKeyCustomer);

        SysPermission disable = new SysPermission("删除", "删除", "/ipBlack/disable", IPConfig.getId(), 3, 0);
        thridPermissions.add(disable);

        SysPermission saveDownloadStation = new SysPermission("保存", "保存", "/download/saveDownloadStation", APPMeange.getId(), 3, 0);
        thridPermissions.add(saveDownloadStation);

        SysPermission findPermissionList = new SysPermission("权限查询", "权限查询", "/role/findPermissionList", roleUserList.getId(), 3, 0);
        SysPermission updatePermissionList = new SysPermission("编辑权限", "编辑权限", "/role/updatePermissionList", roleUserList.getId(), 3, 0);
        SysPermission getRoleList = new SysPermission("查询角色数据", "查询角色数据", "/role/getRoleList", roleUserList.getId(), 3, 0);
        SysPermission getUserRoleBind = new SysPermission("绑定用户角色", "绑定用户角色", "/role/getUserRoleBind", sysUserList.getId(), 3, 0);
        SysPermission getSysUser = new SysPermission("查询用户数据", "查询用户数据", "/role/getSysUser", sysUserList.getId(), 3, 0);
        SysPermission deleteRoleList = new SysPermission("删除角色", "删除角色", "/role/deleteRoleList", sysUserList.getId(), 3, 0);
        thridPermissions.add(findPermissionList);
        thridPermissions.add(updatePermissionList);
        thridPermissions.add(getRoleList);
        thridPermissions.add(getUserRoleBind);
        thridPermissions.add(getSysUser);
        thridPermissions.add(deleteRoleList);

        List<SysPermission> thridPermissionsAdd1 = new ArrayList<>();
        SysPermission updateDirectly = new SysPermission("修改人人代直属下级最大个数", "修改人人代直属下级最大个数", "/platformConfig/updateDirectly", platformConfig.getId(), 3, 0);
        SysPermission findDirectly = new SysPermission("查询人人代直属下级最大个数", "查询人人代直属下级最大个数", "/platformConfig/findDirectly", platformConfig.getId(), 3, 0);
        SysPermission updateMessageBalance = new SysPermission("编辑短信余额风险警戒线", "编辑短信余额风险警戒线", "/platformConfig/updateMessageBalance", platformConfig.getId(), 3, 0);
        thridPermissionsAdd1.add(updateDirectly);
        thridPermissionsAdd1.add(findDirectly);
        thridPermissionsAdd1.add(updateMessageBalance);
        sysPermissionService.saveAllList(thridPermissionsAdd1);

        List<SysPermission> secordPermissionsAdd1 = new ArrayList<>();
        //代理中心下菜单
        SysPermission proxyReportFind = new SysPermission("查询人人代报表", "查询人人代报表", "/proxyReport/find", memberPermission.getId(), 2, 0);
        secordPermissionsAdd1.add(proxyReportFind);
        sysPermissionService.saveAllList(secordPermissionsAdd1);

        List<SysPermission> sysPermissions = new ArrayList<>();
        SysPermission findDetail = new SysPermission("人人代下级明细", "人人代下级明细", "/proxyReport/findDetail", proxyReportFind.getId(), 3, 0);
        SysPermission findDayDetail = new SysPermission("人人代每日结算细节", "人人代每日结算细节", "/proxyReport/findDayDetail", proxyReportFind.getId(), 3, 0);
        sysPermissions.add(findDetail);
        sysPermissions.add(findDayDetail);
        sysPermissionService.saveAllList(sysPermissions);


        sysPermissionService.saveAllList(thridPermissions);
        return null;
    }

    private void getTemp() {

    }

    /**
     * 新加接口权限脚本
     */
    public void addPermissionConfig() {
        List<SysPermission> sysPermissionList = sysPermissionService.findAll();
        SysPermission systemPermission = null; //系统管理
        SysPermission resetPassword = null;
        SysPermission resetGaKey = null;
        SysPermission disabeSysmission = null;
        SysPermission findUserSysmission = null; //会员管理

        SysPermission noticeConfig = null; //公告管理
        SysPermission deletenotice = null; //删除公告

        SysPermission platformConfig = null; //平台设置
        SysPermission findPromotionCode = null; //推广链接查询
        SysPermission updatePromotionCode = null; //修改推广链接码
        SysPermission wmBalancePermission = null; //一键回收用户WM余额
        SysPermission findUploadUrl = null; //图片服务器地址查询
        SysPermission updateUploadUrl = null; //修改图片服务器地址
        SysPermission findReadUploadUrl      = null; //修改图片服务器地址
        SysPermission updateReadUploadUrl = null; //修改图片服务器地址
        SysPermission memberMeonyLog = null; //会员资金流水
        SysPermission memberList = null; //用户列表
        SysPermission addSysUser = null; //新增用户
        SysPermission expand = null; //官方渠道
        SysPermission operatePermission = null; //运营中心
        SysPermission findLogoPictureApp = null; //logo图查询(APP登录注册页)
        SysPermission updateLogoPictureApp = null; //编辑logo图(APP登录注册页)
        SysPermission updateWebConfiguration = null; //修改Web网站域名配置
        SysPermission findLoginRegisterLogoPictureApp = null; //查询Web网站域名配置
        SysPermission updateCustomerCode = null; //修改客服脚本的代号
        SysPermission findCustomerCode = null; //查询客服脚本的代号

        for (SysPermission sysPermission : sysPermissionList) {
            if(sysPermission.getUrl().equals("/login/save")){
                addSysUser = sysPermission;
            }
            if(sysPermission.getUrl().equals("/sysUser/userList")){
                memberList = sysPermission;
            }
            if(sysPermission.getUrl().equals("/sysUser/userList")){
                systemPermission = sysPermission;
            }
            if(sysPermission.getUrl().equals("/user/findUserList")){
                findUserSysmission = sysPermission;
            }
            if(sysPermission.getUrl().equals("/login/resetPassword")){
                resetPassword = sysPermission;
            }
            if(sysPermission.getUrl().equals("/login/resetGaKey")){
                resetGaKey = sysPermission;
            }
            if(sysPermission.getName().equals("代理后台管理")){
                sysPermission.setName("代理管理");
                sysPermissionService.save(sysPermission);
            }
            if(sysPermission.getName().equals("代理后台日报表")){
                sysPermission.setName("综合报表");
                sysPermissionService.save(sysPermission);
            }
            if(sysPermission.getName().equals("代理后台月报表")){
                sysPermission.setName("佣金月结报表");
                sysPermissionService.save(sysPermission);
            }
            if(sysPermission.getUrl().equals("/bankcard/disable")){
                disabeSysmission = sysPermission;
            }
            if(sysPermission.getName().equals("平台配置")){
                platformConfig = sysPermission;
            }
            if(sysPermission.getName().equals("推广链接查询")){
                findPromotionCode = sysPermission;
            }
            if(sysPermission.getName().equals("运营中心")){
                operatePermission = sysPermission;
            }
            if(sysPermission.getName().equals("修改推广链接码")){
                updatePromotionCode = sysPermission;
            }
            if(sysPermission.getName().equals("官方推广")){
                expand = sysPermission;
            }
            if(sysPermission.getName().equals("一键回收用户WM余额")){
                wmBalancePermission = sysPermission;
            }
            if(sysPermission.getName().equals("图片服务器地址查询")){
                findUploadUrl = sysPermission;
            }
            if(sysPermission.getName().equals("修改图片服务器地址")){
                updateUploadUrl = sysPermission;
            }
            if(sysPermission.getName().equals("公告消息配置")){
                noticeConfig = sysPermission;
            }
            if(sysPermission.getName().equals("删除公告")){
                deletenotice = sysPermission;
            }
            if(sysPermission.getName().equals("访问图片地址配置查询")){
                findReadUploadUrl = sysPermission;
            }
            if(sysPermission.getName().equals("修改访问图片服务器地址")){
                updateReadUploadUrl = sysPermission;
            }
            if(sysPermission.getName().equals("查询会员流水报表")){
                memberMeonyLog = sysPermission;
            }
            if(sysPermission.getName().equals("logo图查询APP")){
                findLogoPictureApp = sysPermission;
            }
            if(sysPermission.getName().equals("编辑logo图APP")){
                updateLogoPictureApp = sysPermission;
            }
            if(sysPermission.getName().equals("修改Web网站域名")){
                updateWebConfiguration = sysPermission;
            }
            if(sysPermission.getName().equals("查询Web网站域名")){
                findLoginRegisterLogoPictureApp = sysPermission;
            }
            if(sysPermission.getName().equals("查询客服脚本的代号")){
                findCustomerCode = sysPermission;
            }
            if(sysPermission.getName().equals("修改客服脚本的代号")){
                updateCustomerCode = sysPermission;
            }
        }

        List<SysPermission> sysPermList = new ArrayList<>();
        if(systemPermission != null){
            if(resetPassword == null){
                resetPassword = new SysPermission("重置用户密码", "重置用户密码", "/login/resetPassword", systemPermission.getId(), 3, 0);
                sysPermList.add(resetPassword);
            }
            if(resetGaKey == null){
                resetGaKey = new SysPermission("重置谷歌验证码", "重置谷歌验证码", "/login/resetGaKey", systemPermission.getId(), 3, 0);
                sysPermList.add(resetGaKey);
            }
            if(memberMeonyLog == null){
                memberMeonyLog = new SysPermission("查询会员流水报表", "查询会员流水报表", "/userRunningWater/find", systemPermission.getId(), 3, 0);
                sysPermList.add(memberMeonyLog);
            }
        }
        if(findUserSysmission != null){
            if(disabeSysmission == null){
                disabeSysmission = new SysPermission("解绑", "解绑银行卡", "/bankcard/disable", findUserSysmission.getId(), 3, 0);
                sysPermissionService.save(disabeSysmission);
            }
            if(wmBalancePermission == null){
                wmBalancePermission = new SysPermission("一键回收用户WM余额", "一键回收用户WM余额", "/user/oneKeyRecover", findUserSysmission.getId(), 3, 0);
                sysPermissionService.save(wmBalancePermission);
            }
        }
        if(platformConfig != null){
            if(findPromotionCode == null){
                findPromotionCode = new SysPermission("推广链接查询", "推广链接查询", "/platformConfig/findPromotionCode", platformConfig.getId(), 3, 0);
                sysPermList.add(findPromotionCode);
            }
            if(updatePromotionCode == null){
                updatePromotionCode = new SysPermission("修改推广链接码", "修改推广链接码", "/platformConfig/updatePromotionCode", platformConfig.getId(), 3, 0);
                sysPermList.add(updatePromotionCode);
            }
            if(findLogoPictureApp == null){
                findLogoPictureApp = new SysPermission("logo图查询APP", "logo图查询APP", "/platformConfig/findLoginRegisterLogoPictureApp", platformConfig.getId(), 3, 0);
                sysPermList.add(findLogoPictureApp);
            }
            if(updateLogoPictureApp == null){
                updateLogoPictureApp = new SysPermission("编辑logo图APP", "编辑logo图APP", "/platformConfig/saveLoginRegisterLogoPictureApp", platformConfig.getId(), 3, 0);
                sysPermList.add(updateLogoPictureApp);
            }

            if(updateWebConfiguration == null){
                updateWebConfiguration = new SysPermission("修改Web网站域名", "修改Web网站域名", "/platformConfig/updateWebConfiguration", platformConfig.getId(), 3, 0);
                sysPermList.add(updateWebConfiguration);
            }
            if(findLoginRegisterLogoPictureApp == null){
                findLoginRegisterLogoPictureApp = new SysPermission("查询Web网站域名", "查询Web网站域名", "/platformConfig/findWebConfiguration", platformConfig.getId(), 3, 0);
                sysPermList.add(findLoginRegisterLogoPictureApp);
            }
            if(findCustomerCode == null){
                findCustomerCode = new SysPermission("查询客服脚本的代号", "查询客服脚本的代号", "/platformConfig/findCustomerCode", platformConfig.getId(), 3, 0);
                sysPermList.add(findCustomerCode);
            }
            if(updateCustomerCode == null){
                updateCustomerCode = new SysPermission("修改客服脚本的代号", "修改客服脚本的代号", "/platformConfig/updateCustomerCode", platformConfig.getId(), 3, 0);
                sysPermList.add(updateCustomerCode);
            }

            if(findUploadUrl == null){
                findUploadUrl = new SysPermission("图片服务器地址查询", "图片服务器地址查询", "/platformConfig/findUploadUrl", platformConfig.getId(), 3, 0);
                sysPermList.add(findUploadUrl);
            }
            if(updateUploadUrl == null){
                updateUploadUrl = new SysPermission("修改图片服务器地址", "修改图片服务器地址", "/platformConfig/updateUploadUrl", platformConfig.getId(), 3, 0);
                sysPermList.add(updateUploadUrl);
            }
            if(findReadUploadUrl == null){
                findReadUploadUrl = new SysPermission("访问图片地址配置查询", "访问图片地址配置查询", "/platformConfig/findReadUploadUrl", platformConfig.getId(), 3, 0);
                sysPermList.add(findReadUploadUrl);
            }
            if(updateReadUploadUrl == null){
                updateReadUploadUrl = new SysPermission("修改访问图片服务器地址", "修改访问图片服务器地址", "/platformConfig/updateReadUploadUrl", platformConfig.getId(), 3, 0);
                sysPermList.add(updateReadUploadUrl);
            }
        }
        if(noticeConfig != null){
            if(deletenotice == null){
                updateUploadUrl = new SysPermission("删除公告", "删除公告", "/notice/delNotice", noticeConfig.getId(), 3, 0);
                sysPermList.add(updateUploadUrl);
            }
        }
        if(operatePermission != null){
            if(expand == null){
                expand = new SysPermission("官方推广", "官方推广", "/expand/getChainedAddress", operatePermission.getId(), 2, 0);
                sysPermList.add(expand);
            }
        }

        if(memberList != null){
            if(addSysUser == null){
                addSysUser = new SysPermission("新增用户", "新增用户", "/login/save", memberList.getId(), 3, 0);
                sysPermList.add(addSysUser);
            }
        }

        if(sysPermList.size() > 0){
            sysPermissionService.saveAllList(sysPermList);
        }

    }
}

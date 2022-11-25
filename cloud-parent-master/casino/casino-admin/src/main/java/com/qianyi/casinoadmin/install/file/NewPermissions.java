package com.qianyi.casinoadmin.install.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.qianyi.casinoadmin.model.dto.SysPermissionDTO;
import com.qianyi.casinoadmin.model.dto.SysPermissionDTONode;
import com.qianyi.casinoadmin.util.FileUtils;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.SysPermission;
import com.qianyi.casinocore.service.SysPermissionService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 新添加权限在此方法中写
 *
 */
@Configuration
@Slf4j
public class NewPermissions {

    @Autowired
    private SysPermissionService sysPermissionService;

    // 调试方法，无实际意义
    private String deserializeToRootJson(){
        List<SysPermission> sysPermissionList = sysPermissionService.findAll();
        List<SysPermissionDTONode> nodes = DTOUtil.toNodeTree(sysPermissionList, SysPermissionDTONode.class);
        return JSON.toJSONString(nodes);
    }

    /**
     * 初始化权限相关
     *
     * @author lance
     * @since 2022 -03-02 20:34:10
     */
    public void init() {
        List<SysPermission> sysPermissionList = sysPermissionService.findAll();

        Function<SysPermissionDTONode, String> nodeKeyFn = c-> c.getName() + ":" + c.getUrl() + ":" + c.getMenuLevel();
        Function<SysPermission, String> sysKeyFn = c-> c.getName() + ":" + c.getUrl() + ":" + c.getMenuLevel();

        List<SysPermissionDTONode> nodes = FileUtils.readJsonFileAndParse("/permission/root.json", SysPermissionDTONode.class);

        // 将树状结构展开，并且设置 子-父 对应关系
        Map<String, String> refs = new HashMap<>();
        List<SysPermissionDTONode> unwindList = DTOUtil.unwindRoot(nodes, nodeKeyFn , refs);

        // 待删除列表
        List<SysPermissionDTONode> dropList = unwindList.stream().filter(c-> c.getDelete() != null && c.getDelete()).collect(Collectors.toList());

        if (CollUtil.isNotEmpty(dropList)) {
            sysPermissionList = dropFilter(dropList, sysPermissionList, nodeKeyFn, sysKeyFn);
            unwindList = unwindList.stream().filter(c -> c.getDelete() == null || !c.getDelete()).collect(Collectors.toList());
        }

        // 数据库 权限映射 { `name:url:menuLevel` : SysPermission}
        Map<String, SysPermission> sysMaps = getSysPermissionMap(sysPermissionList, sysKeyFn);

        // 待更新列表
        List<SysPermissionDTONode> updateList = unwindList.stream().filter(c -> c.getUpdate() != null).collect(Collectors.toList());

        if (CollUtil.isNotEmpty(updateList)) {
            handleUpdateList(updateList, nodeKeyFn, sysMaps);
            unwindList = unwindList.stream().filter(c -> c.getUpdate() == null).collect(Collectors.toList());
        }

        // 本地json文件 权限映射 { `name:url:menuLevel` : SysPermissionDTONode}
        Map<String, SysPermissionDTONode> localMaps = unwindList.stream().collect(
                Collectors.toMap(nodeKeyFn, c -> c)
        );

        // 遍历本地权限列表，已存在的权限设置id这样会导致jpa执行更新操作
        for (String key : localMaps.keySet()) {
            // 保存数据库中没有对应的url权限记录
            SysPermission sys = sysMaps.get(key);
            if (null != sys) {
                SysPermissionDTONode local = localMaps.get(key);
                local.setId(sys.getId());
                // 设置pid
                local.setPid(sys.getPid());
            }
        }

        // 保证每个子集都能获取到pid,因此需要按先后顺序去保存
        List<SysPermissionDTONode> roots = DTOUtil.toNodeTree(unwindList, refs, nodeKeyFn);

        //log.info("roots: {}", roots);
        // 级联保存
        deepSave(0L, roots);
    }

    // 更新操作特殊处理, 只有已经保存在数据库的才可以更新，否则忽略
    private void handleUpdateList(
            List<SysPermissionDTONode> updateList,
            Function<SysPermissionDTONode, String> nodeKeyFn,
            Map<String, SysPermission> sysMaps
    ) {
        List<SysPermission> sysUpdateList = new ArrayList<>();
        for (SysPermissionDTONode tobeUpdate: updateList) {
            String tobeKey = nodeKeyFn.apply(tobeUpdate);
            SysPermission has = sysMaps.get(tobeKey);
            if (null != has) {
                SysPermissionDTO update = tobeUpdate.getUpdate();

                if (StrUtil.isNotBlank(update.getName())) {
                    has.setName(update.getName());
                }

                if (StrUtil.isNotBlank(update.getEnglishName())) {
                    has.setEnglishName(update.getEnglishName());
                }

                if (StrUtil.isNotBlank(update.getCambodianName())) {
                    has.setCambodianName(update.getCambodianName());
                }

                if (StrUtil.isNotBlank(update.getDescritpion())) {
                    has.setDescritpion(update.getDescritpion());
                }

                if (StrUtil.isNotBlank(update.getUrl())) {
                    has.setUrl(update.getUrl());
                }

                if (update.getMenuLevel() != null) {
                    has.setMenuLevel(update.getMenuLevel());
                }

                sysUpdateList.add(has);
            }
        }
        if (CollUtil.isNotEmpty(sysUpdateList)) {
            sysPermissionService.saveAllList(sysUpdateList);
        }
    }

    // 删除
    private List<SysPermission> dropFilter(
            List<SysPermissionDTONode> dropList,
            List<SysPermission> sysPermissionList,
            Function<SysPermissionDTONode, String> nodeKeyFn,
            Function<SysPermission, String> sysKeyFn
    ){
        List<SysPermission> list = new ArrayList<>();
        Map<String, SysPermissionDTONode> dropMaps = dropList.stream().collect(
                Collectors.toMap(nodeKeyFn, c -> c)
        );

        List<Long> deleteIdList = new ArrayList<>();

        for (SysPermission sys: sysPermissionList) {
            String sysKey = sysKeyFn.apply(sys);
            if (null != dropMaps.get(sysKey)) {
                deleteIdList.add(sys.getId());
            } else {
                list.add(sys);
            }
        }

        if (CollUtil.isNotEmpty(deleteIdList)) {
            sysPermissionService.deleteAllIds(deleteIdList);
        }

        return list;
    }

    // 获取数据库 权限映射
    private Map<String, SysPermission> getSysPermissionMap(List<SysPermission> sysPermissionList, Function<SysPermission, String> sysKeyFn){
        // ！由于历史原因，数据库中存在相同的key, 在做映射之前需要先将重复的数据过滤掉

        Map<String, List<SysPermission>> sysGroups = sysPermissionList.stream().collect(
                Collectors.groupingBy(sysKeyFn)
        );

        // 数据库 权限映射 { `name:url:menuLevel` : SysPermission}
        Map<String, SysPermission> sysMaps = new HashMap<>();

        List<SysPermission> sysUpdateList = new ArrayList<>();

        // 以key分组，得到长度不为1的都是重复的数据
        for (Map.Entry<String, List<SysPermission>> entry: sysGroups.entrySet()) {
            List<SysPermission> list = entry.getValue();
            if (list.size() == 1) {
                String key = entry.getKey();
                SysPermission value = list.get(0);
                sysMaps.put(key, value);
            } else {
                // 重复的主键, 自动更改名称
                for (int i=0; i< list.size(); ++i) {
                    SysPermission tobeUpdate = list.get(i);
                    String oldName = tobeUpdate.getName();
                    tobeUpdate.setName(oldName + (i + 1));
                    sysUpdateList.add(tobeUpdate);
                }

            }
        }

        if (CollUtil.isNotEmpty(sysUpdateList)) {
            sysPermissionService.saveAllList(sysUpdateList);
        }

        return sysMaps;
    }

    // 递归保存(无法保证事务)
    private void deepSave(Long pid, List<SysPermissionDTONode> roots) {
        for (SysPermissionDTONode node : roots) {
            SysPermission sysPermission = DTOUtil.toDTO(node, SysPermission.class);

            if (sysPermission.getId() == null) {
                if (null == node.getPid()) {
                    sysPermission.setPid(pid);
                }
                sysPermission.setIsDetele(0);
                sysPermission.setCreateBy("system");
                SysPermission save = sysPermissionService.save(sysPermission);
                if (CollUtil.isNotEmpty(node.getChildren())) {
                    deepSave(save.getId(), node.getChildren());
                }
            } else {
                if (CollUtil.isNotEmpty(node.getChildren())) {
                    deepSave(sysPermission.getId(), node.getChildren());
                }
            }
        }
    }

    public void addNewPermission() {
        List<SysPermission> sysPermissionList = sysPermissionService.findAll();
        if(LoginUtil.checkNull(sysPermissionList)){
            return;
        }
        sysPermissionList = removeSysPermission(sysPermissionList);
        List<SysPermission> sysPermissions = new ArrayList<>();

        Map<String, SysPermission> collect = sysPermissionList.stream().collect(Collectors.toMap(SysPermission::getUrl, sysPermission -> sysPermission));
        if(collect.containsKey("/systemCenter")){
            //设置平台配置
            setSystemConfig(collect);
        }
        if(collect.containsKey("/operateCenter")){
            //设置运营客服中心配置
            setCustomerConfigure(collect);
        }
        if(collect.containsKey("/agentCenter")){
            //设置代理中心
            setAgentCenter(collect);
        }
        if(collect.containsKey("/orderCenter")){
            //设置订单中心
            setOrderCenter(collect);
        }
        if(collect.containsKey("/reportCenter")){
            //报表管理
            setReportCenter(collect);
        }
        if(collect.containsKey("/memberCenter")){
            //会员中心
            setMemberCenter(collect);
        }
        if(!collect.containsKey("/thirdGameMange")){
            //第三方游戏管理
            thirdGameMange();
        }
        if(collect.containsKey("/order/findOrderList")){
            SysPermission sysPermission = collect.get("/order/findOrderList");
            if(sysPermission.getName().equals("WM资金明细")){
                sysPermission.setName("第三方资金明细");
                sysPermission.setDescritpion("第三方资金明细");
                sysPermissionService.save(sysPermission);
            }
        }
        if(!collect.containsKey("/domain/findList")){
            //域名管理
            domainConfig(collect);
        }
        sysPermissionService.saveAllList(sysPermissions);
    }

    private void domainConfig(Map<String, SysPermission> collect) {
        Long pid = collect.get("/systemCenter").getId();
        if (!collect.containsKey("/domain/findList")) {
            SysPermission domainPlatform = new SysPermission("域名管理", "域名管理", "/domain/findList", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
            sysPermissionService.save(domainPlatform);
            List<SysPermission> sysPermissionList = new ArrayList<>();
            SysPermission savePlatform = new SysPermission("新增或者修改域名", "新增或者修改域名", "/domain/saveDomain", domainPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
            SysPermission updatePlatform = new SysPermission("修改域名状态", "修改域名状态", "/domain/domainStatus", domainPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
            SysPermission deletePlatform = new SysPermission("删除域名", "删除域名", "/domain/deleteId", domainPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
            SysPermission visitsPlatform = new SysPermission("域名访问量统计", "域名访问量统计", "/domain/visitsFindList", domainPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
            sysPermissionList.add(savePlatform);
            sysPermissionList.add(updatePlatform);
            sysPermissionList.add(deletePlatform);
            sysPermissionList.add(visitsPlatform);
            sysPermissionService.saveAllList(sysPermissionList);
        }

    }

    private void thirdGameMange() {
        SysPermission sysPermission = new SysPermission("第三方游戏管理", "第三方游戏管理", "/thirdGameMange", 0l, CommonConst.NUMBER_1, CommonConst.NUMBER_0);
        sysPermissionService.save(sysPermission);
        List<SysPermission> sysPermissions = new ArrayList<>();
        SysPermission pgPlatform = new SysPermission("PG电子游戏管理", "PG电子游戏管理", "/adGame/platformListPG", sysPermission.getId(), CommonConst.NUMBER_2, CommonConst.NUMBER_0);
        SysPermission cq9Permission = new SysPermission("CQ9电子游戏管理", "CQ9电子游戏管理", "/adGame/platformListCQ9", sysPermission.getId(), CommonConst.NUMBER_2, CommonConst.NUMBER_0);
        SysPermission wmPermission = new SysPermission("WM电子游戏管理", "WM电子游戏管理", "/adGame/platformListWM", sysPermission.getId(), CommonConst.NUMBER_2, CommonConst.NUMBER_0);
        sysPermissions.add(pgPlatform);
        sysPermissions.add(cq9Permission);
        sysPermissions.add(wmPermission);
        sysPermissionService.saveAllList(sysPermissions);
        List<SysPermission> pgPermissions = new ArrayList<>();
        List<SysPermission> cq9Permissions = new ArrayList<>();
        List<SysPermission> wmPermissions = new ArrayList<>();
        SysPermission pgAdgamePlatform = new SysPermission("游戏列表", "游戏列表", "/adGame/findGameList", pgPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission pgPlatformUpdate = new SysPermission("平台是否维护", "平台是否维护", "/adGame/updatePlatformStatus", pgPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission pgAdGameUpdate = new SysPermission("游戏状态修改", "游戏状态修改", "/adGame/updateDomainName", pgPlatform.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        pgPermissions.add(pgAdgamePlatform);
        pgPermissions.add(pgPlatformUpdate);
        pgPermissions.add(pgAdGameUpdate);
        sysPermissionService.saveAllList(pgPermissions);
        SysPermission cq9AdgamePlatform = new SysPermission("游戏列表", "游戏列表", "/adGame/findGameList", cq9Permission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission cq9PlatformUpdate = new SysPermission("平台是否维护", "平台是否维护", "/adGame/updatePlatformStatus", cq9Permission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission cq9AdGameUpdate = new SysPermission("游戏状态修改", "游戏状态修改", "/adGame/updateDomainName", cq9Permission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);

        cq9Permissions.add(cq9AdgamePlatform);
        cq9Permissions.add(cq9PlatformUpdate);
        cq9Permissions.add(cq9AdGameUpdate);
        sysPermissionService.saveAllList(cq9Permissions);

        SysPermission wm9AdgamePlatform = new SysPermission("游戏列表", "游戏列表", "/adGame/findGameList", wmPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission wm9PlatformUpdate = new SysPermission("平台是否维护", "平台是否维护", "/adGame/updatePlatformStatus", wmPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
        SysPermission wm9AdGameUpdate = new SysPermission("游戏状态修改", "游戏状态修改", "/adGame/updateDomainName", wmPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);

        wmPermissions.add(wm9AdgamePlatform);
        wmPermissions.add(wm9PlatformUpdate);
        wmPermissions.add(wm9AdGameUpdate);
        sysPermissionService.saveAllList(wmPermissions);
    }

    private static ArrayList<SysPermission> removeSysPermission( List<SysPermission> sysPermissionList){
        Set<SysPermission> set = new TreeSet<SysPermission>(new Comparator<SysPermission>() {
            @Override
            public int compare(SysPermission o1, SysPermission o2) {
                return o1.getUrl().compareTo(o2.getUrl());
            }
        });
        set.addAll(sysPermissionList);
        return new ArrayList<>(set);
    }

    private void setMemberCenter(Map<String, SysPermission> collect){
        if (collect.containsKey("/user/findUserList")) {
            Long pid = collect.get("/user/findUserList").getId();
            if (!collect.containsKey("/user/refreshPGAndCQ9")) {
                SysPermission sysPermission = new SysPermission("查询用户PG/CQ9余额", "查询用户PG/CQ9余额", "/user/refreshPGAndCQ9", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/user/oneKeyRecoverApi")) {
                SysPermission sysPermission = new SysPermission("一键回收用户PG/CQ9余额", "一键回收用户PG/CQ9余额", "/user/oneKeyRecoverApi", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/user/getWMMoneyTotal")) {
                SysPermission sysPermission = new SysPermission("请求玩家再WM余额总余额", "请求玩家再WM余额总余额", "/user/getWMMoneyTotal", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/user/refreshPGTotal")) {
                SysPermission sysPermission = new SysPermission("查询玩家PG/CQ9总余额", "查询玩家PG/CQ9总余额", "/user/refreshPGTotal", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/user/findUserTotal")) {
                SysPermission sysPermission = new SysPermission("当前查询结果总计", "当前查询结果总计", "/user/findUserTotal", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/bankcard/unboundBankName")) {
                SysPermission sysPermission = new SysPermission("解除银行卡实名认证", "解除银行卡实名认证", "/bankcard/unboundBankName", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/extractPointsConfig/user/findAll")) {
                SysPermission sysPermission = new SysPermission("用户抽点配置表查询", "用户抽点配置表查询", "/extractPointsConfig/user/findAll", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/extractPointsConfig/user/update")) {
                SysPermission sysPermission = new SysPermission("更新用户抽点配置", "更新用户抽点配置", "/extractPointsConfig/user/update", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }
    /**
     * 报表管理
     * @param collect
     */
    private void setReportCenter(Map<String, SysPermission> collect) {
        if (collect.containsKey("/reportCenter")) {
            Long pid = collect.get("/reportCenter").getId();
            if (!collect.containsKey("/report/queryPersonReport")) {
                SysPermission sysPermission = new SysPermission("历史盈亏报表", "历史盈亏报表", "/report/queryPersonReport", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
                if(sysPermission.getId() != null){
                    SysPermission sysPermission1 = new SysPermission("盈亏报表总计", "盈亏报表总计", "/report/queryTotal", sysPermission.getId(), CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                    sysPermissionService.save(sysPermission1);
                }
            }
            if (!collect.containsKey("/inTimeReport/find")) {
                SysPermission sysPermission = new SysPermission("代理对账报表", "代理对账报表", "/inTimeReport/find", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/companyReport/find")) {
                SysPermission sysPermission = new SysPermission("普通会员报表", "普通会员报表", "/companyReport/find", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }


    /**
     * 设置订单中心
     * @param collect
     */
    private void setOrderCenter(Map<String, SysPermission> collect) {
        if (collect.containsKey("/chargeOrder/chargeOrderList")) {
            Long pid = collect.get("/chargeOrder/chargeOrderList").getId();
            if (!collect.containsKey("/chargeOrder/updateChargeOrdersRemark")) {
                SysPermission sysPermission = new SysPermission("修改充值备注", "修改充值备注", "/chargeOrder/updateChargeOrdersRemark", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }

        }
        if (collect.containsKey("/withdraw/withdrawList")) {
            Long pid = collect.get("/withdraw/withdrawList").getId();
            if (!collect.containsKey("/withdraw/updateWithdrawOrderRemark")) {
                SysPermission sysPermission = new SysPermission("修改提现备注", "修改提现备注", "/withdraw/updateWithdrawOrderRemark", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }

        }
    }
    /**
     * 设置代理中心
     * @param collect
     */
    private void setAgentCenter(Map<String, SysPermission> collect) {
        if(collect.containsKey("/proxyUser/findProxyUser")){
            Long pid = collect.get("/proxyUser/findProxyUser").getId();
            if (!collect.containsKey("/proxyUser/transferUser")){
                SysPermission sysPermission = new SysPermission("转移会员", "转移会员", "/proxyUser/transferUser", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/proxyUser/transferProxy")){
                SysPermission sysPermission = new SysPermission("转移代理", "转移代理", "/proxyUser/transferProxy", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/proxyUser/delete")){
                SysPermission sysPermission = new SysPermission("删除", "删除", "/proxyUser/delete", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/extractPointsConfig/poxy/findAll")) {
                SysPermission sysPermission = new SysPermission("代理抽点配置表查询", "代理抽点配置表查询", "/extractPointsConfig/poxy/findAll", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/extractPointsConfig/poxy/update")) {
                SysPermission sysPermission = new SysPermission("更新基础代代理抽点配置", "更新基础代代理抽点配置", "/extractPointsConfig/poxy/update", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }

    /**
     * 运营中心 - 客服中心配置
     * @param collect
     */
    private void setCustomerConfigure(Map<String, SysPermission> collect) {
        SysPermission save =new SysPermission();
        if(!collect.containsKey("/customer/findCustomerList")){
            //先删除之前的菜单
            SysPermission byUrl = sysPermissionService.findByUrl("/customer/findCustomer");
            SysPermission updateByUrl = sysPermissionService.findByUrl("/customer/updateKeyCustomer");
            if (byUrl!=null){
                sysPermissionService.deleteById(byUrl.getId());
            }
            if (updateByUrl!=null){
                sysPermissionService.deleteById(updateByUrl.getId());
            }
            Long pid = collect.get("/operateCenter").getId();
            SysPermission sysConfigPermission = new SysPermission("客服中心配置", "客服中心配置", "/customer/findCustomerList", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
            save = sysPermissionService.save(sysConfigPermission);
        }
        if(save.getUrl()!=null && save.getUrl().equals("/customer/findCustomerList")){
            Long pid = save.getId();
            if(!collect.containsKey("/customer/updateKeyCustomerConfigure")){
                SysPermission sysPermission = new SysPermission("保存", "保存", "/customer/updateKeyCustomerConfigure", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }

        //人人贷开关
        if(collect.containsKey("/chargeConfig/findChargeConfig")){
            Long pid = collect.get("/chargeConfig/findChargeConfig").getId();
            if (!collect.containsKey("/platformConfig/findPeopleProxySwitch")){
                SysPermission sysPermission = new SysPermission("查询人人代开关", "查询人人代开关", "/platformConfig/findPeopleProxySwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/updatePeopleProxySwitch")){
                SysPermission sysPermission = new SysPermission("编辑人人代开关", "编辑人人代开关", "/platformConfig/updatePeopleProxySwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/findBankcardRealNameSwitch")){
                SysPermission sysPermission = new SysPermission("查询银行卡账号校验开关", "查询银行卡账号校验开关", "/platformConfig/findBankcardRealNameSwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/updateBankcardRealNameSwitch")){
                SysPermission sysPermission = new SysPermission("编辑银行卡账号校验开关", "编辑银行卡账号校验开关", "/platformConfig/updateBankcardRealNameSwitch", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/findPlatformMaintenance")){
                SysPermission sysPermission = new SysPermission("查询平台维护开关", "查询平台维护开关", "/platformConfig/findPlatformMaintenance", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if (!collect.containsKey("/platformConfig/updatePlatformMaintenance")){
                SysPermission sysPermission = new SysPermission("修改平台维护开关", "修改平台维护开关", "/platformConfig/updatePlatformMaintenance", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }

        // 代理抽点配置
        if (!collect.containsKey("/extractPointsConfig/findAll")) {
            Long pid = collect.get("/operateCenter").getId();
            SysPermission sysConfigPermission = new SysPermission("代理抽点配置", "代理抽点配置", "/extractPointsConfig/findAll", pid, CommonConst.NUMBER_2, CommonConst.NUMBER_0);
            save = sysPermissionService.save(sysConfigPermission);
            pid = save.getId();
            if (!collect.containsKey("/extractPointsConfig/update")) {
                // 更新默认的抽点配置
                SysPermission sysPermission = new SysPermission("更新默认的抽点配置", "更新默认的抽点配置", "/extractPointsConfig/update", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }
    }
    private void setSystemConfig(Map<String, SysPermission> collect) {
        if(!collect.containsKey("/systemMessage/systemConfig")){
            Long pid = collect.get("/systemCenter").getId();
            SysPermission sysConfigPermission = new SysPermission("系统配置", "系统配置", "/systemMessage/systemConfig", pid, 2, 0);
            sysPermissionService.save(sysConfigPermission);
            if(collect.containsKey("/platformConfig/updateDomainName")){
                SysPermission sysPermission = collect.get("/platformConfig/updateDomainName");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findDomainName")){
                SysPermission sysPermission = collect.get("/platformConfig/findDomainName");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/updateUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/findUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateReadUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/updateReadUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findReadUploadUrl")){
                SysPermission sysPermission = collect.get("/platformConfig/findReadUploadUrl");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateMoneySymbol")){
                SysPermission sysPermission = collect.get("/platformConfig/updateMoneySymbol");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findMoneySymbol")){
                SysPermission sysPermission = collect.get("/platformConfig/findMoneySymbol");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateWebConfiguration")){
                SysPermission sysPermission = collect.get("/platformConfig/updateWebConfiguration");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findWebConfiguration")){
                SysPermission sysPermission = collect.get("/platformConfig/findWebConfiguration");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/findCustomerCode")){
                SysPermission sysPermission = collect.get("/platformConfig/findCustomerCode");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateCustomerCode")){
                SysPermission sysPermission = collect.get("/platformConfig/updateCustomerCode");
                sysPermission.setPid(sysConfigPermission.getId());
                sysPermissionService.save(sysPermission);
            }
        }
        if(collect.containsKey("/systemMessage/systemConfig")){
            Long pid = collect.get("/systemMessage/systemConfig").getId();
            if(collect.containsKey("/platformConfig/findCustomerCode") && !collect.get("/platformConfig/findCustomerCode").getPid().toString().equals(pid.toString()) ){
                SysPermission sysPermission = collect.get("/platformConfig/findCustomerCode");
                sysPermission.setPid(pid);
                //                SysPermission sysPermission = new SysPermission("查询客服脚本的代号", "查询客服脚本的代号", "/platformConfig/findCustomerCode", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
            if(collect.containsKey("/platformConfig/updateCustomerCode") && !collect.get("/platformConfig/updateCustomerCode").getPid().toString().equals(pid.toString())){
                SysPermission sysPermission = collect.get("/platformConfig/updateCustomerCode");
                sysPermission.setPid(pid);
                //                SysPermission sysPermission = new SysPermission("修改客服脚本的代号", "修改客服脚本的代号", "/platformConfig/updateCustomerCode", pid, CommonConst.NUMBER_3, CommonConst.NUMBER_0);
                sysPermissionService.save(sysPermission);
            }
        }

    }
}

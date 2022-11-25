package com.qianyi.casinoweb.runner;

import com.qianyi.casinocore.model.PlatformGame;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.service.PlatformGameService;
import com.qianyi.casinocore.service.WashCodeConfigService;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 第一次启动项目初始化游戏洗码配置
 */
@Component
@Slf4j
@Order(1)
public class WashCodeConfigRunner implements CommandLineRunner {

    @Autowired
    private WashCodeConfigService washCodeConfigService;
    @Autowired
    private PlatformGameService platformGameService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化洗码配置");
        List<WashCodeConfig> list = washCodeConfigService.findAll();
        log.info("洗码配置查询结果data={}", list);
        if (CollectionUtils.isEmpty(list)) {
            List<WashCodeConfig> washCodeConfigs = initData();
            washCodeConfigService.saveAll(washCodeConfigs);
            return;
        }
        List<WashCodeConfig> washCodeConfigs = initData();
        for (WashCodeConfig codeConfig : list) {
            Iterator<WashCodeConfig> configIterator = washCodeConfigs.iterator();
            while (configIterator.hasNext()) {
                WashCodeConfig wash = configIterator.next();
                if (wash.getGameId().equals(codeConfig.getGameId())) {
                    configIterator.remove();
                }
            }
        }
        if (!CollectionUtils.isEmpty(washCodeConfigs)) {
            washCodeConfigService.saveAll(washCodeConfigs);
        }
        log.info("洗码配置初始化完成");
    }

    private List<WashCodeConfig> initData() {
        List<WashCodeConfig> list = new ArrayList<>();
        list.add(setData(Constants.PLATFORM_WM,"101", "百家乐", "Baccarat"));
        list.add(setData(Constants.PLATFORM_WM,"102", "龙虎", "Dragon Tiger"));
        list.add(setData(Constants.PLATFORM_WM,"103", "轮盘", "Roulette"));
        list.add(setData(Constants.PLATFORM_WM,"104", "骰宝", "SicBo"));
        list.add(setData(Constants.PLATFORM_WM,"105", "牛牛", "Niu Niu"));
        list.add(setData(Constants.PLATFORM_WM,"106", "三公", "Three Face"));
        list.add(setData(Constants.PLATFORM_WM,"107", "番摊", "Fantan"));
        list.add(setData(Constants.PLATFORM_WM,"108", "色碟", "Se Die"));
        list.add(setData(Constants.PLATFORM_WM,"110", "鱼虾蟹", "Fish-Prawn-Crab"));
        list.add(setData(Constants.PLATFORM_WM,"111", "炸金花", "Golden Flower"));
        list.add(setData(Constants.PLATFORM_WM,"112", "温州牌九", "Wenzhou Pai Gow"));
        list.add(setData(Constants.PLATFORM_WM,"113", "二八杠", "Mahjong tiles"));
        list.add(setData(Constants.PLATFORM_WM,"128", "安達巴哈", "AndarBahar"));
        list.add(setData(Constants.PLATFORM_PG,Constants.PLATFORM_PG, "PG", "PG"));
        list.add(setData(Constants.PLATFORM_CQ9,Constants.PLATFORM_CQ9, "CQ9", "CQ9"));
        list.add(setData(Constants.PLATFORM_OBDJ,Constants.PLATFORM_OBDJ, "OB电竞", "OB eSports"));
        list.add(setData(Constants.PLATFORM_OBTY,Constants.PLATFORM_OBTY, "OB体育", "OB Sports"));
        list.add(setData(Constants.PLATFORM_SABASPORT,Constants.PLATFORM_SABASPORT, "沙巴体育", "SABASPORT"));
        return list;
    }

    private WashCodeConfig setData(String platform,String gameId, String gameName, String gameEnName) {
        WashCodeConfig codeConfig = new WashCodeConfig();
        codeConfig.setPlatform(platform);
        codeConfig.setGameId(gameId);
        codeConfig.setGameName(gameName);
        codeConfig.setGameEnName(gameEnName);
        if (Constants.PLATFORM_OBDJ.equals(platform) || Constants.PLATFORM_OBTY.equals(platform)) {
            platform = Constants.PLATFORM_OB;
        }
        codeConfig.setState(Constants.open);
        PlatformGame platformGame = platformGameService.findByGamePlatformName(platform);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            codeConfig.setState(Constants.close);
        }
        return codeConfig;
    }
}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserWashCodeConfig;
import com.qianyi.casinocore.model.WashCodeConfig;
import com.qianyi.casinocore.repository.UserWashCodeConfigRepository;
import com.qianyi.casinocore.repository.WashCodeConfigRepository;
import com.qianyi.modulecommon.Constants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class UserWashCodeConfigService {

    @Autowired
    private UserWashCodeConfigRepository userWashCodeConfigRepository;
    @Autowired
    private WashCodeConfigService washCodeConfigService;

    public List<UserWashCodeConfig> saveAll(List<UserWashCodeConfig> list){
        return userWashCodeConfigRepository.saveAll(list);
    }

    public List<UserWashCodeConfig> findByUserIdAndPlatformAndState(Long userId, String platform,Integer state) {
        return userWashCodeConfigRepository.findByUserIdAndPlatformAndState(userId,platform,state);
    }

    public List<UserWashCodeConfig> findByUserId(Long userId) {
        return userWashCodeConfigRepository.findByUserId(userId);
    }

    public List<WashCodeConfig> getWashCodeConfig(String platform,Long userId) {
        //先查询用户级的洗码配置
        List<UserWashCodeConfig> codeConfigs = userWashCodeConfigRepository.findByUserIdAndPlatform(userId, platform);
        if (!CollectionUtils.isEmpty(codeConfigs)) {
            List<WashCodeConfig> list = new ArrayList<>();
            WashCodeConfig config = null;
            for (UserWashCodeConfig codeConfig : codeConfigs) {
                if (codeConfig.getState() != Constants.open) {
                    continue;
                }
                config = new WashCodeConfig();
                BeanUtils.copyProperties(codeConfig, config);
                list.add(config);
            }
            return list;
        }
        //先查询全局洗码配置
        List<WashCodeConfig> configs = washCodeConfigService.findByPlatformAndState(platform,Constants.open);
        return configs;
    }

    public List<WashCodeConfig> getWashCodeConfig(Long userId) {
        List<WashCodeConfig> washCodeList = new ArrayList<>();
        List<String> platformList = new ArrayList<>();
        for (String platform : Constants.PLATFORM_ARRAY) {
            platformList.add(platform);
        }
        List<UserWashCodeConfig> washCodeConfigList = userWashCodeConfigRepository.findByUserIdAndPlatformIn(userId, platformList);
        for (UserWashCodeConfig codeConfig : washCodeConfigList) {
            //筛选用户级别没有配置的平台
            Iterator<String> configIterator = platformList.iterator();
            while (configIterator.hasNext()) {
                String platform = configIterator.next();
                if (platform.equals(codeConfig.getPlatform())) {
                    configIterator.remove();
                }
            }
            WashCodeConfig config = null;
            if (codeConfig.getState() != Constants.open) {
                continue;
            }
            config = new WashCodeConfig();
            BeanUtils.copyProperties(codeConfig, config);
            washCodeList.add(config);
        }
        if (!CollectionUtils.isEmpty(platformList)) {
            List<WashCodeConfig> configList = washCodeConfigService.findByStateAndPlatformIn(Constants.open, platformList);
            washCodeList.addAll(configList);
        }
        return washCodeList;
    }

    public WashCodeConfig getWashCodeConfigByUserIdAndGameId(String platform,Long userId,String gameId) {
        List<WashCodeConfig> washCodeConfig = getWashCodeConfig(platform,userId);
        for (WashCodeConfig config : washCodeConfig) {
            if(!ObjectUtils.isEmpty(config.getGameId())&&config.getGameId().equals(gameId)){
                return config;
            }
        }
        return null;
    }
}

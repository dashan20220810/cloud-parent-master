package com.qianyi.casinocore.business;

import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.*;
import com.qianyi.casinocore.service.GameRecordObdjService;
import com.qianyi.casinocore.service.GameRecordObtyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 代理抽点业务类
 *
 * @author lance
 * @since 2022 -02-21 13:43:14
 */
@Slf4j
@Service
public class ExtractPointsConfigBusiness {

    @Autowired
    private ExtractPointsConfigRepository extractPointsConfigRepository;

    @Autowired
    private PoxyExtractPointsConfigRepository poxyExtractPointsConfigRepository;

    @Autowired
    private UserExtractPointsConfigRepository userExtractPointsConfigRepository;

    @Autowired
    private ExtractPointsChangeRepository extractPointsChangeRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private GameRecordGoldenFRepository gameRecordGoldenFRepository;
    @Autowired
    private GameRecordObdjService gameRecordObdjService;
    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private UserService userService;

    /**
     * 查询代理抽点配置
     *
     * @param poxyId 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 13:43:14
     */
    public List<PoxyExtractPointsConfig> findPoxyExtractPointsConfig(Long poxyId) {
        List<PoxyExtractPointsConfig> configs = poxyExtractPointsConfigRepository.findAllByPoxyId(poxyId);

        if (configs.isEmpty()) {
            List<ExtractPointsConfig> all = extractPointsConfigRepository.findAll();
            // 全部取默认配置
            return DTOUtil.toDTO(all, PoxyExtractPointsConfig.class, c -> {
                c.setId(null);
                c.setPoxyId(poxyId);
            });

        } else {
            List<ExtractPointsConfig> all = extractPointsConfigRepository.findAll();
            if (configs.size() != all.size()) {
                // 有代理配置的，以代理配置为主，剩余取默认配置
                Map<String, PoxyExtractPointsConfig> pmap = configs.stream().collect(
                        Collectors.toMap(PoxyExtractPointsConfig::getGameId, c -> c)
                );
                Map<String, ExtractPointsConfig> allmap = all.stream().collect(
                        Collectors.toMap(ExtractPointsConfig::getGameId, c -> c)
                );

                for (String gameId: allmap.keySet()) {
                    // 没有该配置的，追加到集合
                    if (!pmap.containsKey(gameId)) {
                        ExtractPointsConfig d = allmap.get(gameId);
                        PoxyExtractPointsConfig config = DTOUtil.toDTO(d, PoxyExtractPointsConfig.class, c -> {
                            c.setId(null);
                            c.setPoxyId(poxyId);
                        });
                        configs.add(config);
                    }
                }
            }
        }
        return configs.stream()
                .sorted(Comparator.comparing(PoxyExtractPointsConfig::getGameEnName))
                .collect(Collectors.toList());
    }

    /**
     * 查询用户抽点配置
     *
     * @param userId 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 13:55:37
     */
    public List<UserExtractPointsConfig> findUserExtractPointsConfig(Long userId) {
        User user = userService.findById(userId);
        if (null == user) {
            throw new BusinessException("用户不存在");
        }
        Long poxyId = user.getThirdProxy();
        List<UserExtractPointsConfig> configs = userExtractPointsConfigRepository.findAllByUserIdAndPoxyId(userId, poxyId);
        if (configs.isEmpty()) {
            // 如果没有配置，则取代理配置
            List<PoxyExtractPointsConfig> poxyConfigs = findPoxyExtractPointsConfig(poxyId);
            return DTOUtil.toDTO(poxyConfigs, UserExtractPointsConfig.class, c -> {
                c.setId(null);
                c.setUserId(userId);
            });
        } else {
            List<PoxyExtractPointsConfig> poxyConfigs = findPoxyExtractPointsConfig(poxyId);
            if (configs.size() != poxyConfigs.size()) {
                Map<String, UserExtractPointsConfig> umap = configs.stream().collect(
                        Collectors.toMap(UserExtractPointsConfig::getGameId, c -> c)
                );

                Map<String, PoxyExtractPointsConfig> allmap = poxyConfigs.stream().collect(
                        Collectors.toMap(PoxyExtractPointsConfig::getGameId, c -> c)
                );

                for (String gameId: allmap.keySet()) {
                    // 没有该配置的，追加到集合
                    if (!umap.containsKey(gameId)) {
                        PoxyExtractPointsConfig d = allmap.get(gameId);
                        UserExtractPointsConfig config = DTOUtil.toDTO(d, UserExtractPointsConfig.class, c -> {
                            c.setId(null);
                            c.setPoxyId(poxyId);
                            c.setUserId(userId);
                        });
                        configs.add(config);
                    }
                }

            }
        }
        return configs.stream()
                .sorted(Comparator.comparing(UserExtractPointsConfig::getGameEnName))
                .collect(Collectors.toList());
    }


    /**
     * 更新抽点配置
     *
     * @param configList 入参释义
     * @return {@link Map} 出参释义
     * @author lance
     * @since 2022 -02-21 17:58:14
     */
    public Map<String, List<ExtractPointsConfig>> updateExtractPointsConfigs(List<ExtractPointsConfig> configList) {
        if (configList.isEmpty()) {
            throw new BusinessException("保存抽点配置失败");
        }

        // 校验参数
        for (ExtractPointsConfig config: configList) {
            if (config.getRate() != null) {
                // 小于0
                if (config.getRate().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_0) {
                    throw new BusinessException("倍率不能小于0");
                }
                // 大于 5%
                if (config.getRate().compareTo(BigDecimal.valueOf(5)) > CommonConst.NUMBER_0) {
                    throw new BusinessException("倍率超过限制");
                }
            }

        }

        List<ExtractPointsConfig> extractPointsConfigs = extractPointsConfigRepository.saveAll(configList);

        return extractPointsConfigs.stream().collect(
                Collectors.groupingBy(ExtractPointsConfig::getPlatform)
        );
    }

    /**
     * 更新代理抽点配置
     *
     * @param configList 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 19:24:03
     */
    public List<PoxyExtractPointsConfig> updatePoxyExtractPointsConfigs(List<PoxyExtractPointsConfig> configList) {
        if (configList.isEmpty()) {
            throw new BusinessException("保存抽点配置失败");
        }

        // 校验参数
        for (PoxyExtractPointsConfig config : configList) {
            if (config.getPoxyId() == null) {
                throw new BusinessException("必须指定代理id");
            }
            if (config.getRate() != null) {
                // 小于0
                if (config.getRate().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_0) {
                    throw new BusinessException("倍率不能小于0");
                }
                // 大于 5%
                if (config.getRate().compareTo(BigDecimal.valueOf(5)) > CommonConst.NUMBER_0) {
                    throw new BusinessException("倍率超过限制");
                }
            }
        }
        Long poxyId = configList.get(0).getPoxyId();
        List<PoxyExtractPointsConfig> poxyConfigs = poxyExtractPointsConfigRepository.findAllByPoxyId(poxyId);
        // 第一次编辑，保存所有抽点配置
        if (poxyConfigs.isEmpty()) {
            List<ExtractPointsConfig> all = extractPointsConfigRepository.findAll();

            Map<String, PoxyExtractPointsConfig> pmap = configList.stream().collect(
                    Collectors.toMap(PoxyExtractPointsConfig::getGameId, c -> c)
            );
            Map<String, ExtractPointsConfig> allmap = all.stream().collect(
                    Collectors.toMap(ExtractPointsConfig::getGameId, c -> c)
            );

            for (String gameId: allmap.keySet()) {
                // 没有该配置的，追加到集合
                if (!pmap.containsKey(gameId)) {
                    ExtractPointsConfig d = allmap.get(gameId);
                    PoxyExtractPointsConfig config = DTOUtil.toDTO(d, PoxyExtractPointsConfig.class, c -> {
                        c.setId(null);
                        c.setPoxyId(poxyId);
                    });
                    configList.add(config);
                }
            }
        }

        return poxyExtractPointsConfigRepository.saveAll(configList);
    }

    /**
     * 更新会员抽点配置
     *
     * @param configList 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 19:24:03
     */
    public List<UserExtractPointsConfig> updateUserExtractPointsConfigs(List<UserExtractPointsConfig> configList) {
        if (configList.isEmpty()) {
            throw new BusinessException("保存抽点配置失败");
        }

        // 校验参数
        for (UserExtractPointsConfig config : configList) {

            if (config.getUserId() == null) {
                throw new BusinessException("必须指定用户id");
            }
            if (config.getRate() != null) {
                // 小于0
                if (config.getRate().compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_0) {
                    throw new BusinessException("倍率不能小于0");
                }
                // 大于 5%
                if (config.getRate().compareTo(BigDecimal.valueOf(5)) > CommonConst.NUMBER_0) {
                    throw new BusinessException("倍率超过限制");
                }
            }

        }

        Long userId = configList.get(0).getUserId();
        User user = userService.findById(userId);
        if (null == user) {
            throw new BusinessException("用户不存在");
        }
        Long poxyId = user.getThirdProxy();
        if (null == poxyId) {
            throw new BusinessException("只允许设置基础代理下的用户");
        }
        List<UserExtractPointsConfig> userConfigs = userExtractPointsConfigRepository.findAllByUserIdAndPoxyId(userId, poxyId);

        // 第一次编辑，保存所有抽点配置
        if (userConfigs.isEmpty()) {
            List<PoxyExtractPointsConfig> all = findPoxyExtractPointsConfig(poxyId);

            Map<String, UserExtractPointsConfig> umap = configList.stream().collect(
                    Collectors.toMap(UserExtractPointsConfig::getGameId, c -> c)
            );

            Map<String, PoxyExtractPointsConfig> allmap = all.stream().collect(
                    Collectors.toMap(PoxyExtractPointsConfig::getGameId, c -> c)
            );

            for (String gameId: allmap.keySet()) {
                // 没有该配置的，追加到集合
                if (!umap.containsKey(gameId)) {
                    PoxyExtractPointsConfig d = allmap.get(gameId);
                    UserExtractPointsConfig config = DTOUtil.toDTO(d, UserExtractPointsConfig.class, c -> {
                        c.setId(null);
                        c.setPoxyId(poxyId);
                        c.setUserId(userId);
                    });
                    configList.add(config);
                }
            }
        }

        return userExtractPointsConfigRepository.saveAll(configList);
    }


    /**
     * 获取代理抽点配置
     *
     * @param gameId 入参释义
     * @param poxyId 入参释义
     * @return {@link PoxyExtractPointsConfig} 出参释义
     * @author lance
     * @since 2022 -02-28 18:51:45
     */
    public PoxyExtractPointsConfig getPoxyExtractPointsConfig(String gameId, Long poxyId) {
        PoxyExtractPointsConfig poxyConfig = poxyExtractPointsConfigRepository.findFirstByGameIdAndPoxyId(gameId, poxyId);
        if (null == poxyConfig) {
            // 默认抽点配置
            ExtractPointsConfig defaultConfig = extractPointsConfigRepository.findFirstByGameId(gameId);
            return DTOUtil.toDTO(defaultConfig, PoxyExtractPointsConfig.class, c -> {
                c.setPoxyId(poxyId);
            });
        }
        return poxyConfig;
    }

    /**
     * 获取用户抽点配置
     * 先获取代理抽点配置，如果对应的抽点配置为关闭状态，则以代理抽点配置为主，否则取用户的抽点配置
     *
     * @param gameId 入参释义
     * @param poxyId 入参释义
     * @param userId 入参释义
     * @return {@link UserExtractPointsConfig} 出参释义
     * @author lance
     * @since 2022 -02-28 18:51:45
     */
    public UserExtractPointsConfig getUserExtractPointsConfig(String gameId, Long poxyId, Long userId) {
        // 取代理配置
        PoxyExtractPointsConfig poxyConfig = getPoxyExtractPointsConfig(gameId, poxyId);
        if (Constants.no.equals(poxyConfig.getState())) {
            return DTOUtil.toDTO(poxyConfig, UserExtractPointsConfig.class, c-> {
                c.setUserId(userId);
            });
        }

        UserExtractPointsConfig userConfig = userExtractPointsConfigRepository.findFirstByGameIdAndPoxyIdAndUserId(gameId, poxyId, userId);
        // 如果没有用户配置，则取代理配置
        if (null == userConfig) {
            return DTOUtil.toDTO(poxyConfig, UserExtractPointsConfig.class, c-> {
                c.setUserId(userId);
            });
        }
        return userConfig;
    }

    /**
     * 抽点
     *
     * @param platform   平台
     * @param gameRecord 游戏记录
     * @author lance
     * @since 2022 -02-22 11:36:09
     */
    @Transactional
    public void extractPoints(String platform, GameRecord gameRecord) {
        //已经处理过的不需要再次处理
        if (gameRecord.getExtractStatus() != null && gameRecord.getExtractStatus() == Constants.yes) {
            return;
        }
        // 有效投注额
        BigDecimal validBet = new BigDecimal(gameRecord.getValidbet());
        // 基础代理id
        Long poxyId = gameRecord.getThirdProxy();
        if (null == poxyId) {
            log.warn("数据异常: {}", gameRecord);
            return;
        }
        // 玩家id
        Long userId = gameRecord.getUserId();

        String gameId = null;
        if (Constants.PLATFORM_WM.equals(platform)) {
            gameId = gameRecord.getGid().toString();
        } else if (Constants.PLATFORM_PG.equals(platform) || Constants.PLATFORM_CQ9.equals(platform)) {
            //PG和CQ9的配置是以平台配置的不是以里面的游戏
            gameId = platform;
        }

        log.info("开始抽点,平台={},注单ID={},注单明细={}",platform, gameRecord.getBetId(), gameRecord);
        UserExtractPointsConfig config = getUserExtractPointsConfig(gameId, poxyId, userId);
        if (null == config) {
            log.warn("找不到抽点配置: gameId[{}], poxyId[{}], userId[{}]", gameId, poxyId, userId);
            return ;
        }
        log.info("代理抽点配置: {}", config);
        if (Constants.no.equals(config.getState())) {
            log.info("禁用抽点配置：gameId[{}], poxyId[{}], userId[{}]", gameId, poxyId, userId);
            return ;
        }
        //数据库存的10是代表百分之10
        BigDecimal rate = config.getRate().divide(new BigDecimal(100));//转换百分比
        // 抽水
        BigDecimal water = validBet.multiply(rate);

        // 保存抽水记录
        ExtractPointsChange e = new ExtractPointsChange();
        e.setAmount(water);
        e.setGameName(gameRecord.getGname());
        e.setGameRecordId(gameRecord.getId());
        e.setPlatform(platform);
        e.setRate(config.getRate());
        e.setValidBet(validBet);
        e.setPoxyId(poxyId);
        e.setUserId(userId);
        if (Constants.PLATFORM_WM.equals(platform)) {
            e.setGameId(gameRecord.getGid().toString());
        } else {
            e.setGameId(gameRecord.getGameCode());
        }
        extractPointsChangeRepository.save(e);

        // 更新抽点状态
        if (Constants.PLATFORM_WM.equals(platform)) {
            gameRecordRepository.updateExtractStatus(gameRecord.getId(), Constants.yes);
        } else if (Constants.PLATFORM_PG.equals(platform) || Constants.PLATFORM_CQ9.equals(platform)|| Constants.PLATFORM_SABASPORT.equals(platform)) {
            gameRecordGoldenFRepository.updateExtractStatus(gameRecord.getId(), Constants.yes);
        } else if (Constants.PLATFORM_OBDJ.equals(platform)) {
            gameRecordObdjService.updateExtractStatus(gameRecord.getId(), Constants.yes);
        } else if (Constants.PLATFORM_OBTY.equals(platform)) {
            gameRecordObtyService.updateExtractStatus(gameRecord.getId(), Constants.yes);
        }

        log.info("抽点完成，平台={}, 注单id={}", platform, gameRecord.getBetId());
    }

}

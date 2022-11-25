package com.qianyi.casinocore.service;

import com.qianyi.casinocore.co.extractpoints.ExtractPointsConfigCo;
import com.qianyi.casinocore.model.ExtractPointsConfig;
import com.qianyi.casinocore.repository.ExtractPointsConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代理抽点默认配置服务类
 *
 * @author lance
 * @since 2022 -02-21 12:54:42
 */
@Service
@Slf4j
public class ExtractPointsConfigService {

    @Autowired
    private ExtractPointsConfigRepository repository;

    /**
     * 代理抽点列表查询
     *
     * @param co 查询参数
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 12:54:42
     */
    public List<ExtractPointsConfig> findList(ExtractPointsConfigCo co) {
        Specification<ExtractPointsConfig> condition = (root, q, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (!StringUtils.isEmpty(co.getPlatform())) {
                list.add(cb.equal(root.get("platform").as(String.class), co.getPlatform()));
            }
            return cb.and(list.toArray(new Predicate[list.size()]));
        };
        List<ExtractPointsConfig> list = repository.findAll(condition);

        return list.stream().sorted(Comparator.comparing(ExtractPointsConfig::getGameEnName)).collect(Collectors.toList());
    }

    public List<ExtractPointsConfig> findByPlatform(String platform){
        return repository.findByPlatform(platform);
    }

    @Transactional
    public ExtractPointsConfig save(ExtractPointsConfig extractPointsConfig){
        ExtractPointsConfig save = repository.save(extractPointsConfig);
        return save;
    }
}

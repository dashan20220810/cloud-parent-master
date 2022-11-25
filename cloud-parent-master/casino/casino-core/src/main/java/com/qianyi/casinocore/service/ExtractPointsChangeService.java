package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ExtractPointsChange;
import com.qianyi.casinocore.repository.ExtractPointsChangeRepository;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 抽点记录
 *
 * @author lance
 * @since 2022 -02-22 19:27:40
 */
@Service
@Slf4j
public class ExtractPointsChangeService {

    @Autowired
    private ExtractPointsChangeRepository repository;

    public List<ExtractPointsChange> findBetween(String startTime, String endTime) {
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = DateUtil.getSimpleDateFormat().parse(startTime);
            endDate = DateUtil.getSimpleDateFormat().parse(endTime);
        } catch (ParseException e) {
            log.error("日期格式化异常", e);
        }

        return findBetween(startDate, endDate);
    }

    /**
     * 查询开始时间和结束时间之间的抽点记录
     *
     * @param startDate 入参释义
     * @param endDate   入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-22 19:27:41
     */
    public List<ExtractPointsChange> findBetween(Date startDate, Date endDate) {
        Specification<ExtractPointsChange> condition = (root, q, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (startDate != null) {
                list.add(
                    cb.greaterThanOrEqualTo(
                        root.get("createTime").as(Date.class), startDate
                    )
                );
            }
            if (endDate != null) {
                list.add(
                    cb.lessThanOrEqualTo(
                        root.get("createTime").as(Date.class), endDate
                    )
                );
            }
            return cb.and(list.toArray(new Predicate[list.size()]));
        };
        return repository.findAll(condition);
    }

    public BigDecimal sumAmount(String startTime, String endTime){
        return repository.sumAmount(startTime,endTime);
    }

    public BigDecimal sumAmount(){
        return repository.sumAmount();
    }

    public List<Map<String, Object>> getMapSumAmount(String startTime, String endTime){
        return repository.getMapSumAmount(startTime,endTime);
    }
}

package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordObdj;
import com.qianyi.casinocore.model.GameRecordObty;
import com.qianyi.casinocore.repository.GameRecordObtyRepository;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GameRecordObtyService {
    @Autowired
    private GameRecordObtyRepository gameRecordObtyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime) {
        List<Map<String,Object>> orderAmountVoList = gameRecordObtyRepository.getStatisticsResult(startTime,endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json,CompanyOrderAmountVo.class);

    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordObtyRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }


    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordObtyRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordObtyRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordObtyRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordObtyRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public void updateProfitStatus(Long id, Integer shareProfitStatus) {
        gameRecordObtyRepository.updateProfitStatus(id, shareProfitStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordObtyRepository.updateExtractStatus(id,extractStatus);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num){
        return gameRecordObtyRepository.queryGameRecords(id,num);
    }

    public GameRecordObty findGameRecordById(Long gameId){return gameRecordObtyRepository.findById(gameId).orElse(null);}

    public GameRecordObty save(GameRecordObty gameRecord) {
        return gameRecordObtyRepository.save(gameRecord);
    }

    public  GameRecordObty  findRecordRecordSum(GameRecordObty gameRecordObty,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordObty> query = cb.createQuery(GameRecordObty.class);
        Root<GameRecordObty> root = query.from(GameRecordObty.class);

        query.multiselect(
            cb.sum(root.get("profitAmount").as(BigDecimal.class)).alias("profitAmount"),
            cb.sum(root.get("orderAmount").as(BigDecimal.class)).alias("orderAmount"),
            cb.sum(root.get("settleAmount").as(BigDecimal.class)).alias("settleAmount")
        );

        List<Predicate> list = new ArrayList();
        if (!CommonUtil.checkNull(gameRecordObty.getUserName())) {
            list.add(cb.equal(root.get("userName").as(String.class), gameRecordObty.getUserName()));
        }
        if (!CommonUtil.checkNull(gameRecordObty.getOrderNo())) {
            list.add(cb.equal(root.get("orderNo").as(String.class), gameRecordObty.getOrderNo()));
        }
        if (gameRecordObty.getOutcome() != null) {
            list.add(cb.equal(root.get("outcome").as(Integer.class), gameRecordObty.getOutcome()));
        }
        if (gameRecordObty.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), gameRecordObty.getUserId()));
        }
        if (gameRecordObty.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordObty.getFirstProxy()));
        }
        if (gameRecordObty.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordObty.getSecondProxy()));
        }
        if (gameRecordObty.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordObty.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            list.add(
                cb.between(root.get("betStrTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                cb.between(root.get("settleStrTime").as(String.class), startSetTime, endSetTime)
            );
        }
        query
            .where(list.toArray(new Predicate[list.size()]));
        //                .groupBy(root.get("conversionStepCode"))
        //                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecordObty singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public Page<GameRecordObty> findGameRecordPage(GameRecordObty gameRecordObty, Pageable pageable, String startBetTime, String endBetTime, String startSetTime, String endSetTime)  {
        Specification<GameRecordObty> condition = getCondition(gameRecordObty,startBetTime,endBetTime,startSetTime,endSetTime);

        return gameRecordObtyRepository.findAll(condition, pageable);
    }

    private Specification<GameRecordObty> getCondition(GameRecordObty gameRecordObty, String startTime, String endTime, String startSetTime, String endSetTime) {
        Specification<GameRecordObty> specification = new Specification<GameRecordObty>() {
            @Override
            public Predicate toPredicate(Root<GameRecordObty> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecordObty.getUserName())) {
                    list.add(cb.equal(root.get("userName").as(String.class), gameRecordObty.getUserName()));
                }
                if (!CommonUtil.checkNull(gameRecordObty.getOrderNo())) {
                    list.add(cb.equal(root.get("orderNo").as(String.class), gameRecordObty.getOrderNo()));
                }
                if (gameRecordObty.getOutcome() != null) {
                    list.add(cb.equal(root.get("outcome").as(Integer.class), gameRecordObty.getOutcome()));
                }
                if (gameRecordObty.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecordObty.getUserId()));
                }
                if (gameRecordObty.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordObty.getFirstProxy()));
                }
                if (gameRecordObty.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordObty.getSecondProxy()));
                }
                if (gameRecordObty.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordObty.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("betStrTime").as(String.class), startTime, endTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                        cb.between(root.get("settleStrTime").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public List<GameRecordObty> findGameRecord(GameRecordObty gameRecord,String startTime,String endTime)  {
        Specification<GameRecordObty> condition = getConditionGameRecord(gameRecord,startTime,endTime);

        return gameRecordObtyRepository.findAll(condition);
    }

    private Specification<GameRecordObty> getConditionGameRecord(GameRecordObty gameRecord, String startTime, String endTime) {
        Specification<GameRecordObty> specification = new Specification<GameRecordObty>() {
            @Override
            public Predicate toPredicate(Root<GameRecordObty> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecord.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), gameRecord.getGameRecordStatus()));
                }

                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("createTime").as(String.class), startTime, endTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

}

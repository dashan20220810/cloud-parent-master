package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordObdj;
import com.qianyi.casinocore.repository.GameRecordObdjRepository;
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
public class GameRecordObdjService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private GameRecordObdjRepository gameRecordObdjRepository;

    public void updateCodeNumStatus(Long id, Integer codeNumStatus) {
        gameRecordObdjRepository.updateCodeNumStatus(id, codeNumStatus);
    }

    public void updateWashCodeStatus(Long id, Integer washCodeStatus) {
        gameRecordObdjRepository.updateWashCodeStatus(id, washCodeStatus);
    }

    public void updateRebateStatus(Long id, Integer rebateStatus) {
        gameRecordObdjRepository.updateRebateStatus(id, rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer gameRecordStatus) {
        gameRecordObdjRepository.updateGameRecordStatus(id, gameRecordStatus);
    }

    public void updateProfitStatus(Long id, Integer shareProfitStatus) {
        gameRecordObdjRepository.updateProfitStatus(id, shareProfitStatus);
    }

    public void updateExtractStatus(Long id, Integer extractStatus) {
        gameRecordObdjRepository.updateExtractStatus(id,extractStatus);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num){
        return gameRecordObdjRepository.queryGameRecords(id,num);
    }

    public GameRecordObdj findGameRecordById(Long gameId){return gameRecordObdjRepository.findById(gameId).orElse(null);}

    public GameRecordObdj save(GameRecordObdj gameRecord) {
        return gameRecordObdjRepository.save(gameRecord);
    }

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime){
        List<Map<String,Object>> orderAmountVoList = gameRecordObdjRepository.getStatisticsResult(startTime,endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json,CompanyOrderAmountVo.class);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long userId) {
        return gameRecordObdjRepository.countByIdLessThanEqualAndUserId(createTime,userId);
    }

    public GameRecordObdj findByBetId(Long betId){
        return gameRecordObdjRepository.findByBetId(betId);
    }

    public Page<GameRecordObdj> findGameRecordPage(GameRecordObdj gameRecordObdj, Pageable pageable,String startBetTime,String endBetTime,String startSetTime,String endSetTime)  {
        Specification<GameRecordObdj> condition = getCondition(gameRecordObdj,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordObdjRepository.findAll(condition, pageable);
    }

    public  GameRecordObdj  findRecordRecordSum(GameRecordObdj gameRecordObdj,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordObdj> query = cb.createQuery(GameRecordObdj.class);
        Root<GameRecordObdj> root = query.from(GameRecordObdj.class);

        query.multiselect(
            cb.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
            cb.sum(root.get("winAmount").as(BigDecimal.class)).alias("winAmount")
        );

        List<Predicate> list = new ArrayList();

        if (!CommonUtil.checkNull(gameRecordObdj.getMemberAccount())) {
            list.add(cb.equal(root.get("memberAccount").as(String.class), gameRecordObdj.getMemberAccount()));
        }
        if (gameRecordObdj.getBetId() != null) {
            list.add(cb.equal(root.get("betId").as(Long.class), gameRecordObdj.getBetId()));
        }
        if (gameRecordObdj.getBetStatus() != null) {
            list.add(cb.equal(root.get("betStatus").as(Integer.class), gameRecordObdj.getBetStatus()));
        }
        if (gameRecordObdj.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), gameRecordObdj.getUserId()));
        }
        if (gameRecordObdj.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordObdj.getFirstProxy()));
        }
        if (gameRecordObdj.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordObdj.getSecondProxy()));
        }
        if (gameRecordObdj.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordObdj.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            list.add(
                cb.between(root.get("betStrTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                cb.between(root.get("setStrTime").as(String.class), startSetTime, endSetTime)
            );
        }
        query
            .where(list.toArray(new Predicate[list.size()]));
        //                .groupBy(root.get("conversionStepCode"))
        //                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecordObdj singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }


    private Specification<GameRecordObdj> getCondition(GameRecordObdj gameRecordObdj, String startTime, String endTime, String startSetTime, String endSetTime) {
        Specification<GameRecordObdj> specification = new Specification<GameRecordObdj>() {
            @Override
            public Predicate toPredicate(Root<GameRecordObdj> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecordObdj.getMemberAccount())) {
                    list.add(cb.equal(root.get("memberAccount").as(String.class), gameRecordObdj.getMemberAccount()));
                }
                if (gameRecordObdj.getBetId() != null) {
                    list.add(cb.equal(root.get("betId").as(Long.class), gameRecordObdj.getBetId()));
                }
                if (gameRecordObdj.getBetStatus() != null) {
                    list.add(cb.equal(root.get("betStatus").as(Integer.class), gameRecordObdj.getBetStatus()));
                }
                if (gameRecordObdj.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecordObdj.getUserId()));
                }
                if (gameRecordObdj.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordObdj.getFirstProxy()));
                }
                if (gameRecordObdj.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordObdj.getSecondProxy()));
                }
                if (gameRecordObdj.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordObdj.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("betStrTime").as(String.class), startTime, endTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                        cb.between(root.get("setStrTime").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public List<GameRecordObdj> findGameRecord(GameRecordObdj gameRecord,String startTime,String endTime,List<Integer> betStatus)  {
        Specification<GameRecordObdj> condition = getCondition(gameRecord,startTime,endTime,betStatus);

        return gameRecordObdjRepository.findAll(condition);
    }

    private Specification<GameRecordObdj> getCondition(GameRecordObdj gameRecord, String startTime, String endTime,List<Integer> betStatus) {
        Specification<GameRecordObdj> specification = new Specification<GameRecordObdj>() {
            @Override
            public Predicate toPredicate(Root<GameRecordObdj> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecord.getGameRecordStatus() != null) {
                    list.add(cb.equal(root.get("gameRecordStatus").as(Integer.class), gameRecord.getGameRecordStatus()));
                }

                if (betStatus != null){
                    CriteriaBuilder.In<Object> in = cb.in(root.get("betStatus"));
                    for (Integer id : betStatus) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
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

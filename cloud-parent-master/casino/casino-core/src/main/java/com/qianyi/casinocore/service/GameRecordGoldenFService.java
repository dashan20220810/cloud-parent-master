package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.model.GameRecordGoldenfEndTime;
import com.qianyi.casinocore.repository.GameRecordGoldenFRepository;
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
import java.util.*;

@Service
public class GameRecordGoldenFService {

    @Autowired
    private GameRecordGoldenFRepository gameRecordGoldenFRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public void updateCodeNumStatus(Long id,Integer codeNumStatus){
        gameRecordGoldenFRepository.updateCodeNumStatus(id,codeNumStatus);
    }

    public void updateWashCodeStatus(Long id,Integer washCodeStatus){
        gameRecordGoldenFRepository.updateWashCodeStatus(id,washCodeStatus);
    }

    public void updateRebateStatus(Long id,Integer rebateStatus){
        gameRecordGoldenFRepository.updateRebateStatus(id,rebateStatus);
    }

    public void updateGameRecordStatus(Long id,Integer gameRecordStatus){
        gameRecordGoldenFRepository.updateGameRecordStatus(id,gameRecordStatus);
    }

    public List<Map<String, Object>> findSumBetAmount(String startTime, String endTime){
        return gameRecordGoldenFRepository.findSumBetAmount(startTime,endTime);
    }

    public Set<Long> findGroupByUser(String startTime,String endTime){
        return gameRecordGoldenFRepository.findGroupByUser(startTime,endTime);
    }

    public Set<Long> findGroupByUser(){
        return gameRecordGoldenFRepository.findGroupByUser();
    }

    public Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime){
        return gameRecordGoldenFRepository.findSumBetAndWinLoss(startTime,endTime);
    }

    public Map<String, Object> findSumBetAndWinLoss(){
        return gameRecordGoldenFRepository.findSumBetAndWinLoss();
    }

    public void updateProfitStatus(Long id,Integer shareProfitStatus){
        gameRecordGoldenFRepository.updateProfitStatus(id,shareProfitStatus);
    }

    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime){
        List<Map<String,Object>> orderAmountVoList = gameRecordGoldenFRepository.getStatisticsResult(startTime,endTime);
        String json = JSON.toJSONString(orderAmountVoList);
        return JSON.parseArray(json,CompanyOrderAmountVo.class);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num,String platform){
        return gameRecordGoldenFRepository.queryGameRecords(id,num,platform);
    }

    public BigDecimal findSumBetAmount(Long userId,String startTime,String endTime){
        return gameRecordGoldenFRepository.findSumBetAmount(userId,startTime,endTime);
    }

    public Set<Long> findGroupByFirst(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findGroupByFirst(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossByFirst(startTime,endTime,firstProxy);
    }

    public Set<Long> findGroupBySecond(String startTime,String endTime,Long secondProxy){
        return gameRecordGoldenFRepository.findGroupBySecond(startTime,endTime,secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long secondProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossBySecond(startTime,endTime,secondProxy);
    }

    public Set<Long> findGroupByThird(String startTime,String endTime,Long thirdProxy){
        return gameRecordGoldenFRepository.findGroupByThird(startTime,endTime,thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long thirdProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossByThird(startTime,endTime,thirdProxy);
    }

    public Set<Long> findGroupByFirst(Long firstProxy){
        return gameRecordGoldenFRepository.findGroupByFirst(firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossByFirst(firstProxy);
    }

    public Set<Long> findGroupBySecond(Long secondProxy){
        return gameRecordGoldenFRepository.findGroupBySecond(secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossBySecond(secondProxy);
    }

    public Set<Long> findGroupByThird(Long thirdProxy){
        return gameRecordGoldenFRepository.findGroupByThird(thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy){
        return gameRecordGoldenFRepository.findSumBetAndWinLossByThird(thirdProxy);
    }

    public int countByIdLessThanEqualAndUserId(Date createTime, Long UserId){
        return gameRecordGoldenFRepository.countByIdLessThanEqualAndUserId(createTime,UserId);
    }

    public GameRecordGoldenF findGameRecordById(Long gameId){return gameRecordGoldenFRepository.findById(gameId).orElse(null);}

    public GameRecordGoldenF save(GameRecordGoldenF gameRecordGoldenF){
        return gameRecordGoldenFRepository.save(gameRecordGoldenF);
    }

    public GameRecordGoldenF findGameRecordGoldenFByTraceId(String traceId){
        return gameRecordGoldenFRepository.findGameRecordGoldenFByTraceId(traceId);
    }


    public List<GameRecordGoldenF> findGameRecordGoldenFs(GameRecordGoldenF gameRecordGoldenF,String startTime,String endTime) {
        Specification<GameRecordGoldenF> condition = getCondition(gameRecordGoldenF,startTime,endTime);
        return gameRecordGoldenFRepository.findAll(condition);
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecordGoldenF> getCondition(GameRecordGoldenF gameRecordGoldenF,String startTime,String endTime) {
        Specification<GameRecordGoldenF> specification = new Specification<GameRecordGoldenF>() {
            @Override
            public Predicate toPredicate(Root<GameRecordGoldenF> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecordGoldenF.getId() != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("id").as(Long.class), gameRecordGoldenF.getId()));
                }
                if (gameRecordGoldenF.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecordGoldenF.getUserId()));
                }
                if (!CommonUtil.checkNull(gameRecordGoldenF.getPlayerName())) {
                    list.add(cb.equal(root.get("playerName").as(String.class), gameRecordGoldenF.getPlayerName()));
                }
                if (!CommonUtil.checkNull(gameRecordGoldenF.getVendorCode())) {
                    list.add(cb.equal(root.get("vendorCode").as(String.class), gameRecordGoldenF.getVendorCode()));
                }
                if (!CommonUtil.checkNull(gameRecordGoldenF.getTransType())) {
                    list.add(cb.equal(root.get("transType").as(String.class), gameRecordGoldenF.getTransType()));
                }
                if (!CommonUtil.checkNull(gameRecordGoldenF.getGameCode())) {
                    list.add(cb.equal(root.get("gameCode").as(String.class), gameRecordGoldenF.getGameCode()));
                }
                if (!CommonUtil.checkNull(gameRecordGoldenF.getBetId())) {
                    list.add(cb.equal(root.get("betId").as(String.class), gameRecordGoldenF.getBetId()));
                }
                if (!CommonUtil.checkNull(gameRecordGoldenF.getParentBetId())) {
                    list.add(cb.equal(root.get("parentBetId").as(String.class), gameRecordGoldenF.getParentBetId()));
                }
                if (gameRecordGoldenF.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordGoldenF.getFirstProxy()));
                }
                if (gameRecordGoldenF.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordGoldenF.getSecondProxy()));
                }
                if (gameRecordGoldenF.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordGoldenF.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("createAtStr").as(String.class), startTime, endTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }

    public Page<GameRecordGoldenF> findGameRecordGoldenFPage(GameRecordGoldenF gameRecordGoldenF, Pageable pageable,String startBetTime,String endBetTime) {
        Specification<GameRecordGoldenF> condition = getCondition(gameRecordGoldenF,startBetTime,endBetTime);
        return gameRecordGoldenFRepository.findAll(condition, pageable);
    }

    public Long findMaxId(){
        return gameRecordGoldenFRepository.findMaxId();
    }

    public List<GameRecordGoldenF> findGameRecord(GameRecordGoldenF gameRecord,String startTime,String endTime)  {
        Specification<GameRecordGoldenF> condition = getConditionGameRecord(gameRecord,startTime,endTime);

        return gameRecordGoldenFRepository.findAll(condition);
    }

    private Specification<GameRecordGoldenF> getConditionGameRecord(GameRecordGoldenF gameRecord, String startTime, String endTime) {
        Specification<GameRecordGoldenF> specification = new Specification<GameRecordGoldenF>() {
            @Override
            public Predicate toPredicate(Root<GameRecordGoldenF> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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

    public  GameRecordGoldenF  findRecordRecordSum(GameRecordGoldenF gameRecordGoldenF,String startSetTime,String endSetTime) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordGoldenF> query = cb.createQuery(GameRecordGoldenF.class);
        Root<GameRecordGoldenF> root = query.from(GameRecordGoldenF.class);

        query.multiselect(
            cb.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
            cb.sum(root.get("winAmount").as(BigDecimal.class)).alias("winAmount")
        );

        List<Predicate> list = new ArrayList();

        if (gameRecordGoldenF.getUserId() != null) {
            list.add(cb.equal(root.get("userId").as(Long.class), gameRecordGoldenF.getUserId()));
        }
        if (!CommonUtil.checkNull(gameRecordGoldenF.getPlayerName())) {
            list.add(cb.equal(root.get("playerName").as(String.class), gameRecordGoldenF.getPlayerName()));
        }
        if (!CommonUtil.checkNull(gameRecordGoldenF.getVendorCode())) {
            list.add(cb.equal(root.get("vendorCode").as(String.class), gameRecordGoldenF.getVendorCode()));
        }
        if (!CommonUtil.checkNull(gameRecordGoldenF.getTransType())) {
            list.add(cb.equal(root.get("transType").as(String.class), gameRecordGoldenF.getTransType()));
        }
        if (!CommonUtil.checkNull(gameRecordGoldenF.getGameCode())) {
            list.add(cb.equal(root.get("gameCode").as(String.class), gameRecordGoldenF.getGameCode()));
        }
        if (!CommonUtil.checkNull(gameRecordGoldenF.getBetId())) {
            list.add(cb.equal(root.get("betId").as(String.class), gameRecordGoldenF.getBetId()));
        }
        if (!CommonUtil.checkNull(gameRecordGoldenF.getParentBetId())) {
            list.add(cb.equal(root.get("parentBetId").as(String.class), gameRecordGoldenF.getParentBetId()));
        }
        if (gameRecordGoldenF.getFirstProxy() != null) {
            list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecordGoldenF.getFirstProxy()));
        }
        if (gameRecordGoldenF.getSecondProxy() != null) {
            list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecordGoldenF.getSecondProxy()));
        }
        if (gameRecordGoldenF.getThirdProxy() != null) {
            list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecordGoldenF.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            list.add(
                cb.between(root.get("createAtStr").as(String.class), startSetTime, endSetTime)
            );
        }
        query
            .where(list.toArray(new Predicate[list.size()]));
        //                .groupBy(root.get("conversionStepCode"))
        //                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecordGoldenF singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

}

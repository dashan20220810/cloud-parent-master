package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.repository.GameRecordRepository;
import com.qianyi.casinocore.util.DTOUtil;
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
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @PersistenceContext
    private EntityManager entityManager;


    public List<GameRecord> findGameRecordList(GameRecord gameRecord, String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecord> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordRepository.findAll(condition);
    }

    public Page<GameRecord> findGameRecordPage(GameRecord gameRecord, Pageable pageable,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecord> condition = getCondition(gameRecord,startBetTime,endBetTime,startSetTime,endSetTime);
        return gameRecordRepository.findAll(condition, pageable);
    }

    public List<GameRecord> findGameRecords(GameRecord gameRecord,String startTime,String endTime) {
        Specification<GameRecord> condition = getCondition(gameRecord,startTime,endTime);
        return gameRecordRepository.findAll(condition);
    }

    public List<Map<String, Object>> findGameRecords(String startTime,String endTime) {
        return gameRecordRepository.findGameRecords(startTime,endTime);
    }

    public BigDecimal findGameRecords(Long userId,String startTime,String endTime) {
        return gameRecordRepository.findGameRecords(userId,startTime,endTime);
    }

    public Set<Long> findGroupByUser(String startTime,String endTime){
        return gameRecordRepository.findGroupByUser(startTime,endTime);
    }

    public Set<Long> findGroupByUser(){
        return gameRecordRepository.findGroupByUser();
    }

    public Map<String, Object> findSumBetAndWinLoss(String startTime,String endTime){
        return gameRecordRepository.findSumBetAndWinLoss(startTime,endTime);
    }

    public Map<String, Object> findSumBetAndWinLoss(){
        return gameRecordRepository.findSumBetAndWinLoss();
    }

    public Set<Long> findGroupByFirst(String startTime,String endTime,Long firstProxy){
        return gameRecordRepository.findGroupByFirst(startTime,endTime,firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(String startTime,String endTime,Long firstProxy){
        return gameRecordRepository.findSumBetAndWinLossByFirst(startTime,endTime,firstProxy);
    }

    public Set<Long> findGroupBySecond(String startTime,String endTime,Long secondProxy){
        return gameRecordRepository.findGroupBySecond(startTime,endTime,secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(String startTime,String endTime,Long secondProxy){
        return gameRecordRepository.findSumBetAndWinLossBySecond(startTime,endTime,secondProxy);
    }

    public Set<Long> findGroupByThird(String startTime,String endTime,Long thirdProxy){
        return gameRecordRepository.findGroupByThird(startTime,endTime,thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(String startTime,String endTime,Long thirdProxy){
        return gameRecordRepository.findSumBetAndWinLossByThird(startTime,endTime,thirdProxy);
    }

    public Set<Long> findGroupByFirst(Long firstProxy){
        return gameRecordRepository.findGroupByFirst(firstProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy){
        return gameRecordRepository.findSumBetAndWinLossByFirst(firstProxy);
    }

    public Set<Long> findGroupBySecond(Long secondProxy){
        return gameRecordRepository.findGroupBySecond(secondProxy);
    }

    public Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy){
        return gameRecordRepository.findSumBetAndWinLossBySecond(secondProxy);
    }

    public Set<Long> findGroupByThird(Long thirdProxy){
        return gameRecordRepository.findGroupByThird(thirdProxy);
    }

    public Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy){
        return gameRecordRepository.findSumBetAndWinLossByThird(thirdProxy);
    }

    public  GameRecord  findRecordRecordSum(GameRecord game,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecord> query = builder.createQuery(GameRecord.class);
        Root<GameRecord> root = query.from(GameRecord.class);

        query.multiselect(
            builder.sum(root.get("bet").as(BigDecimal.class)).alias("bet"),
            builder.sum(root.get("validbet").as(BigDecimal.class)).alias("validbet"),
            builder.sum(root.get("water").as(BigDecimal.class)).alias("water"),
            builder.sum(root.get("waterbet").as(BigDecimal.class)).alias("waterbet"),
            builder.sum(root.get("winLoss").as(BigDecimal.class)).alias("winLoss")
        );

        List<Predicate> predicates = new ArrayList();

        if (game.getUserId() != null) {
            predicates.add(
                builder.equal(root.get("userId").as(Integer.class), game.getUserId())
            );
        }
        if (!CommonUtil.checkNull(game.getBetId())) {
            predicates.add(
                builder.equal(root.get("betId").as(String.class), game.getBetId())
            );
        }
        if (!CommonUtil.checkNull(game.getUser())) {
            predicates.add(
                builder.equal(root.get("user").as(String.class), game.getUser())
            );
        }
        if (!CommonUtil.checkNull(game.getGname())) {
            predicates.add(
                builder.equal(root.get("gname").as(String.class), game.getGname())
            );
        }
        if (game.getGid() != null) {
            predicates.add(
                builder.equal(root.get("gid").as(Integer.class), game.getGid())
            );
        }
        if (game.getFirstProxy() != null) {
            predicates.add(
                builder.equal(root.get("firstProxy").as(Long.class), game.getFirstProxy())
            );
        }
        if (game.getSecondProxy() != null) {
            predicates.add(
                builder.equal(root.get("secondProxy").as(Long.class), game.getSecondProxy())
            );
        }
        if (game.getThirdProxy() != null) {
            predicates.add(
                builder.equal(root.get("thirdProxy").as(Long.class), game.getThirdProxy())
            );
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            predicates.add(
                builder.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
            );
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            predicates.add(
                builder.between(root.get("settime").as(String.class), startSetTime, endSetTime)
            );
        }
        query
            .where(predicates.toArray(new Predicate[predicates.size()]));
        //                .groupBy(root.get("conversionStepCode"))
        //                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecord singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public GameRecord save(GameRecord gameRecord) {
        return gameRecordRepository.save(gameRecord);
    }

    public List<Map<String,Object>> queryGameRecords(String startTime,String endTime){
        return gameRecordRepository.queryGameRecords(startTime,endTime);
    }

    public List<Map<String,Object>> queryGameRecords(Long id,Integer num){
        return gameRecordRepository.queryGameRecords(id,num);
    }

    public GameRecord findGameRecordById(Long gameId){return gameRecordRepository.findById(gameId).orElse(null);}

    public int countByIdLessThanEqualAndUserId(Date createTime, Long UserId){
        return gameRecordRepository.countByIdLessThanEqualAndUserId(createTime,UserId);
    }


    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecord> getCondition(GameRecord gameRecord,String startTime,String endTime) {
        Specification<GameRecord> specification = new Specification<GameRecord>() {
            @Override
            public Predicate toPredicate(Root<GameRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(gameRecord.getUser())) {
                    list.add(cb.equal(root.get("user").as(String.class), gameRecord.getUser()));
                }
                if (!CommonUtil.checkNull(gameRecord.getBetId())) {
                    list.add(cb.equal(root.get("betId").as(String.class), gameRecord.getBetId()));
                }
                if (!CommonUtil.checkNull(gameRecord.getGname())) {
                    list.add(cb.equal(root.get("gname").as(String.class), gameRecord.getGname()));
                }
                if (gameRecord.getGid() != null) {
                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
                }
                if (gameRecord.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecord.getUserId()));
                }
                if (gameRecord.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecord.getFirstProxy()));
                }
                if (gameRecord.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecord.getSecondProxy()));
                }
                if (gameRecord.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecord.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
                    list.add(
                        cb.between(root.get("betTime").as(String.class), startTime, endTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
    /**
     * 查询条件拼接，灵活添加条件
     * @param
     * @return
     */
    private Specification<GameRecord> getCondition(GameRecord gameRecord,String startBetTime,String endBetTime,String startSetTime,String endSetTime) {
        Specification<GameRecord> specification = new Specification<GameRecord>() {
            @Override
            public Predicate toPredicate(Root<GameRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();
                List<Predicate> list = new ArrayList<Predicate>();
                if (gameRecord.getId() != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("id").as(Long.class), gameRecord.getId()));
                }
                if (!CommonUtil.checkNull(gameRecord.getUser())) {
                    list.add(cb.equal(root.get("user").as(String.class), gameRecord.getUser()));
                }
                if (!CommonUtil.checkNull(gameRecord.getBetId())) {
                    list.add(cb.equal(root.get("betId").as(String.class), gameRecord.getBetId()));
                }
                if (!CommonUtil.checkNull(gameRecord.getGname())) {
                    list.add(cb.equal(root.get("gname").as(String.class), gameRecord.getGname()));
                }
                if (gameRecord.getGid() != null) {
                    list.add(cb.equal(root.get("gid").as(Integer.class), gameRecord.getGid()));
                }
                if (gameRecord.getUserId() != null) {
                    list.add(cb.equal(root.get("userId").as(Long.class), gameRecord.getUserId()));
                }
                if (gameRecord.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), gameRecord.getFirstProxy()));
                }
                if (gameRecord.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), gameRecord.getSecondProxy()));
                }
                if (gameRecord.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), gameRecord.getThirdProxy()));
                }
                if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
                    list.add(
                        cb.between(root.get("betTime").as(String.class), startBetTime, endBetTime)
                    );
                }
                if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
                    list.add(
                        cb.between(root.get("settime").as(String.class), startSetTime, endSetTime)
                    );
                }
                predicate = cb.and(list.toArray(new Predicate[list.size()]));
                return predicate;
            }
        };
        return specification;
    }
    public List<CompanyOrderAmountVo> getStatisticsResult(String startTime, String endTime){
        List<Map<String,Object>> orderAmountVoList = gameRecordRepository.getStatisticsResult(startTime,endTime);
        // String json = JSON.toJSONString(orderAmountVoList);
        // return JSON.parseArray(json,CompanyOrderAmountVo.class);
        return DTOUtil.map2DTO(orderAmountVoList, CompanyOrderAmountVo.class);
    }

    public List<GameRecord> findGameRecordIdAll(List<Long> recordIdList) {
        return gameRecordRepository.findAllById(recordIdList);
    }
    public  GameRecord  findRecordRecordSum(Long userId,String startTime,String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecord> query = builder.createQuery(GameRecord.class);
        Root<GameRecord> root = query.from(GameRecord.class);

        query.multiselect(
            builder.sum(root.get("validbet").as(BigDecimal.class)).alias("validbet")
        );

        List<Predicate> predicates = new ArrayList();
        if (userId != null) {
            predicates.add(
                builder.equal(root.get("userId").as(Long.class), userId)
            );
        }
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(
                builder.between(root.get("betTime").as(String.class), startTime, endTime)
            );
        }
        query
            .where(predicates.toArray(new Predicate[predicates.size()]));
        //                .groupBy(root.get("conversionStepCode"))
        //                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecord singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
    public  GameRecord  findRecordRecordSum(String startTime,String endTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecord> query = builder.createQuery(GameRecord.class);
        Root<GameRecord> root = query.from(GameRecord.class);

        query.multiselect(
            builder.sum(root.get("bet").as(BigDecimal.class)).alias("bet"),
            builder.sum(root.get("winLoss").as(BigDecimal.class)).alias("winLoss")
        );

        List<Predicate> predicates = new ArrayList();
        if (!ObjectUtils.isEmpty(startTime) && !ObjectUtils.isEmpty(endTime)) {
            predicates.add(
                builder.between(root.get("betTime").as(String.class), startTime, endTime)
            );
        }
        query
            .where(predicates.toArray(new Predicate[predicates.size()]));
        //                .groupBy(root.get("conversionStepCode"))
        //                .orderBy(builder.desc(root.get("contactUserNums")));
        GameRecord singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public void updateCodeNumStatus(Long id,Integer codeNumStatus){
        gameRecordRepository.updateCodeNumStatus(id,codeNumStatus);
    }

    public void updateWashCodeStatus(Long id,Integer washCodeStatus){
        gameRecordRepository.updateWashCodeStatus(id,washCodeStatus);
    }

    public void updateProfitStatus(Long id,Integer washCodeStatus){
        gameRecordRepository.updateProfitStatus(id,washCodeStatus);
    }

    public void updateRebateStatus(Long id,Integer rebateStatus){
        gameRecordRepository.updateRebateStatus(id,rebateStatus);
    }

    public void updateGameRecordStatus(Long id, Integer status){
        gameRecordRepository.updateGameRecordStatus(id,status);
    }

    public List<GameRecord> findByCreateByAndIdGreaterThanEqualOrderByIdAsc(String createBy,Long id) {
        return gameRecordRepository.findByCreateByAndIdGreaterThanEqualOrderByIdAsc(createBy,id);
    }

    public GameRecord findByBetId(String betId) {
        return gameRecordRepository.findByBetId(betId);
    }

    public Long findMaxId(){
        return gameRecordRepository.findMaxId();
    }

    public List<GameRecord> findGameRecord(GameRecord gameRecord,String startTime,String endTime)  {
        Specification<GameRecord> condition = getConditionGameRecord(gameRecord,startTime,endTime);

        return gameRecordRepository.findAll(condition);
    }

    private Specification<GameRecord> getConditionGameRecord(GameRecord gameRecord, String startTime, String endTime) {
        Specification<GameRecord> specification = new Specification<GameRecord>() {
            @Override
            public Predicate toPredicate(Root<GameRecord> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
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

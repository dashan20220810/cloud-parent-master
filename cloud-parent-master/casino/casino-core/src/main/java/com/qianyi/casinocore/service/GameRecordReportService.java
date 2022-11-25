//package com.qianyi.casinocore.service;
//
//import com.qianyi.casinocore.model.GameRecordReport;
//import com.qianyi.casinocore.model.ProxyUser;
//import com.qianyi.casinocore.repository.GameRecordReportRepository;
//import com.qianyi.casinocore.util.CommonConst;
//import com.qianyi.modulecommon.util.CommonUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.util.ObjectUtils;
//
//import javax.persistence.EntityManager;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class GameRecordReportService {
//
//    @Autowired
//    private EntityManager entityManager;
//    @Autowired
//    private GameRecordReportRepository gameRecordReportRepository;
//
//    public GameRecordReport save(GameRecordReport gameRecordReport) {
//        return gameRecordReportRepository.save(gameRecordReport);
//    }
//
//    public List<GameRecordReport> findByStaticsTimes(String staticsTimes){
//        return gameRecordReportRepository.findByStaticsTimes(staticsTimes);
//    }
//
//    /**
//       * 分组分页
//      */
//    public Page<GameRecordReport> findGameRecordReportPage(Pageable page, GameRecordReport gameRecordReport, String startSetTime, String endSetTime,Long proxyId,Integer proxyRole){
//        //criteriaBuilder用于构建CriteriaQuery的构建器对象
//        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//        //criteriaQuery包含查询语句的各个部分，如where、max、sum、groupBy、orderBy等
//        CriteriaQuery<GameRecordReport> criteriaQuery = criteriaBuilder.createQuery(GameRecordReport.class);
//        //获取查询实例的属性，select * from books
//        Root<GameRecordReport> root = criteriaQuery.from(GameRecordReport.class);
//        List<Predicate> predicates = new ArrayList();
//        //相当于select type,max(price) maxPrice,sum(price) sumPrice from books中select 与 from之间的部分
//        if (proxyRole==null){
//            criteriaQuery.multiselect(root.get("firstProxy"), criteriaBuilder.sum(root.get("bettingNumber")), criteriaBuilder.sum(root.get("amount")),
//                    criteriaBuilder.sum(root.get("betAmount")),criteriaBuilder.sum(root.get("validAmount")),criteriaBuilder.sum(root.get("winLossAmount")));
//        }else if (proxyRole== CommonConst.NUMBER_1){
//            criteriaQuery.multiselect(root.get("firstProxy"), criteriaBuilder.sum(root.get("bettingNumber")), criteriaBuilder.sum(root.get("amount")),
//                    criteriaBuilder.sum(root.get("betAmount")),criteriaBuilder.sum(root.get("validAmount")),criteriaBuilder.sum(root.get("winLossAmount")));
//            predicates.add(
//                    criteriaBuilder.equal(root.get("firstProxy").as(Long.class), proxyId)
//            );
//        }else if (proxyRole== CommonConst.NUMBER_2){
//            criteriaQuery.multiselect(root.get("secondProxy"), criteriaBuilder.sum(root.get("bettingNumber")), criteriaBuilder.sum(root.get("amount")),
//                    criteriaBuilder.sum(root.get("betAmount")),criteriaBuilder.sum(root.get("validAmount")),criteriaBuilder.sum(root.get("winLossAmount")));
//            predicates.add(
//                    criteriaBuilder.equal(root.get("secondProxy").as(Long.class), proxyId)
//            );
//        }else {
//            criteriaQuery.multiselect(root.get("thirdProxy"), criteriaBuilder.sum(root.get("bettingNumber")), criteriaBuilder.sum(root.get("amount")),
//                    criteriaBuilder.sum(root.get("betAmount")),criteriaBuilder.sum(root.get("validAmount")),criteriaBuilder.sum(root.get("winLossAmount")));
//            predicates.add(
//                    criteriaBuilder.equal(root.get("thirdProxy").as(Long.class), proxyId)
//            );
//        }
//
//        if (gameRecordReport.getGid() != null) {
//            predicates.add(
//                    criteriaBuilder.equal(root.get("gid").as(Integer.class), gameRecordReport.getGid())
//            );
//        }
//        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
//            predicates.add(
//                    criteriaBuilder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime)
//            );
//        }
//        predicates.add(
//                criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L)
//        );
//        //group by type
//        criteriaQuery.groupBy(root.get("firstProxy"));
//        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
//        //criteriaQuery拼成的sql是select type,max(price) maxPrice,sum(price) sumPrice from books group by type；查询出的列与对象BookInfo的属性对应
//        //记录当前sql查询结果总条数
//        List<GameRecordReport> counts = entityManager.createQuery(criteriaQuery).getResultList();
//        //sql查询对象
//        TypedQuery<GameRecordReport> createQuery = entityManager.createQuery(criteriaQuery);
//        //设置分页参数
//        createQuery.setFirstResult(page.getPageNumber()*page.getPageSize());
//        createQuery.setMaxResults(page.getPageSize());
//        //返回查询的分页结果，createQuery.getResultList()为分页查询的结果对象，counts.size()为设置分页参数之前查询的总数
//        return new PageImpl<GameRecordReport>(createQuery.getResultList(), page, counts.size());
//    }
//
//    public  GameRecordReport  findRecordRecordSum(GameRecordReport gameRecordReport,String startSetTime,String endSetTime,Long proxyId,Integer proxyRole) {
//        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<GameRecordReport> query = builder.createQuery(GameRecordReport.class);
//        Root<GameRecordReport> root = query.from(GameRecordReport.class);
//
//        query.multiselect(
//                builder.sum(root.get("bettingNumber").as(Integer.class)).alias("bettingNumber"),
//                builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
//                builder.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
//                builder.sum(root.get("validAmount").as(BigDecimal.class)).alias("validAmount"),
//                builder.sum(root.get("winLossAmount").as(BigDecimal.class)).alias("winLossAmount")
//        );
//
//        List<Predicate> predicates = new ArrayList();
//        if (proxyRole != null && proxyRole == CommonConst.NUMBER_1){
//            predicates.add(
//                    builder.equal(root.get("firstProxy").as(Long.class), proxyId)
//            );
//        }else if (proxyRole != null && proxyRole == CommonConst.NUMBER_2){
//            predicates.add(
//                    builder.equal(root.get("secondProxy").as(Long.class), proxyId)
//            );
//        }else if (proxyRole != null && proxyRole == CommonConst.NUMBER_3){
//            predicates.add(
//                    builder.equal(root.get("thirdProxy").as(Long.class), proxyId)
//            );
//        }
//
//        if (gameRecordReport.getGid() != null) {
//            predicates.add(
//                    builder.equal(root.get("gid").as(Integer.class), gameRecordReport.getGid())
//            );
//        }
//        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
//            predicates.add(
//                    builder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime)
//            );
//        }
//        predicates.add(
//                builder.notEqual(root.get("firstProxy").as(Long.class), 0L)
//        );
//        query
//                .where(predicates.toArray(new Predicate[predicates.size()]));
//        GameRecordReport singleResult = entityManager.createQuery(query).getSingleResult();
//        return singleResult;
//    }
//
//    public  GameRecordReport  findRecordRecordSum(GameRecordReport gameRecordReport,String startSetTime,String endSetTime) {
//        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<GameRecordReport> query = builder.createQuery(GameRecordReport.class);
//        Root<GameRecordReport> root = query.from(GameRecordReport.class);
//
//        query.multiselect(
//                builder.sum(root.get("bettingNumber").as(Integer.class)).alias("bettingNumber"),
//                builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
//                builder.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
//                builder.sum(root.get("validAmount").as(BigDecimal.class)).alias("validAmount"),
//                builder.sum(root.get("winLossAmount").as(BigDecimal.class)).alias("winLossAmount")
//        );
//
//        List<Predicate> predicates = new ArrayList();
//        if (gameRecordReport.getGid() != null) {
//            predicates.add(
//                    builder.equal(root.get("gid").as(Integer.class), gameRecordReport.getGid())
//            );
//        }
//        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
//            predicates.add(
//                    builder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime)
//            );
//        }
//        predicates.add(
//                builder.equal(root.get("firstProxy").as(Long.class), 0L)
//        );
//        query
//                .where(predicates.toArray(new Predicate[predicates.size()]));
//        GameRecordReport singleResult = entityManager.createQuery(query).getSingleResult();
//        return singleResult;
//    }
//}

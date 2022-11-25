package com.qianyi.casinocore.service;

import com.mysql.cj.util.StringUtils;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserRepository;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.util.RebateSqlConst;
import com.qianyi.casinocore.util.SqlConst;
import com.qianyi.casinocore.vo.PersonReportVo;
import com.qianyi.casinocore.vo.RebateReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.text.MessageFormat;
import java.util.*;

@Service
@Transactional
@CacheConfig(cacheNames = {"user"})
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public User findByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public User findOne(User user) {
        Specification<User> condition = this.getCondition(user,null,null);
        Optional<User> optional = userRepository.findOne(condition);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @CachePut(key="#result.id",condition = "#result != null")
    public User save(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(key="#id")
    public void updatePassword(Long id, String password) {
        userRepository.updatePassword(id, password);
    }

    public List<User> saveAll(List<User> userList){
        return userRepository.saveAll(userList);
    }

    @Cacheable(key = "#id")
    public User findById(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @CacheEvict(key="#id")
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }


    public Integer countByIp(String ip) {
        return userRepository.countByRegisterIp(ip);
    }

    public User findUserByIdUseLock(Long userId) {
        return userRepository.findUserByUserIdUseLock(userId);
    }

    public User findUserByUserIdUse(Long userId) {
        return userRepository.findUserByUserIdUse(userId);
    }
    public void updateIsFirstBet(Long userId,Integer isFirstBet){
        userRepository.updateIsFirstBet(userId,isFirstBet);
    }

    /**
     * 用户列表查询
     *
     * @param user
     * @return
     */
    public Page<User> findUserPage(Pageable pageable, User user, Date startDate,Date endDate) {
        Specification<User> condition = this.getCondition(user,startDate,endDate);
        return userRepository.findAll(condition, pageable);
    }

    public List<User> findUserList(User user,Date startDate,Date endDate) {
        Specification<User> condition = this.getCondition(user,startDate,endDate);
        return userRepository.findAll(condition);
    }

    public Long findUserCount(User user,Date startDate,Date endDate) {
        Specification<User> condition = this.getCondition(user,startDate,endDate);
        return userRepository.count(condition);
    }
    /**
     * 查询条件拼接，灵活添加条件
     *
     * @param user
     * @return
     */
    private Specification<User> getCondition(User user,Date startDate,Date endDate) {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if (!CommonUtil.checkNull(user.getAccount())) {
                    list.add(cb.equal(root.get("account").as(String.class), user.getAccount()));
                }
                if (!CommonUtil.checkNull(user.getRegisterIp())) {
                    list.add(cb.equal(root.get("registerIp").as(String.class), user.getRegisterIp()));
                }
                if (user.getState() != null) {
                    list.add(cb.equal(root.get("state").as(String.class), user.getState()));
                }
                if (user.getRegisterDomainName() != null) {
                    list.add(cb.equal(root.get("registerDomainName").as(String.class), user.getRegisterDomainName()));
                }
                if (user.getId() != null) {
                    list.add(cb.equal(root.get("id").as(Long.class), user.getId()));
                }
                if (user.getFirstProxy() != null) {
                    list.add(cb.equal(root.get("firstProxy").as(Long.class), user.getFirstProxy()));
                }
                if (user.getSecondProxy() != null) {
                    list.add(cb.equal(root.get("secondProxy").as(Long.class), user.getSecondProxy()));
                }
                if (user.getThirdProxy() != null) {
                    list.add(cb.equal(root.get("thirdProxy").as(Long.class), user.getThirdProxy()));
                }
                if (startDate != null) {
                    list.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (endDate != null) {
                    list.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class),endDate));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<User> findAll(List<Long> userIds) {
        Specification<User> condition = getCondition(userIds);
        List<User> userList = userRepository.findAll(condition);
        return userList;
    }

    private Specification<User> getCondition(List<Long> userIds) {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("id");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public List<User> findByRegisterIp(String ip) {
        return userRepository.findByRegisterIp(ip);
    }

    public User findByInviteCode(String inviteCode) {
        return userRepository.findByInviteCode(inviteCode);
    }

    public List<User> findByStateAndFirstPid(Integer state,Long firstPid){
        return userRepository.findByStateAndFirstPid(state,firstPid);
    }

    public List<User> findByThirdPid(Long id) {
        return userRepository.findByThirdPid(id);
    }

    public List<User> findBySecondPid(Long id) {
        return userRepository.findBySecondPid(id);
    }

    public List<User> findFirstUser(Long id) {
        return userRepository.findByFirstPid(id);
    }


    public List<User> findFirstUserList(List<Long> userIds) {
        Specification<User> condition = getConditionFirstPid(userIds);
        List<User> userList = userRepository.findAll(condition);
        return userList;
    }

    private Specification<User> getConditionFirstPid(List<Long> userIds) {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("firstPid");
                    CriteriaBuilder.In<Object> in = cb.in(userId);
                    for (Long id : userIds) {
                        in.value(id);
                    }
                    list.add(cb.and(cb.and(in)));
                }
                return cb.and(list.toArray(new Predicate[list.size()]));
            }
        };
        return specification;
    }

    public Integer countByFirstPid(Long userId) {
        return userRepository.countByFirstPid(userId);
    }

    /**
     * 正常情况一个手机号只能注册一个号，这里是为了兼容历史数据，防止查询报错
     * @param phone
     * @return
     */
    public List<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User findByFirstPidAndAccount(Long userId,String account) {
        return userRepository.findByFirstPidAndAccount(userId,account);
    }

    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMap(String platform,String startTime,String endTime,Integer page,Integer pageSize,String sort,String orderTimeStart,String orderTimeEnd,String proxy)
        throws Exception {
        startTime = "'"+startTime+"'";
        endTime = "'"+endTime+"'";
        String sql = "";
        //        if(StringUtils.isNullOrEmpty(platform)){
        //            sql = MessageFormat.format(SqlConst.totalSql, startTime, endTime, sort, page.toString(), pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);
        //        }else if (platform.equals("WM")){
        //            sql = MessageFormat.format(SqlConst.wmSql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'wm'",proxy);
        //        }else{
        //            sql = MessageFormat.format(SqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(),"'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        //        }
        if(StringUtils.isNullOrEmpty(platform)){
            //            sql = MessageFormat.format(SqlConst.totalSqlReport, startTime, endTime, sort, page.toString(), pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);//走报表
            sql = MessageFormat.format(SqlConst.totalSql, startTime, endTime, sort, page.toString(), pageSize.toString(),proxy);
        }else if (platform.equals(Constants.PLATFORM_WM_BIG)){
            sql = MessageFormat.format(SqlConst.wmSql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'wm'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBDJ)){
            sql = MessageFormat.format(SqlConst.obdjSql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'OBDJ'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBTY)){
            sql = MessageFormat.format(SqlConst.obtySql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'OBTY'",proxy);
        }else if (platform.equals(Constants.PLATFORM_PG)){
            sql = MessageFormat.format(SqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'PG'",proxy);
        }else if (platform.equals(Constants.PLATFORM_SABASPORT)){
            sql = MessageFormat.format(SqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'SABASPORT'",proxy);
        }else{
            sql = MessageFormat.format(SqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'CQ9'",proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }
    @SuppressWarnings("unchecked")
    public List<PersonReportVo> findMap(String platform, String startTime, String endTime, Long userId,String orderTimeStart,String orderTimeEnd,String proxy){
        startTime = "'"+startTime+"'";
        endTime = "'"+endTime+"'";
        String sql = "";
        //        if(StringUtils.isNullOrEmpty(platform)){
        //            sql = MessageFormat.format(SqlConst.seleOneTotal,startTime,endTime,userId.toString(),orderTimeStart,orderTimeEnd,proxy);
        //        }else if (platform.equals("WM")){
        //            sql = MessageFormat.format(SqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'",proxy);
        //        }else{
        //            sql = MessageFormat.format(SqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(),"'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        //        }
        if(StringUtils.isNullOrEmpty(platform)){
            //走报表
            //            sql = MessageFormat.format(SqlConst.seleOneTotalReport,startTime,endTime,userId.toString(),orderTimeStart,orderTimeEnd,proxy);
            sql = MessageFormat.format(SqlConst.seleOneTotal,startTime,endTime,userId.toString(),proxy);
        }else if (platform.equals(Constants.PLATFORM_WM_BIG)){
            sql = MessageFormat.format(SqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBDJ)){
            sql = MessageFormat.format(SqlConst.seleOneObdj,startTime, endTime, userId.toString(), "'OBDJ'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBTY)){
            sql = MessageFormat.format(SqlConst.seleOneObty,startTime, endTime, userId.toString(), "'OBTY'",proxy);
        }else if (platform.equals(Constants.PLATFORM_PG)){
            sql = MessageFormat.format(SqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(), "'PG'",proxy);
        }else if (platform.equals(Constants.PLATFORM_SABASPORT)){
            sql = MessageFormat.format(SqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(), "'SABASPORT'",proxy);
        }else{
            sql = MessageFormat.format(SqlConst.seleOnePgOrCq9Sql,startTime,endTime,userId.toString(),"'CQ9'",proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parsePersonReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, PersonReportVo.class);
    }

    private static final List<String> PERSON_REPORT_TOTAL_FIELD_LIST = Arrays.asList(
        "num",
        "bet_amount",
        "validbet",
        "win_loss",
        "wash_amount",
        "service_charge",
        "all_profit_amount",
        "avg_benefit",
        "total_amount",
        "all_water"
    );

    @SuppressWarnings("unchecked")
    public Map<String,Object> findMap(String platform,String startTime,String endTime,String orderTimeStart,String orderTimeEnd,String proxy){
        startTime = "'"+startTime+"'";
        endTime = "'"+endTime+"'";
        String sql = "";
        //        if(StringUtils.isNullOrEmpty(platform)){
        //            sql = MessageFormat.format(SqlConst.sumSql,startTime,endTime,orderTimeStart,orderTimeEnd, proxy);
        //        }else if (platform.equals("WM")){
        //            sql = MessageFormat.format(SqlConst.WMSumSql,startTime, endTime, "'wm'", proxy);
        //        }else{
        //            sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql,startTime, endTime,"'"+platform+"'",orderTimeStart,orderTimeEnd, proxy);
        //        }
        if(StringUtils.isNullOrEmpty(platform)){
            //走报表
            //            sql = MessageFormat.format(SqlConst.sumSqlReport,startTime,endTime,orderTimeStart,orderTimeEnd, proxy);
            sql = MessageFormat.format(SqlConst.sumSql,startTime,endTime, proxy);
        }else if (platform.equals(Constants.PLATFORM_WM_BIG)){
            sql = MessageFormat.format(SqlConst.WMSumSql,startTime, endTime, "'wm'", proxy);
        }else if (platform.equals(Constants.PLATFORM_OBDJ)){
            sql = MessageFormat.format(SqlConst.obdjSumSql,startTime, endTime, "'OBDJ'", proxy);
        }else if (platform.equals(Constants.PLATFORM_OBTY)){
            sql = MessageFormat.format(SqlConst.obtySumSql,startTime, endTime, "'OBTY'", proxy);
        }else if (platform.equals(Constants.PLATFORM_PG)){
            sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql,startTime, endTime, "'PG'", proxy);
        }else if (platform.equals(Constants.PLATFORM_SABASPORT)){
            sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql,startTime, endTime, "'SABASPORT'", proxy);
        }else{
            sql = MessageFormat.format(SqlConst.PGAndCQ9SumSql,startTime, endTime, "'CQ9'", proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String,Object> map = new HashMap<>();
        Object[] obj = (Object[]) result;
        for (int i=0; i< PERSON_REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = PERSON_REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }

        return map;
    }

    private static final List<String> PERSON_REPORT_VO_FIELD_LIST = Arrays.asList(
        "account",
        "third_proxy",
        "id",
        "num",
        "bet_amount",
        "validbet",
        "win_loss",
        "wash_amount",
        "service_charge",
        "all_profit_amount",
        "avg_benefit",
        "total_amount",
        "all_water"
    );

    private List<Map<String,Object>> parsePersonReportMapList(List<Object> resultList) {
        List<Map<String,Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0){
            list = new LinkedList<>();

            for (Object result:resultList){
                Map<String,Object> map = new HashMap<>();
                Object[] obj = (Object[]) result;
                for (int i=0; i< PERSON_REPORT_VO_FIELD_LIST.size(); i++) {
                    String field = PERSON_REPORT_VO_FIELD_LIST.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                list.add(map);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<RebateReportVo> findRebateMap(String platform,String startTime,String endTime,Integer page,Integer pageSize,String sort,String orderTimeStart,String orderTimeEnd,String proxy)
        throws Exception {
        startTime = "'"+startTime+"'";
        endTime = "'"+endTime+"'";
        String sql = "";
        //        if(StringUtils.isNullOrEmpty(platform)){
        //            sql = MessageFormat.format(RebateSqlConst.totalSql, startTime, endTime, sort, page.toString(), pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);
        //        }else if (platform.equals("WM")){
        //            sql = MessageFormat.format(RebateSqlConst.wmSql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'wm'",proxy);
        //        }else{
        //            sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        //        }
        if(StringUtils.isNullOrEmpty(platform)){
            //            sql = MessageFormat.format(RebateSqlConst.totalSqlReport, startTime, endTime, sort, page.toString(), pageSize.toString(),orderTimeStart,orderTimeEnd,proxy);
            sql = MessageFormat.format(RebateSqlConst.totalSql, startTime, endTime, sort, page.toString(), pageSize.toString(),proxy);
        }else if (platform.equals(Constants.PLATFORM_WM_BIG)){
            sql = MessageFormat.format(RebateSqlConst.wmSql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'wm'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBDJ)){
            sql = MessageFormat.format(RebateSqlConst.obdjSql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'OBDJ'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBTY)){
            sql = MessageFormat.format(RebateSqlConst.obtySql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'OBTY'",proxy);
        }else if (platform.equals(Constants.PLATFORM_PG)){
            sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'PG'",proxy);
        }else if (platform.equals(Constants.PLATFORM_SABASPORT)){
            sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'SABASPORT'",proxy);
        }else{
            sql = MessageFormat.format(RebateSqlConst.pgOrCq9Sql,startTime, endTime, sort, page.toString(), pageSize.toString(), "'CQ9'",proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parseRebateReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, RebateReportVo.class);
    }
    @SuppressWarnings("unchecked")
    public List<RebateReportVo> findRebateMap(String platform, String startTime, String endTime, Long userId,String orderTimeStart,String orderTimeEnd,String proxy){
        startTime = "'"+startTime+"'";
        endTime = "'"+endTime+"'";
        String sql = "";
        //        if(StringUtils.isNullOrEmpty(platform)){
        //            sql = MessageFormat.format(RebateSqlConst.seleOneTotal,startTime,endTime,userId.toString(),orderTimeStart,orderTimeEnd,proxy);
        //        }else if (platform.equals("WM")){
        //            sql = MessageFormat.format(RebateSqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'",proxy);
        //        }else{
        //            sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(),  "'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        //        }
        if(StringUtils.isNullOrEmpty(platform)){
            //            sql = MessageFormat.format(RebateSqlConst.seleOneTotalReport,startTime,endTime,userId.toString(),orderTimeStart,orderTimeEnd,proxy);
            sql = MessageFormat.format(RebateSqlConst.seleOneTotal,startTime,endTime,userId.toString(),proxy);
        }else if (platform.equals(Constants.PLATFORM_WM_BIG)){
            sql = MessageFormat.format(RebateSqlConst.seleOneWm, startTime, endTime, userId.toString(), "'wm'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBDJ)){
            sql = MessageFormat.format(RebateSqlConst.seleOneObdj, startTime, endTime, userId.toString(), "'OBDJ'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBTY)){
            sql = MessageFormat.format(RebateSqlConst.seleOneObty, startTime, endTime, userId.toString(), "'OBTY'",proxy);
        }else if (platform.equals(Constants.PLATFORM_PG)){
            sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(), "'PG'",proxy);
        }else if (platform.equals(Constants.PLATFORM_SABASPORT)){
            sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql,startTime, endTime, userId.toString(), "'SABASPORT'",proxy);
        }else{
            sql = MessageFormat.format(RebateSqlConst.seleOnePgOrCq9Sql,startTime,endTime,userId.toString(),"'CQ9'",proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        List<Object> resultList = countQuery.getResultList();
        List<Map<String, Object>> mapList = parseRebateReportMapList(resultList);
        return DTOUtil.map2DTO(mapList, RebateReportVo.class);
    }

    private static final List<String> REBATE_REPORT_TOTAL_FIELD_LIST = Arrays.asList(
        "num",
        "bet_amount",
        "validbet",
        "win_loss",
        "total_rebate",
        "user_amount",
        "surplus_amount",
        "service_charge",
        "avg_benefit",
        "total_amount"
    );

    @SuppressWarnings("unchecked")
    public Map<String,Object> findRebateMap(String platform,String startTime,String endTime,String orderTimeStart,String orderTimeEnd,String proxy){
        startTime = "'"+startTime+"'";
        endTime = "'"+endTime+"'";
        String sql = "";
        //        if(StringUtils.isNullOrEmpty(platform)){
        //            sql = MessageFormat.format(RebateSqlConst.sumSql,startTime,endTime,orderTimeStart,orderTimeEnd,proxy);
        //        }else if (platform.equals("WM")){
        //            sql = MessageFormat.format(RebateSqlConst.WMSumSql,startTime, endTime, "'wm'",proxy);
        //        }else{
        //            sql = MessageFormat.format(RebateSqlConst.PGAndCQ9SumSql,startTime, endTime,  "'"+platform+"'",orderTimeStart,orderTimeEnd,proxy);
        //        }
        if(StringUtils.isNullOrEmpty(platform)){
            //            sql = MessageFormat.format(RebateSqlConst.sumSqlReport,startTime,endTime,orderTimeStart,orderTimeEnd,proxy);
            sql = MessageFormat.format(RebateSqlConst.sumSql,startTime,endTime,proxy);
        }else if (platform.equals(Constants.PLATFORM_WM_BIG)){
            sql = MessageFormat.format(RebateSqlConst.WMSumSql,startTime, endTime, "'wm'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBDJ)){
            sql = MessageFormat.format(RebateSqlConst.obdjSumSql,startTime, endTime, "'OBDJ'",proxy);
        }else if (platform.equals(Constants.PLATFORM_OBTY)){
            sql = MessageFormat.format(RebateSqlConst.obtySumSql,startTime, endTime, "'OBTY'",proxy);
        }else if (platform.equals(Constants.PLATFORM_PG)){
            sql = MessageFormat.format(RebateSqlConst.PGAndCQ9SumSql,startTime, endTime, "'PG'",proxy);
        }else if (platform.equals(Constants.PLATFORM_SABASPORT)){
            sql = MessageFormat.format(RebateSqlConst.PGAndCQ9SumSql,startTime, endTime, "'SABASPORT'",proxy);
        }else{
            sql = MessageFormat.format(RebateSqlConst.PGAndCQ9SumSql,startTime, endTime, "'CQ9'",proxy);
        }
        log.info(sql);
        Query countQuery = entityManager.createNativeQuery(sql);
        Object result = countQuery.getSingleResult();
        Map<String,Object> map = new HashMap<>();
        Object[] obj = (Object[]) result;
        for (int i=0; i< REBATE_REPORT_TOTAL_FIELD_LIST.size(); i++) {
            String field = REBATE_REPORT_TOTAL_FIELD_LIST.get(i);
            Object value = obj[i];
            map.put(field, value);
        }

        return map;
    }

    private static final List<String> REBATE_REPORT_VO_FIELD_LIST = Arrays.asList(
        "account",
        "third_proxy",
        "id",
        "num",
        "bet_amount",
        "validbet",
        "win_loss",
        "total_rebate",
        "user_amount",
        "surplus_amount",
        "service_charge",
        "avg_benefit",
        "total_amount"
    );

    private List<Map<String,Object>> parseRebateReportMapList(List<Object> resultList) {
        List<Map<String,Object>> list = null;
        if (resultList != null && resultList.size() > CommonConst.NUMBER_0){
            list = new LinkedList<>();

            for (Object result:resultList){
                Map<String,Object> map = new HashMap<>();
                Object[] obj = (Object[]) result;
                for (int i=0; i< REBATE_REPORT_VO_FIELD_LIST.size(); i++) {
                    String field = REBATE_REPORT_VO_FIELD_LIST.get(i);
                    Object value = obj[i];
                    map.put(field, value);
                }
                list.add(map);
            }
        }
        return list;
    }

    public Set<Long> findUserByRegisterDomainName(String registerDomainName,String startTime,String endTime){
        return userRepository.findUserByRegisterDomainName(registerDomainName,startTime,endTime);
    }

    public List<User> findByAccountUpper(String account) {
        return userRepository.findByAccountUpper(account);
    }
}

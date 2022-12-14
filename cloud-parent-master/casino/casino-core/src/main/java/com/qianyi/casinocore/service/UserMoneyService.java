package com.qianyi.casinocore.service;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.exception.UserMoneyChangeException;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.UserThird;
import com.qianyi.casinocore.repository.UserMoneyRepository;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.HttpClient4Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@CacheConfig(cacheNames = {"userMoney"})
public class UserMoneyService {

    @Autowired
    private UserMoneyRepository userMoneyRepository;


    @Autowired
    private UserService userService;
    @Autowired
    private PlatformConfigService platformConfigService;

    private String refreshUrl = "/wm/getWmBalanceApi?";

    private String recycleUrl = "/wm/oneKeyRecoverApi?";

    private String PG_refreshUrl = "/golednf/getBalanceApi?";

    private String PG_recycleUrl = "/golednf/oneKeyRecoverApi?";

    private String OB_refreshUrl = "/obdjGame/getBalanceApi?";

    private String OB_recycleUrl = "/obdjGame/oneKeyRecoverApi?";

    private String OBTY_refreshUrl = "/obtyGame/getBalanceApi?";

    private String OBTY_recycleUrl = "/obtyGame/oneKeyRecoverApi?";

    public UserMoney findUserByUserIdUseLock(Long userId) {
        //return userMoneyRepository.findUserByUserIdUseLock(userId);
        return userMoneyRepository.findUserMoneyByUserId(userId);
    }

    public UserMoney findUserByUserIdUse(Long userId) {
        return userMoneyRepository.findByUserId(userId);
        // return userMoneyRepository.findUserMoneyByUserId(userId);
    }


    public List<UserMoney> saveAll(List<UserMoney> userMoneyList){
        return userMoneyRepository.saveAll(userMoneyList);
    }

    @CacheEvict(key = "#userId")
    /*    @Transactional*/
    public void changeProfit(Long userId,BigDecimal shareProfit){
        userMoneyRepository.changeProfit(userId,shareProfit);
    }

    /**
     *
     * @param userId ??????id
     * @param money ??????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subMoney(Long userId, BigDecimal money) {
        synchronized (userId) {
            UserMoney userMoneyLock = findUserByUserIdUseLock(userId);
            //??????????????????????????????
            if (money.compareTo(userMoneyLock.getMoney()) == 1) {
                throw new UserMoneyChangeException("????????????????????????????????????");
            }
            userMoneyRepository.subMoney(userId, money);
        }
    }

    public Page<UserMoney> findUserMoneyPage(Specification<UserMoney> condition, Pageable pageable){
        return userMoneyRepository.findAll(condition,pageable);
    }

    /**
     *
     * @param userId ??????id
     * @param money ??????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void addMoney(Long userId, BigDecimal money) {
        synchronized (userId) {
            userMoneyRepository.addMoney(userId, money);
        }
    }

    /**
     *
     * @param userId ??????id
     * @param codeNum ?????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            UserMoney userMoneyLock = findUserByUserIdUseLock(userId);
            //??????????????????????????????
            if (codeNum.compareTo(userMoneyLock.getCodeNum()) == 1) {
                throw new UserMoneyChangeException("???????????????????????????????????????");
            }
            userMoneyRepository.subCodeNum(userId, codeNum);
        }
    }

    /**
     * ??????????????????
     * @param userId ??????id
     * @param washCode ????????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void addWashCode(Long userId, BigDecimal washCode) {
        synchronized (userId) {
            userMoneyRepository.addWashCode(userId, washCode);
        }
    }


    /**
     * ??????????????????
     * @param userId ??????id
     * @param washCode ????????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subWashCode(Long userId, BigDecimal washCode) {
        synchronized (userId) {
            UserMoney userMoneyLock = findUserByUserIdUseLock(userId);
            //??????????????????????????????
            if (washCode.compareTo(userMoneyLock.getWashCode()) == 1) {
                throw new UserMoneyChangeException("???????????????????????????????????????");
            }
            userMoneyRepository.subWashCode(userId, washCode);
        }
    }

    @CacheEvict(key = "#userId")
    @Transactional
    public void addCodeNum(Long userId, BigDecimal codeNum) {
        synchronized (userId) {
            userMoneyRepository.addCodeNum(userId, codeNum);
        }
    }


    /**
     * ??????????????????
     * @param userId ??????id
     * @param shareProfit ????????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void addShareProfit(Long userId, BigDecimal shareProfit) {
        synchronized (userId) {
            userMoneyRepository.addShareProfit(userId, shareProfit);
        }
    }


    /**
     * ??????????????????
     * @param userId ??????id
     * @param shareProfit ????????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subShareProfit(Long userId, BigDecimal shareProfit) {
        synchronized (userId) {
            UserMoney userMoneyLock = findUserByUserIdUseLock(userId);
            //??????????????????????????????
            if (shareProfit.compareTo(userMoneyLock.getShareProfit()) == 1) {
                throw new UserMoneyChangeException("??????????????????????????????????????????");
            }
            userMoneyRepository.subShareProfit(userId, shareProfit);
        }
    }

    /**
     * ??????????????????
     * @param userId ??????id
     * @param balance ????????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void addBalance(Long userId, BigDecimal balance) {
        synchronized (userId) {
            userMoneyRepository.addBalance(userId, balance);
        }
    }


    /**
     * ??????????????????
     * @param userId ??????id
     * @param balance ????????????
     */
    @CacheEvict(key = "#userId")
    @Transactional
    public void subBalance(Long userId, BigDecimal balance) {
        synchronized (userId) {
            UserMoney userMoneyLock = findUserByUserIdUseLock(userId);
            //??????????????????????????????
            if (balance.compareTo(userMoneyLock.getBalance()) == 1) {
                throw new UserMoneyChangeException("??????????????????????????????????????????");
            }
            userMoneyRepository.subBalance(userId, balance);
        }
    }

    @Cacheable(key = "#userId")
    public UserMoney findByUserId(Long userId) {
        UserMoney userMoney = userMoneyRepository.findByUserId(userId);
        if (userMoney != null) {
            BigDecimal defaultVal = BigDecimal.ZERO;
            BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney();
            userMoney.setMoney(money);
            BigDecimal washCode = userMoney.getWashCode() == null ? defaultVal : userMoney.getWashCode();
            userMoney.setWashCode(washCode);
            BigDecimal codeNum = userMoney.getCodeNum() == null ? defaultVal : userMoney.getCodeNum();
            userMoney.setCodeNum(codeNum);
            BigDecimal freezeMoney = userMoney.getFreezeMoney() == null ? defaultVal : userMoney.getFreezeMoney();
            userMoney.setFreezeMoney(freezeMoney);
        }
        return userMoney;
    }

    @CachePut(key="#userMoney.userId")
    @Transactional
    public UserMoney save(UserMoney userMoney) {
        return userMoneyRepository.save(userMoney);
    }

    public List<UserMoney> findAll(List<Long> userIds) {
        Specification<UserMoney> condition = getCondition(userIds);
        List<UserMoney> userMoneyList = userMoneyRepository.findAll(condition);
        return userMoneyList;
    }

    private Specification<UserMoney> getCondition(List<Long> userIds) {
        Specification<UserMoney> specification = new Specification<UserMoney>() {
            @Override
            public Predicate toPredicate(Root<UserMoney> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
                if (userIds != null && userIds.size() > 0) {
                    Path<Object> userId = root.get("userId");
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
    public JSONObject getWMonetUser(User user, UserThird third) {
        Integer lang = user.getLanguage();
        if (lang == null) {
            lang = 0;
        }
        try {
            String param = "account={0}&lang={1}";
            param = MessageFormat.format(param,third.getAccount(),lang.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}??????web????????????{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
            //            Object data = parse.get("data");
            //            return new BigDecimal(data.toString());
        } catch (Exception e) {
            return null;
        }
    }
    public JSONObject getWMonetUser(UserThird third) {
        User byId = userService.findById(third.getUserId());
        Integer lang;
        if (byId == null){
            lang = 0;
        }else {
            lang = byId.getLanguage();
        }
        if (lang == null) {
            lang = 0;
        }
        try {
            String param = "account={0}&lang={1}";
            param = MessageFormat.format(param,third.getAccount(),lang.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("????????????:{}", e.getMessage());
            return null;
        }
    }

    public JSONObject getSABAonetUser(UserThird third) {

        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param, third.getUserId(), Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }


    public JSONObject oneKeyRecover(User user){
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}????????????web????????????{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ??????????????????
     * @param userId
     * @return
     */
    public JSONObject refreshOB(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + OB_refreshUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}??????OB??????web????????????{}",userId,s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshPGAndCQ9(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}??????PG??????web????????????{}",userId,s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshSABA(Long userId) {
        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param,userId.toString(), Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}??????PG??????web????????????{}",userId,s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeySABAApi(User user){
        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param,user.getId().toString(), Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}??????PG??????web????????????{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshPGAndCQ9UserId(String userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param, userId);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshSABAUserId(String userId) {
        try {
            String param = "userId={0}&vendorCode={1}";
            param = MessageFormat.format(param, userId, Constants.PLATFORM_SABASPORT);
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }


    public JSONObject oneKeyRecoverApi(User user){
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + PG_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}??????PG??????web????????????{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeyOBRecoverApi(User user){
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + OB_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}??????OB????????????web????????????{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject refreshOBTY(Long userId) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,userId.toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + OBTY_refreshUrl;
            String s = HttpClient4Util.get(WMurl + param);
            log.info("{}??????OB??????web????????????{}",userId,s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject oneKeyOBTYRecoverApi(User user) {
        try {
            String param = "userId={0}";
            param = MessageFormat.format(param,user.getId().toString());
            PlatformConfig first = platformConfigService.findFirst();
            String WMurl = first == null?"":first.getWebConfiguration();
            WMurl = WMurl + OBTY_recycleUrl;
            String s = HttpClient4Util.getWeb(WMurl + param);
            log.info("{}??????OB????????????web????????????{}",user.getAccount(),s);
            JSONObject parse = JSONObject.parseObject(s);
            return parse;
        } catch (Exception e) {
            return null;
        }
    }
}

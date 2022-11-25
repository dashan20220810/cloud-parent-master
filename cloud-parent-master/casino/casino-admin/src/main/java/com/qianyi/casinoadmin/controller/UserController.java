package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinocore.co.user.UserCleanMudCo;
import com.qianyi.casinocore.util.*;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.UserTotalVo;
import com.qianyi.casinocore.vo.UserVo;
import com.qianyi.casinocore.business.ChargeOrderBusiness;
import com.qianyi.casinocore.business.WithdrawBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.RegexEnum;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import com.qianyi.modulecommon.util.MessageUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 对用户表进行增删改查操作
 */
@RestController
@RequestMapping("user")
@Api(tags = "客户中心")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMoneyService userMoneyService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ChargeOrderBusiness chargeOrderBusiness;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private WithdrawBusiness withdrawBusiness;

    @Autowired
    UserThirdService userThirdService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    GenerateInviteCodeRunner generateInviteCodeRunner;
    @Autowired
    PlatformConfigService platformConfigService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MessageUtil messageUtil;

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_10);

    @ApiOperation("查询代理下级的用户数据")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("getProxyUser")
    public ResponseEntity<UserVo> getProxyUser(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }

        //查询直属代理会员
        List<User> firstUserList = userService.findFirstUser(id);
        List<Long> userIds = firstUserList.stream().map(User::getId).collect(Collectors.toList());
        if(userIds == null || userIds.size() == 0){
            List<UserMoney> userMoneyList =  userMoneyService.findAll(userIds);
            List<UserVo> userVoList = getUserVoList(firstUserList, userMoneyList);
            return ResponseUtil.success(userVoList);
        }

        List<User> secordUsersList = userService.findFirstUserList(userIds);
        secordUsersList.forEach(u -> firstUserList.add(u));

        List<Long> thridUserId = secordUsersList.stream().map(User::getId).collect(Collectors.toList());
        if(userIds == null || userIds.size() == 0){
            List<Long> ids = firstUserList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> userMoneyList =  userMoneyService.findAll(ids);
            List<UserVo> userVoList = getUserVoList(firstUserList, userMoneyList);
            return ResponseUtil.success(userVoList);
        }

        List<User> thridUsersList = userService.findFirstUserList(thridUserId);
        thridUsersList.forEach(u -> firstUserList.add(u));

        List<Long> ids = firstUserList.stream().map(User::getId).collect(Collectors.toList());
        List<UserMoney> userMoneyList =  userMoneyService.findAll(ids);
        List<UserVo> userVoList = getUserVoList(firstUserList, userMoneyList);
        return ResponseUtil.success(userVoList);
    }

    private List<UserVo> getUserVoList(List<User> firstUserList, List<UserMoney> userMoneyList) {
        List<UserVo> userVoList = new ArrayList<>();
        for (User user : firstUserList) {
            UserVo userVo = new UserVo(user);
            userMoneyList.stream().forEach(userMoney -> {
                if(user.getId().equals(userMoney.getUserId())){
                    userVo.setMoney(userMoney.getMoney());
                    userVo.setCodeNum(userMoney.getCodeNum());
                    userVo.setWithdrawMoney(userMoney.getWithdrawMoney());//可以提现金额
                }
            });
            userVoList.add(userVo);
        }
        return userVoList;
    }

    /**
     * 用户列表总计
     * @return
     */
    @ApiOperation("用户列表总计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "proxyAccount", value = "代理线", required = false),
            @ApiImplicitParam(name = "state", value = "1：启用，其他：禁用", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false)
    })
    @GetMapping("findUserTotal")
    public ResponseEntity<UserTotalVo> findUserTotal(String account, String proxyAccount, Integer state,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate){
        //后续扩展加参数。
        User user = new User();
        user.setAccount(account);
        user.setState(state);
        if (!LoginUtil.checkNull(proxyAccount)){
            ProxyUser byUserName = proxyUserService.findByUserName(proxyAccount);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(BigDecimal.ZERO);
            }
            if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                user.setFirstProxy(byUserName.getId());
            }else if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                user.setSecondProxy(byUserName.getId());
            }else {
                user.setThirdProxy(byUserName.getId());
            }
        }
        List<User> userList = userService.findUserList(user, startDate, endDate);
        UserTotalVo userTotalVo=new UserTotalVo();
        if(userList != null && userList.size() > 0){
            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            List<UserMoney> all = userMoneyService.findAll(userIds);
            BigDecimal sum = all.stream().map(UserMoney::getMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal washCode = all.stream().map(UserMoney::getWashCode).reduce(BigDecimal.ZERO, BigDecimal::add);
            userTotalVo.setMoney(sum);
            userTotalVo.setWashCode(washCode);
            return ResponseUtil.success(userTotalVo);
        }
        return ResponseUtil.success(BigDecimal.ZERO);
    }



        /**
         * 查询操作
         * 注意：jpa 是从第0页开始的
         * @return
         */
    @ApiOperation("用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "account", value = "用户名", required = false),
            @ApiImplicitParam(name = "proxyAccount", value = "代理线", required = false),
            @ApiImplicitParam(name = "state", value = "1：启用，其他：禁用", required = false),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = false),
            @ApiImplicitParam(name = "sortType", value = "1：余额小到大排序，2余额大到下排序", required = false),
    })
    @GetMapping("findUserList")
    public ResponseEntity<UserVo> findUserList(Integer pageSize, Integer pageCode, String account,String proxyAccount,Integer state,
                                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date startDate,
                                       @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate, Integer sortType){

        //后续扩展加参数。
        User user = new User();
        user.setAccount(account);
        user.setState(state);
        if (!LoginUtil.checkNull(proxyAccount)){
            ProxyUser byUserName = proxyUserService.findByUserName(proxyAccount);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.custom("没有这个代理");
            }
            if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                user.setFirstProxy(byUserName.getId());
            }else if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                user.setSecondProxy(byUserName.getId());
            }else {
                user.setThirdProxy(byUserName.getId());
            }
        }

        PageResultVO<UserVo> pageResultVO;
        List<UserMoney> userMoneyList = new ArrayList<>();
        List<User> userList;
        if(sortType != null && LoginUtil.checkNull(account)){
            Page<UserMoney> userMoneyPage = getPageResultVO(sortType, startDate, endDate, pageCode, pageSize);
            userMoneyList = userMoneyPage.getContent();
            List<Long> userIds = userMoneyPage.getContent().stream().map(UserMoney::getUserId).collect(Collectors.toList());
            userList = userService.findAll(userIds);
            pageResultVO = new PageResultVO(userMoneyPage);
        }else{
            Sort sort=Sort.by("id").descending();
            Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
            Page<User> userPage = userService.findUserPage(pageable, user,startDate,endDate);
            pageResultVO = new PageResultVO(userPage);
            userList = userPage.getContent();
        }

        if(userList != null && userList.size() > 0){
            List<UserVo> userVoList = new LinkedList();
            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            if (userMoneyList == null || userMoneyList.isEmpty()) {
                userMoneyList =  userMoneyService.findAll(userIds);
            }
            List<Long> firstPids = userList.stream().map(User::getFirstPid).collect(Collectors.toList());
            List<User> firstPidUsers = userService.findAll(firstPids);
            List<Long> thirdProxys = userList.stream().map(User::getThirdProxy).collect(Collectors.toList());
            List<Long> firstProxys = userList.stream().map(User::getFirstProxy).collect(Collectors.toList());
            thirdProxys.addAll(firstProxys);
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(thirdProxys);
            if(userMoneyList != null){
                List<UserMoney> finalUserMoneyList = userMoneyList;
                userList.stream().forEach(u -> {
                    // UserVo userVo = new UserVo(u);
                    UserVo userVo = DTOUtil.toDTO(u, UserVo.class);
                    finalUserMoneyList.stream().forEach(userMoney -> {
                        if(u.getId().equals(userMoney.getUserId())){
                            userVo.setMoney(userMoney.getMoney());
                            //待领取洗码金额
                            userVo.setNotCodeWashingAmount(userMoney.getWashCode());
                            userVo.setCodeNum(userMoney.getCodeNum());
                            userVo.setWithdrawMoney(userMoney.getWithdrawMoney());//可以提现金额
                        }
                    });
                    firstPidUsers.stream().forEach(firstPid -> {
                        if(firstPid.getId().equals(u.getFirstPid() == null ? "":u.getFirstPid())){
                            userVo.setFirstPidAccount(firstPid.getAccount());
                        }
                    });
                    proxyUsers.stream().forEach(proxyUser -> {
                        if(proxyUser.getId().equals(u.getThirdProxy() == null ? "":u.getThirdProxy())){
                            userVo.setThirdProxyAccount(proxyUser.getUserName());
                            userVo.setThirdProxyId(proxyUser.getId());
                        }
                        if(proxyUser.getId().equals(u.getFirstProxy() == null ? "":u.getFirstProxy())){
                            userVo.setFirstProxyAccount(proxyUser.getUserName());
                        }
                    });
                    userVoList.add(userVo);
                });
                if(sortType !=null){
                    sortUserVoList(userVoList, sortType);
                }
                pageResultVO.setContent(userVoList);
            }
        }
        return ResponseUtil.success(pageResultVO);
    }

    private List<UserVo> sortUserVoList(List<UserVo> userVoList, Integer sortType) {
        if(sortType == CommonConst.NUMBER_1){
            return searchAsc(userVoList);
        }
        return searchDesc(userVoList);
    }


    //降序排序
    public List<UserVo> searchDesc(List<UserVo> userVoList){
        Collections.sort(userVoList, (o1, o2) -> -o1.getMoney().compareTo(o2.getMoney()));
        return userVoList;
    }

    //升序排列
    public List<UserVo> searchAsc(List<UserVo> userVoList){
        Collections.sort(userVoList, (o1, o2) -> o1.getMoney().compareTo(o2.getMoney()));
        return userVoList;
    }


    private Page<UserMoney> getPageResultVO(Integer sortType, Date startDate, Date endDate, Integer pageCode, Integer pageSize) {
        Sort sort = Sort.by("money").descending();
        if(sortType == CommonConst.NUMBER_1){
            sort = Sort.by("money").ascending();
        }
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Specification<UserMoney> condition = this.getCondition(startDate, endDate);
        Page<UserMoney> userMoneyPage = userMoneyService.findUserMoneyPage(condition, pageable);

        return userMoneyPage;
    }

    private Specification<UserMoney> getCondition(Date startDate, Date endDate) {
        Specification<UserMoney> specification = new Specification<UserMoney>() {
            @Override
            public Predicate toPredicate(Root<UserMoney> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                Predicate predicate = cb.conjunction();
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

    @ApiOperation("刷新WM余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshWM")
    public ResponseEntity getWMMoney(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        UserThird userThird = userThirdService.findByUserId(user.getId());
        if (LoginUtil.checkNull(userThird) || LoginUtil.checkNull(userThird.getAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.getWMonetUser(user, userThird);
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("查询WM余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (LoginUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询WM余额失败");
        }
    }


    @ApiOperation("请求玩家再WM余额总余额")
    @GetMapping("getWMMoneyTotal")
    public ResponseEntity getWMMoneyTotal(){
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_WM_BIG;
        Object wmBalance = redisUtil.get(key);
        if(!LoginUtil.checkNull(wmBalance)){
            return ResponseUtil.success(wmBalance);
        }

        List<UserThird> allAcount = userThirdService.findAllAcount();
        if (LoginUtil.checkNull(allAcount) || allAcount.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(allAcount.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:allAcount){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.getWMonetUser(u);

                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                           list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);

        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ResponseUtil.success(sum);
    }




    @ApiOperation("一键回收用户WM余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("oneKeyRecover")
    public ResponseEntity oneKeyRecover(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }
        JSONObject jsonObject = userMoneyService.oneKeyRecover(user);
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("回收WM余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("回收WM余额失败");
        }
    }

    @ApiOperation("查询玩家PG/CQ9总余额")
    @GetMapping("refreshPGTotal")
    public ResponseEntity refreshPGTotal(){
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_PG;
        Object pgBalance = redisUtil.get(key);
        if(!LoginUtil.checkNull(pgBalance)){
            return ResponseUtil.success(pgBalance);
        }

        List<UserThird> allGoldenfAccount = userThirdService.findAllGoldenfAccount();
        if (LoginUtil.checkNull(allGoldenfAccount) || allGoldenfAccount.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(allGoldenfAccount.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:allGoldenfAccount){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshPGAndCQ9UserId(u.getUserId().toString());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            synchronized (this){
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ResponseUtil.success(sum);
    }

    @ApiOperation("查询玩家OB电竞总余额")
    @GetMapping("refreshOBDJTotal")
    public ResponseEntity refreshOBDJTotal(){
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBDJ;
        Object obdjBalance = redisUtil.get(key);
        if(!LoginUtil.checkNull(obdjBalance)){
            return ResponseUtil.success(obdjBalance);
        }

        List<UserThird> allOBDJAccount = userThirdService.findAllOBDJAccount();
        if (LoginUtil.checkNull(allOBDJAccount) || allOBDJAccount.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(allOBDJAccount.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:allOBDJAccount){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshOB(u.getUserId());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ResponseUtil.success(sum);
    }

    @ApiOperation("查询玩家OB体育总余额")
    @GetMapping("refreshOBTYTotal")
    public ResponseEntity refreshOBTYTotal(){
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_OBTY;
        Object obtyBalance = redisUtil.get(key);
        if(!LoginUtil.checkNull(obtyBalance)){
            return ResponseUtil.success(obtyBalance);
        }

        List<UserThird> allOBTYAccount = userThirdService.findAllOBTYAccount();
        if (LoginUtil.checkNull(allOBTYAccount) || allOBTYAccount.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(allOBTYAccount.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:allOBTYAccount){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshOBTY(u.getUserId());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            list.add(new BigDecimal(jsonObject.get("data").toString()));
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ResponseUtil.success(sum);
    }

    @ApiOperation("查询用户OB余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshOB")
    public ResponseEntity refreshOB(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (LoginUtil.checkNull(third) || ObjectUtils.isEmpty(third.getObdjAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshOB(third.getUserId());
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("OB余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (LoginUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询OB余额失败");
        }
    }


    @ApiOperation("一键回收用户OB余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("oneOBRecoverApi")
    public ResponseEntity oneKeyOBRecoverApi(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }
        JSONObject jsonObject = userMoneyService.oneKeyOBRecoverApi(user);
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("回收OB余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("回收OB余额失败");
        }
    }


    @ApiOperation("查询用户OB体育余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshOBTY")
    public ResponseEntity refreshOBTY(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (LoginUtil.checkNull(third) || ObjectUtils.isEmpty(third.getObtyAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshOBTY(third.getUserId());
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("OB体育余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (LoginUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询OB体育余额失败");
        }
    }


    @ApiOperation("一键回收用户OB体育余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("oneOBTYRecoverApi")
    public ResponseEntity oneKeyOBTYRecoverApi(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }
        JSONObject jsonObject = userMoneyService.oneKeyOBTYRecoverApi(user);
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("回收OB体育余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("回收OB体育余额失败");
        }
    }


    @ApiOperation("查询用户PG/CQ9余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshPGAndCQ9")
    public ResponseEntity refreshPGAndCQ9(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (LoginUtil.checkNull(third) || ObjectUtils.isEmpty(third.getGoldenfAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshPGAndCQ9(third.getUserId());
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("查询PG/CQ9余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (LoginUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询PG/CQ9余额失败");
        }
    }

    @ApiOperation("一键回收用户PG/CQ9余额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("oneKeyRecoverApi")
    public ResponseEntity oneKeyRecoverApi(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }
        JSONObject jsonObject = userMoneyService.oneKeyRecoverApi(user);
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("回收PG/CQ9余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("回收PG/CQ9余额失败");
        }
    }


    @ApiOperation("查询玩家沙巴总余额")
    @GetMapping("refreshSABATotal")
    public ResponseEntity refreshSABATotal(){
        String key = Constants.REDIS_THRID_SUMBALANCE + Constants.PLATFORM_SABASPORT;
        Object pgBalance = redisUtil.get(key);
        if(!LoginUtil.checkNull(pgBalance)){
            return ResponseUtil.success(pgBalance);
        }

        List<UserThird> allGoldenfAccount = userThirdService.findAllGoldenfAccount();
        if (LoginUtil.checkNull(allGoldenfAccount) || allGoldenfAccount.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(BigDecimal.ZERO);
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
        AtomicInteger atomicInteger = new AtomicInteger(allGoldenfAccount.size());
        Vector<BigDecimal> list = new Vector<>();
        for (UserThird u:allGoldenfAccount){
            threadPool.execute(() ->{
                try {
                    JSONObject jsonObject = userMoneyService.refreshSABAUserId(u.getUserId().toString());
                    if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                        list.add(BigDecimal.ZERO);
                    }else {
                        Integer code = (Integer) jsonObject.get("code");
                        if (code == CommonConst.NUMBER_0 && !LoginUtil.checkNull(jsonObject.get("data"))){
                            synchronized (this){
                                list.add(new BigDecimal(jsonObject.get("data").toString()));
                            }
                        }
                    }
                }finally {
                    atomicInteger.decrementAndGet();
                    BillThreadPool.toResume(reentrantLock, condition);
                }
            });
        }
        BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
        BigDecimal sum = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sum = new BigDecimal(sum.toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return ResponseUtil.success(sum);
    }


    @ApiOperation("查询用户沙巴体育余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("refreshSABA")
    public ResponseEntity refreshSABA(Long id){
        UserThird third = userThirdService.findByUserId(id);
        if (LoginUtil.checkNull(third) || ObjectUtils.isEmpty(third.getGoldenfAccount())){
            return ResponseUtil.success(CommonConst.NUMBER_0);
        }
        JSONObject jsonObject = userMoneyService.refreshSABA(third.getUserId());
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("查询沙巴余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (LoginUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("查询沙巴余额失败");
        }
    }

    @ApiOperation("一键回收用户沙巴余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "客户id", required = true),
    })
    @GetMapping("recoverySABABalance")
    public ResponseEntity recoverySABABalance(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("客户不存在");
        }
        JSONObject jsonObject = userMoneyService.oneKeySABAApi(user);
        if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
            return ResponseUtil.custom("回收沙巴余额失败");
        }
        try {
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                return ResponseUtil.success();
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }
        }catch (Exception ex){
            return ResponseUtil.custom("回收沙巴余额失败");
        }
    }

    @ApiOperation("添加用户")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "account", value = "用户名", required = true),
        @ApiImplicitParam(name = "name", value = "用户昵称", required = false),
        @ApiImplicitParam(name = "phone", value = "电话号码", required = false),
        @ApiImplicitParam(name = "thirdAccount", value = "基层代理账号", required = false),
    })
    @PostMapping("saveUser")
    @Transactional
    public ResponseEntity saveUser(String account, String name, String phone,String thirdAccount){
        if (LoginUtil.checkNull(account)){
            return ResponseUtil.custom("参数不合法");
        }
        if (!account.matches(RegexEnum.ACCOUNT.getRegex())){
            return ResponseUtil.custom("账号格式错误");
        }
        List<User> us = userService.findByAccountUpper(account);
        if(!us.isEmpty()){
            return ResponseUtil.custom("账户已存在");
        }

        User user = new User();
        user.setAccount(account);
        if(LoginUtil.checkNull(name)){
            user.setName(account);
        }else{
            if (!name.matches(RegexEnum.NAME.getRegex())){
                return ResponseUtil.custom("昵称请输入1~20位中文或字母");
            }
            user.setName(name);
        }

        user.setState(Constants.open);

        if(!LoginUtil.checkNull(phone)){
            if (!phone.matches(RegexEnum.PHONE.getRegex())) {
                return ResponseUtil.custom("手机号格式错误");
            }
            user.setPhone(phone);
        }

        //默认中文
        //        user.setLanguage(Constants.USER_LANGUAGE_CH);
        if (LoginUtil.checkNull(thirdAccount)){
            //来源 公司会员
            user.setType(Constants.USER_TYPE2);
        }else {
            ProxyUser byUserName = proxyUserService.findByUserName(thirdAccount);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.custom("没有这个代理");
            }
            if (byUserName.getProxyRole() != CommonConst.NUMBER_3){
                return ResponseUtil.custom("代理级别不对应");
            }
            user.setFirstProxy(byUserName.getFirstProxy());
            user.setSecondProxy(byUserName.getSecondProxy());
            user.setThirdProxy(byUserName.getId());
            //来源 渠道会员
            user.setType(Constants.USER_TYPE1);
        }

        //随机生成
        String password = UserPasswordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        user.setPassword(bcryptPassword);
        //默认展示两张收款卡
        user.setCreditCard(Constants.creditCard);
        String inviteCodeNew = generateInviteCodeRunner.getInviteCode();
        user.setInviteCode(inviteCodeNew);
        User save = userService.save(user);
        //userMoney表初始化数据
        UserMoney userMoney=new UserMoney();
        userMoney.setUserId(save.getId());
        userMoney.setMoney(BigDecimal.ZERO);
        userMoney.setCodeNum(BigDecimal.ZERO);
        userMoney.setIsFirst(CommonConst.NUMBER_0);
        userMoneyService.save(userMoney);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }

    @ApiOperation("添加用户获取基层代理数据")
    @GetMapping("getThirdProxy")
    @NoAuthorization
    public ResponseEntity<ProxyUser> getThirdProxy(){
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        proxyUser.setUserFlag(CommonConst.NUMBER_1);
        proxyUser.setProxyRole(CommonConst.NUMBER_3);
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        return ResponseUtil.success(proxyUserList);
    }

    /**
     * 修改用户
     * 只有修改电话功能，那电话不能为空
     *
     * @param id
     * @param state
     * @param phone
     * @return
     */
    @ApiOperation("修改用户电话")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
        @ApiImplicitParam(name = "phone", value = "电话号码", required = true),

    })
    @PostMapping("updateUser")
    public ResponseEntity updateUser(Long id, Integer state, String phone){
        if(LoginUtil.checkNull(id,phone)){
            return ResponseUtil.custom("参数错误");
        }
        //查询用户信息
        User user = userService.findById(id);
        if(user == null){
            return ResponseUtil.custom("账户不存在");
        }
        if (!phone.matches(RegexEnum.PHONE.getRegex())) {
            return ResponseUtil.custom("手机号格式错误");
        }
        user.setPhone(phone);
        userService.save(user);
        return ResponseUtil.success();
    }

    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("updateUserStatus")
    public ResponseEntity updateUserStatus(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        //开启状态，冻结
        if(user.getState() == Constants.USER_NORMAL){
            user.setState(Constants.USER_LOCK_ACCOUNT);
        }else{
            user.setState(Constants.USER_NORMAL);
        }
        userService.save(user);
        return ResponseUtil.success();
    }

    @ApiOperation("重置用户提现密码")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("withdrawPassword")
    public ResponseEntity withdrawPassword(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        //随机生成
        String withdrawPassword = PasswordUtil.getWithdrawPassword();
        String bcryptPassword = LoginUtil.bcrypt(withdrawPassword);
        user.setWithdrawPassword(bcryptPassword);
        userService.save(user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("withdrawPassword", withdrawPassword);
        return ResponseUtil.success(jsonObject);
    }

    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @PostMapping("resetPassword")
    public ResponseEntity resetPassword(Long id){
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        log.info("重置密码之前密码{}, 账号：【{}】",user.getPassword(), user.getAccount());
        //随机生成
        String password = UserPasswordUtil.getRandomPwd();
        String bcryptPassword = LoginUtil.bcrypt(password);
        log.info("生成密码{}",bcryptPassword);
        user.setPassword(bcryptPassword);
        userService.updatePassword(user.getId(), user.getPassword());

        log.info("重置密码之后密码{}",user.getPassword());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", user.getAccount());
        jsonObject.put("password", password);
        return ResponseUtil.success(jsonObject);
    }

    /**
     * 后台新增充值订单
     *
     * @param id 会员id
     * @param remitter 汇款人姓名
     * @param chargeAmount 汇款金额
     * @param remark 汇款备注
     * @return
     */
    @ApiOperation("后台新增充值订单 上分")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "会员id", required = true),
        @ApiImplicitParam(name = "remitter", value = "汇款人姓名", required = false),
        @ApiImplicitParam(name = "chargeAmount", value = "汇款金额", required = true),
        @ApiImplicitParam(name = "remark", value = "汇款备注", required = false),
        @ApiImplicitParam(name = "betRate", value = "打码倍率", required = true),
    })
    @PostMapping("/saveChargeOrder")
    public ResponseEntity saveChargeOrder(Long id,String remitter,String remark, String chargeAmount,BigDecimal betRate){
        if (LoginUtil.checkNull(id,chargeAmount,betRate)){
            return ResponseUtil.custom("参数不合法");
        }
        BigDecimal money = CommonUtil.checkMoney(chargeAmount);
        if(betRate.compareTo(BigDecimal.ZERO)<0){
            return ResponseUtil.custom("打码倍率错误");
        }
        if(money.compareTo(BigDecimal.ZERO)<1){
            return ResponseUtil.custom("金额类型错误");
        }
        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
            return ResponseUtil.custom("金额不能大于99999999");
        }
//        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_100)) >= CommonConst.NUMBER_1){
//            return ResponseUtil.custom("测试环境加钱不能超过100RMB");
//        }
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        //        if (user.getThirdProxy() != null && user.getThirdProxy() >= CommonConst.LONG_1){
        //            return ResponseUtil.custom("代理会员不能操作");
        //        }
        Long userId = LoginUtil.getLoginUserId();
        SysUser sysUser = sysUserService.findById(userId);
        String lastModifier = (sysUser == null || sysUser.getUserName() == null)? "" : sysUser.getUserName();
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setUserId(id);
        //打码倍率
        chargeOrder.setBetRate(betRate);
        chargeOrder.setRemitter(remitter);
        chargeOrder.setRemark(remark);
        chargeOrder.setOrderNo(orderService.getOrderNo());
        chargeOrder.setChargeAmount(money);
        chargeOrder.setLastModifier(lastModifier);
        chargeOrder.setType(user.getType());
        //        chargeOrder.setRealityAmount(money);
        Boolean aBoolean = platformConfigService.queryTotalPlatformQuota();
        if (!aBoolean){
            return ResponseUtil.custom("上分失败,平台额度不足");
        }
        ResponseEntity responseEntity = chargeOrderBusiness.saveOrderSuccess(user, chargeOrder, Constants.chargeOrder_masterControl, Constants.remitType_general, Constants.CODENUMCHANGE_MASTERCONTROL);
        if(responseEntity.getCode()==CommonConst.NUMBER_0){
            platformConfigService.backstage(CommonConst.NUMBER_0, new BigDecimal(chargeAmount));
        }
        return responseEntity;
    }



    @ApiOperation("系统上分")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "会员id", required = true),
            @ApiImplicitParam(name = "remitter", value = "汇款人姓名", required = false),
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = false),
            @ApiImplicitParam(name = "chargeAmount", value = "汇款金额", required = true),
            @ApiImplicitParam(name = "remark", value = "汇款备注", required = false),
            @ApiImplicitParam(name = "betRate", value = "打码倍率", required = true),
    })
    @PostMapping("/saveSystemChargeOrder")
    public ResponseEntity saveSystemChargeOrder(String orderNo,Long id,String remitter,String remark, String chargeAmount,BigDecimal betRate){
        if (LoginUtil.checkNull(id,chargeAmount,betRate)){
            return ResponseUtil.custom("参数不合法");
        }
        BigDecimal money = CommonUtil.checkMoney(chargeAmount);
        if(betRate.compareTo(BigDecimal.ZERO)<0){
            return ResponseUtil.custom("打码倍率错误");
        }
        if(money.compareTo(BigDecimal.ZERO)<1){
            return ResponseUtil.custom("金额类型错误");
        }
        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
            return ResponseUtil.custom("金额不能大于99999999");
        }
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        Long userId = LoginUtil.getLoginUserId();
        SysUser sysUser = sysUserService.findById(userId);
        String lastModifier = (sysUser == null || sysUser.getUserName() == null)? "" : sysUser.getUserName();
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setUserId(id);
        //打码倍率
        chargeOrder.setBetRate(betRate);
        chargeOrder.setRemitter(remitter);
        chargeOrder.setRemark(remark);
        chargeOrder.setOrderNo(orderService.getOrderNo());
        chargeOrder.setChargeAmount(money);
        chargeOrder.setLastModifier(lastModifier);
        chargeOrder.setType(user.getType());
        Boolean aBoolean = platformConfigService.queryTotalPlatformQuota();
        if (!aBoolean){
            return ResponseUtil.custom("上分失败,平台额度不足");
        }
        ResponseEntity responseEntity = chargeOrderBusiness.saveSystemOrderSuccess(orderNo,user, chargeOrder, Constants.chargeOrder_masterControl, Constants.remitType_general, Constants.CODENUMCHANGE_MASTERCONTROL);
        if(responseEntity.getCode()==CommonConst.NUMBER_0){
            platformConfigService.backstage(CommonConst.NUMBER_0, new BigDecimal(chargeAmount));
        }
        return responseEntity;
    }
    /**
     * 后台新增提现订单
     *
     * @param id 会员id
     * @param withdrawMoney 提现金额
     * @param bankId 银行id
     * @return
     */
    @ApiOperation("后台新增提现订单 下分")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
        @ApiImplicitParam(name = "withdrawMoney", value = "提现金额", required = true),
        @ApiImplicitParam(name = "bankId", value = "银行id", required = false),
        @ApiImplicitParam(name = "remark", value = "备注", required = false),
    })
    @PostMapping("/saveWithdrawOrder")
    public ResponseEntity saveWithdrawOrder(Long id,String withdrawMoney,String bankId,String remark){
        if (LoginUtil.checkNull(id,withdrawMoney)){
            return ResponseUtil.custom("参数不合法");
        }
        BigDecimal money = CommonUtil.checkMoney(withdrawMoney);
        if(money.compareTo(BigDecimal.ZERO)<1){
            return ResponseUtil.custom("金额类型错误");
        }
        if (money.compareTo(new BigDecimal(CommonConst.NUMBER_99999999)) >= CommonConst.NUMBER_1){
            return ResponseUtil.custom("金额不能大于99999999");
        }
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("找不到这个会员");
        }
        //        if (user.getThirdProxy() != null && user.getThirdProxy() >= CommonConst.LONG_1){
        //            return ResponseUtil.custom("代理会员不能操作");
        //        }
        Long userId = LoginUtil.getLoginUserId();
        //        SysUser sysUser = sysUserService.findById(userId);
        //        String lastModifier = (sysUser == null || sysUser.getUserName() == null)? "" : sysUser.getUserName();
        ResponseEntity responseEntity = withdrawBusiness.updateWithdrawAndUser(user, id, money, bankId, Constants.withdrawOrder_masterControl, userId, remark);
        if (responseEntity.getCode()==CommonConst.NUMBER_0){
            platformConfigService.backstage(CommonConst.NUMBER_1,new BigDecimal(withdrawMoney));
        }
        return responseEntity;
    }

    @ApiOperation("后台下分检验可提款金额")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
        @ApiImplicitParam(name = "withdrawMoney", value = "提现金额", required = true),
    })
    @NoAuthorization
    @GetMapping("/checkoutWithdrawMoney")
    public ResponseEntity checkoutWithdrawMoney(Long id,String withdrawMoney) {
        if (LoginUtil.checkNull(id,withdrawMoney)){
            return ResponseUtil.custom("参数不合法");
        }
        BigDecimal money = CommonUtil.checkMoney(withdrawMoney);
        if(money.compareTo(BigDecimal.ZERO) < CommonConst.NUMBER_1){
            return ResponseUtil.custom("金额类型错误");
        }
        UserMoney byUserId = userMoneyService.findByUserId(id);
        if (LoginUtil.checkNull(byUserId)){
            return ResponseUtil.custom("用户钱包不存在");
        }
        BigDecimal drawMoney = byUserId.getWithdrawMoney();//得到可提现金额
        if(drawMoney.compareTo(money) < CommonConst.NUMBER_0){
            return ResponseUtil.success(false);
        }
        return ResponseUtil.success(true);
    }

    @ApiOperation("打码量清零")
    @PostMapping("/cleanMud")
    public ResponseEntity<String> cleanMud(@RequestBody UserCleanMudCo co) {
        chargeOrderBusiness.cleanUserMud(co.getUserId());
        return ResponseUtil.success();
    }

    /**
     * 后台配置会员收款卡修改
     *
     * @param id 会员id
     * @param creditCard 收款卡张数
     * @param cardLevel 配置收款卡等级
     * @return
     */
    @ApiOperation("后台配置会员收款卡修改")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
        @ApiImplicitParam(name = "creditCard", value = "收款卡张数", required = true),
        @ApiImplicitParam(name = "cardLevel", value = "配置收款卡等级", required = false),
    })
    @PostMapping("/saveCollectionBankcard")
    public ResponseEntity saveCollectionBankcard(Long id,Integer creditCard,String cardLevel){
        if (LoginUtil.checkNull(id,creditCard)){
            return ResponseUtil.custom("参数不合法");
        }
        if (creditCard != CommonConst.NUMBER_1 && creditCard != CommonConst.NUMBER_2){
            return ResponseUtil.custom("收款卡张数最多两张");
        }
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("账户不存在");
        }
        user.setCreditCard(creditCard);
        if (!LoginUtil.checkNull(cardLevel)){
            String[] split = cardLevel.split(CommonConst.HYPHEN);
            if (!CommonConst.cardLevel.containsAll(Arrays.asList(split))){
                return ResponseUtil.custom("收款卡等级不合法");
            }
        }
        user.setCardLevel(cardLevel);
        userService.save(user);
        return ResponseUtil.success();
    }

    /**
     * 根据id查询用户登录注册ip
     *
     * @param id 会员id
     * @param pageSize 每页大小
     * @param pageCode 当前页
     * @return
     */
    @ApiOperation("根据id查询用户登录注册ip")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @GetMapping("/findIp")
    public ResponseEntity<LoginLog> findIp(Integer pageSize, Integer pageCode,Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        Sort sort = Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(id);
        Page<LoginLog> loginLogPage = loginLogService.findLoginLogPage(loginLog, pageable);
        return ResponseUtil.success(loginLogPage);
    }
    /**
     * 根据id查询推广数据
     *会员列表详情
     * @param id 会员id
     * @return
     */
    @ApiOperation("根据id查询推广数据")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @NoAuthorization
    @GetMapping("/findProxyReport")
    public ResponseEntity<ProxyReport> findProxyReport(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyReport proxyReport = proxyReportService.findByUserId(id);
        return ResponseUtil.success(proxyReport);
    }
    /**
     * 根据id查询上下三级代理线
     *会员列表详情
     * @param id 会员id
     * @return
     */
    @ApiOperation("根据id查询上下三级代理线")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "用户id", required = true),
    })
    @NoAuthorization
    @GetMapping("/findAgency")
    public ResponseEntity findAgency(Long id){
        if (LoginUtil.checkNull(id)){
            return ResponseUtil.custom("参数不合法");
        }
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.success("");
        }
        String agency = user.getAccount()+"("+messageUtil.get("当前")+")";
        User first = userService.findById(user.getFirstPid() == null ? 0L:user.getFirstPid());
        if (LoginUtil.checkNull(first)){
            return ResponseUtil.success(agency);
        }
        agency = first.getAccount() + " — "  + agency;
        User second = userService.findById(user.getSecondPid() == null ? 0L:user.getSecondPid());
        if (LoginUtil.checkNull(second)){
            return ResponseUtil.success(agency);
        }
        agency = second.getAccount() + " — "  + agency;
        User third = userService.findById(user.getThirdPid() == null ? 0L:user.getThirdPid());
        if (LoginUtil.checkNull(third)){
            return ResponseUtil.success(agency);
        }
        agency = third.getAccount() + " — "  + agency;
        return ResponseUtil.success(agency);
    }
}

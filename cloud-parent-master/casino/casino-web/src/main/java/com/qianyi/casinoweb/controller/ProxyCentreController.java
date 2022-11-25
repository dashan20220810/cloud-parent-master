package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.ProxyCentreVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("proxyCentre")
@Api(tags = "代理中心")
public class ProxyCentreController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Autowired
    private ProxyDayReportService proxyDayReportService;
    @Autowired
    private ProxyReportService proxyReportService;
    @Autowired
    private ShareProfitChangeService shareProfitChangeService;
    @Autowired
    private PlatformConfigService platformConfigService;
    @Autowired
    @Qualifier("asyncExecutor")
    private Executor executor;


    @ApiOperation("查询今日，昨日，本周佣金")
    @GetMapping("/getCommission")
    public ResponseEntity<ProxyCentreVo> getCommission() {
        //人人代开关检查
        ResponseEntity response = checkPeopleProxySwitch();
        if (response != null) {
            return response;
        }
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        ProxyCentreVo vo = new ProxyCentreVo();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        String today = formatter.format(calendar.getTime());
        List<ProxyDayReport> todayData = proxyDayReportService.getCommission(userId, today, null);
        if (!CollectionUtils.isEmpty(todayData)) {
            vo.setTodayCommission(todayData.get(0).getProfitAmount());
        }
        //昨日佣金
        calendar.add(Calendar.DATE, -1);
        String yesterday = formatter.format(calendar.getTime());
        List<ProxyDayReport> yesterdayData = proxyDayReportService.getCommission(userId, yesterday, null);
        if (!CollectionUtils.isEmpty(yesterdayData)) {
            vo.setYesterdayCommission(yesterdayData.get(0).getProfitAmount());
        }
        //本周佣金
        Date weekStartDate = DateUtil.getWeekStartDate();
        String weekStart = formatter.format(weekStartDate);
        Date weekEndDate = DateUtil.getWeekEndDate();
        String weekEnd = formatter.format(weekEndDate);
        List<ProxyDayReport> weekData = proxyDayReportService.getCommission(userId, weekStart, weekEnd);
        if (!CollectionUtils.isEmpty(weekData)) {
            BigDecimal weekSum = weekData.stream().filter(item -> item.getProfitAmount() != null).map(ProxyDayReport::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setWeekCommission(weekSum);
        }
        return ResponseUtil.success(vo);
    }

    @ApiOperation("我的团队")
    @GetMapping("/myTeam")
    public ResponseEntity<ProxyCentreVo.MyTeam> myTeam() {
        //人人代开关检查
        ResponseEntity response = checkPeopleProxySwitch();
        if (response != null) {
            return response;
        }
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        ProxyCentreVo.MyTeam myTeam = new ProxyCentreVo.MyTeam();
        List<ProxyDayReport> todayData = proxyDayReportService.getCommission(userId, today, null);
        if (!CollectionUtils.isEmpty(todayData)) {
            ProxyDayReport proxyDayReport = todayData.get(0);
            myTeam.setDeppositeAmount(proxyDayReport.getDeppositeAmount());
            myTeam.setBetAmount(proxyDayReport.getBetAmount());
            int newNum = proxyDayReport.getNewNum() == null ? 0 : proxyDayReport.getNewNum();
            myTeam.setNewNum(newNum);
        }
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if (proxyReport != null) {
            int allGroupNum = proxyReport.getAllGroupNum() == null ? 0 : proxyReport.getAllGroupNum();
            int directGroupNum = proxyReport.getDirectGroupNum() == null ? 0 : proxyReport.getDirectGroupNum();
            myTeam.setAllGroupNum(allGroupNum);
            myTeam.setDirectGroupNum(directGroupNum);
        }
        return ResponseUtil.success(myTeam);
    }

    @ApiOperation("代理报表")
    @GetMapping("/proxyReport")
    public ResponseEntity<ProxyReport> proxyReport() {
        //人人代开关检查
        ResponseEntity response = checkPeopleProxySwitch();
        if (response != null) {
            return response;
        }
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if (proxyReport != null) {
            return ResponseUtil.success(proxyReport);
        }
        return ResponseUtil.success(new ProxyReport());
    }

    @ApiOperation("业绩查询")
    @GetMapping("/findAchievementList")
    @ApiImplicitParams({@ApiImplicitParam(name = "account", value = "会员账号", required = false),
    })
    public ResponseEntity<List<ProxyCentreVo.ShareProfit>> findAchievementList(String account) {
        //人人代开关检查
        ResponseEntity response = checkPeopleProxySwitch();
        if (response != null) {
            return response;
        }
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        List<ProxyCentreVo.ShareProfit> dataList = new ArrayList<>();
        //根据前端传递的账号查询对应id
        Long directUserId = null;
        User directUser = null;
        if (!ObjectUtils.isEmpty(account)) {
            directUser = userService.findByFirstPidAndAccount(userId, account);
            if (directUser == null) {
                return ResponseUtil.success(dataList);
            }
            directUserId = directUser.getId();
        }
        //查询所有直属,条件查询时只显示对应的直属数据
        List<User> users = new ArrayList<>();
        if (directUser != null) {
            users.add(directUser);
        } else {
            users = userService.findFirstUser(userId);
        }
        //没有直属直接返回
        if (CollectionUtils.isEmpty(users)) {
            return ResponseUtil.success(dataList);
        }
        //查询直属总贡献
        List<ShareProfitChange> directList = shareProfitChangeService.getShareProfitList(userId, 1, directUserId);
        //补全直属，没有数据的直属默认显示0
        for (User user : users) {
            boolean flag = true;
            for (ShareProfitChange shareProfit : directList) {
                if (user.getId().equals(shareProfit.getFromUserId())) {
                    flag = false;
                    ProxyCentreVo.ShareProfit share = new ProxyCentreVo.ShareProfit();
                    share.setUserId(shareProfit.getFromUserId());
                    share.setAccount(user.getAccount());
                    share.setDirectProfitAmount(shareProfit.getAmount());
                    share.setDirectBetAmount(shareProfit.getValidbet());
                    dataList.add(share);
                    break;
                }
            }
            if (flag) {
                ProxyCentreVo.ShareProfit share = new ProxyCentreVo.ShareProfit();
                share.setUserId(user.getId());
                share.setAccount(user.getAccount());
                dataList.add(share);
            }
        }
        //查询第一级附属,查询出来后的数据的上级是当前用户的直属，数据按直属归类
        List<ShareProfitChange> subsidiaryList1 = shareProfitChangeService.getShareProfitList(userId, 2, null);
        //查询第二级附属,查询出来后的数据的上上级是当前用户的直属，数据按直属归类
        List<ShareProfitChange> subsidiaryList2 = shareProfitChangeService.getShareProfitList(userId, 3, null);
        List<CompletableFuture> completableFutures = new ArrayList<>();
        for (ProxyCentreVo.ShareProfit direct : dataList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                setOtherProfitAmount(direct, subsidiaryList1, subsidiaryList2);
            }, executor);
            completableFutures.add(future);
        }
        //等待所有子线程计算完成
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).join();
        return ResponseUtil.success(dataList);
    }

    /**
     * 按直属分类归属附属佣金
     *
     * @param direct
     * @param subsidiaryList1
     * @param subsidiaryList2
     */
    public void setOtherProfitAmount(ProxyCentreVo.ShareProfit direct, List<ShareProfitChange> subsidiaryList1, List<ShareProfitChange> subsidiaryList2) {
        for (ShareProfitChange change : subsidiaryList1) {
            User user = userService.findById(change.getFromUserId());
            if (user.getFirstPid() != null && user.getFirstPid().equals(direct.getUserId())) {
                direct.setOtherProfitAmount(direct.getOtherProfitAmount().add(change.getAmount()));
            }
        }
        for (ShareProfitChange change : subsidiaryList2) {
            User user = userService.findById(change.getFromUserId());
            if (user.getSecondPid() != null && user.getSecondPid().equals(direct.getUserId())) {
                direct.setOtherProfitAmount(direct.getOtherProfitAmount().add(change.getAmount()));
            }
        }
    }

    @ApiOperation("用户领取分润金额")
    @GetMapping("/receiveShareProfit")
    public ResponseEntity<String> receiveWashCode() {
        //人人代开关检查
        ResponseEntity response = checkPeopleProxySwitch();
        if (response != null) {
            return response;
        }
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal shareProfit = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getShareProfit() != null) {
            shareProfit = userMoney.getShareProfit();
        }
        if (shareProfit.compareTo(BigDecimal.ONE) == -1) {
            return ResponseUtil.custom("金额小于1,不能领取");
        }
        userMoneyService.addMoney(userId, shareProfit);
        userMoneyService.subShareProfit(userId, shareProfit);

        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.SHARE_PROFIT);
        vo.setAmount(shareProfit);
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(shareProfit));
        asyncService.executeAsync(vo);
        //后台异步增减平台总余额
        platformConfigService.reception(CommonConst.NUMBER_0,shareProfit.stripTrailingZeros());
        return ResponseUtil.success("成功领取金额" , shareProfit.stripTrailingZeros().toPlainString());
    }

    /**
     * 人人代开关检查
     * @return
     */
    public ResponseEntity checkPeopleProxySwitch() {
        PlatformConfig platformConfig = platformConfigService.findFirst();
        boolean proxySwitch = PlatformConfig.checkPeopleProxySwitch(platformConfig);
        if (!proxySwitch) {
            return ResponseUtil.custom("不支持此功能");
        }
        return null;
    }
}

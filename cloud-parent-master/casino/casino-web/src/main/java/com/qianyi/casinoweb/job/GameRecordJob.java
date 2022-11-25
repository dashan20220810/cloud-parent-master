package com.qianyi.casinoweb.job;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.business.ThirdGameBusiness;
import com.qianyi.casinocore.business.UserMoneyBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.livewm.api.PublicWMApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class GameRecordJob {

    @Qualifier("asyncExecutor")
    @Autowired
    private Executor executor;

    @Autowired
    PublicWMApi wmApi;
    @Autowired
    GameRecordService gameRecordService;
    @Autowired
    GameRecordEndTimeService gameRecordEndTimeService;
    @Autowired
    UserThirdService userThirdService;
    @Autowired
    ThirdGameBusiness thirdGameBusiness;
    @Autowired
    UserService userService;
    @Autowired
    PlatformConfigService platformConfigService;
    @Autowired
    GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    UserMoneyBusiness userMoneyBusiness;
    @Autowired
    PlatformGameService platformGameService;
    @Value("${spring.profiles.active}")
    String active;

    //每隔5分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void pullGameRecord() {
        PlatformGame platformGame = platformGameService.findByGamePlatformName(Constants.PLATFORM_WM_BIG);
        if (platformGame != null && platformGame.getGameStatus() == 2) {
            log.info("后台已关闭WM,无需拉单,platformGame={}",platformGame);
            return;
        }
        log.info("定时器开始拉取游戏记录");
        String timeMsg = null;
        try {
            //多环境不能同时发起请求，test环境延迟30s执行，正式环境优先,报表查询需间隔30秒，未搜寻到数据需间隔10秒。
            if ("test".equals(active)) {
                Thread.sleep(30 * 1000);
            }
            if ("dev".equals(active)) {
                Thread.sleep(60 * 1000);
            }
            GameRecordEndTime gameRecord = gameRecordEndTimeService.findFirstByEndTimeDesc();
            String time = gameRecord == null ? null : gameRecord.getEndTime();
            //获取查询游戏记录的时间范围
            StartTimeAndEndTime startTimeAndEndTime = getStartTimeAndEndTime(time);
            String startTime = startTimeAndEndTime.getStartTime();
            String endTime = startTimeAndEndTime.getEndTime();
            timeMsg = startTime + "到" + endTime;
            log.info("开始拉取{}的wm游戏记录",timeMsg);
            //查询时间范围内的所有游戏记录，（以结算时间为条件）
            String result = wmApi.getDateTimeReport(null, startTime, endTime, 0, 1, 2, null, null);
            //远程请求异常
            if (ObjectUtils.isEmpty(result)) {
                log.error("{}游戏记录拉取异常",timeMsg);
                gameRecordAsyncOper.sendMsgToTelegramBot(timeMsg + active + "环境,游戏记录拉取异常,原因:远程请求异常");
                return;
            }
            //查询结果无记录
            if ("notData".equals(result)) {
                log.info("{}时间范围无记录",timeMsg);
                updateEndTime(endTime, gameRecord);
                return;
            }
            List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
            if (!CollectionUtils.isEmpty(gameRecords)) {
                saveAll(gameRecords);
            }
            updateEndTime(endTime, gameRecord);
            log.info("{}wm游戏记录拉取完成",timeMsg);
        } catch (Exception e) {
            gameRecordAsyncOper.sendMsgToTelegramBot(timeMsg + active + "环境,游戏记录拉取异常,原因:" + e.getMessage());
            log.error("{}游戏记录拉取异常",timeMsg);
            e.printStackTrace();
        }
    }

    /**
     * 获取拉取游戏的开始时间和结束时间
     *
     * @param time
     * @return
     * @throws ParseException
     */
    private StartTimeAndEndTime getStartTimeAndEndTime(String time) throws ParseException {
        String startTime = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date nowDate = new Date();
        String endTime = format.format(nowDate);
        //第一次拉取数据取当前时间前5分钟为开始时间，之后以上次拉取数据的结束时间为开始时间
        if (ObjectUtils.isEmpty(time)) {
            startTime = getBeforeDateTime(format,endTime,-5);
        } else {
            startTime = time;
        }
        Date startDate = format.parse(startTime);
        long startTimeNum = startDate.getTime();
        long endTimeNum = nowDate.getTime();
        //第三方要求拉取数据时间范围不能大于1天，大于1天，取开始时间的后一天为结束时间
        //重叠时间区间 -2代表重叠2分钟
        int overlap = -2;
        if ((endTimeNum - startTimeNum) > 60 * 60 * 24 * 1000) {
            Calendar now = Calendar.getInstance();
            now.setTime(startDate);
            now.add(Calendar.DAY_OF_MONTH, 1);
            Date afterFiveMin = now.getTime();
            endTime = format.format(afterFiveMin);
            //下面开始时间前移2分钟，结束时间也要前移2分钟
            endTime = getBeforeDateTime(format,endTime,overlap);
        }
        //开始时间往前2分钟，重叠两分钟的时间区间
        startTime = getBeforeDateTime(format,startTime,overlap);

        StartTimeAndEndTime startTimeAndEndTime = new StartTimeAndEndTime();
        startTimeAndEndTime.setStartTime(startTime);
        startTimeAndEndTime.setEndTime(endTime);
        return startTimeAndEndTime;
    }

    private String getBeforeDateTime(SimpleDateFormat format,String currentTime,int before) throws ParseException {
        Date date = format.parse(currentTime);
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.add(Calendar.MINUTE, before);
        Date afterFiveMin = now.getTime();
        String dateTime = format.format(afterFiveMin);
        return dateTime;
    }

    /**
     * 更新游戏表结束时间
     *
     * @param endTime
     */
    public void updateEndTime(String endTime, GameRecordEndTime gameRecordEndTime) {
        if (gameRecordEndTime == null) {
            gameRecordEndTime = new GameRecordEndTime();
        }
        gameRecordEndTime.setEndTime(endTime);
        gameRecordEndTimeService.save(gameRecordEndTime);
    }

    public void saveAll(List<GameRecord> gameRecordList) {
        log.info("开始处理游戏记录数据");
        //查询最小清0打码量
        PlatformConfig platformConfig = platformConfigService.findFirst();
        for (GameRecord gameRecord : gameRecordList) {
            try {
                UserThird account = userThirdService.findByAccount(gameRecord.getUser());
                if (account == null || account.getUserId() == null) {
                    log.error("同步游戏记录时，UserThird查询结果为null,account={}", gameRecord.getUser());
                    continue;
                }
                gameRecord.setUserId(account.getUserId());
                BigDecimal validbet = ObjectUtils.isEmpty(gameRecord.getValidbet()) ? BigDecimal.ZERO : new BigDecimal(gameRecord.getValidbet());
                //有效投注额为0不参与洗码,打码,分润,抽點
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    gameRecord.setWashCodeStatus(Constants.yes);
                    gameRecord.setCodeNumStatus(Constants.yes);
                    gameRecord.setShareProfitStatus(Constants.yes);
                    gameRecord.setExtractStatus(Constants.yes);
                    gameRecord.setRebateStatus(Constants.yes);
                }
                //有数据会重复注单id唯一约束会报错，所以一条一条保存，避免影响后面的
                GameRecord record = save(gameRecord);
                //计算用户账号实时余额
                changeUserBalance(account.getUserId(),gameRecord.getResult());
                //发送注单消息到MQ后台要统计数据
                gameRecordAsyncOper.proxyGameRecordReport(Constants.PLATFORM_WM,record);
                if (validbet.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                //洗码
                gameRecordAsyncOper.washCode(Constants.PLATFORM_WM, record);
                // 抽点
                gameRecordAsyncOper.extractPoints(Constants.PLATFORM_WM, record);
                //扣减打码量
                gameRecordAsyncOper.subCodeNum(Constants.PLATFORM_WM,platformConfig, record);
                //代理分润
                gameRecordAsyncOper.shareProfit(Constants.PLATFORM_WM,record);
                //返利
                gameRecordAsyncOper.rebate(Constants.PLATFORM_WM,record);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("保存游戏记录时报错,message={}", e.getMessage());
            }
        }
    }

    /**
     * 改变用户实时余额
     * @param userId
     * @param result
     */
    private void changeUserBalance(Long userId, String result) {
        try {
            if (ObjectUtils.isEmpty(result)) {
                return;
            }
            BigDecimal amount = new BigDecimal(result);
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            //大于0加钱
            if (amount.compareTo(BigDecimal.ZERO) == 1) {
                userMoneyBusiness.addBalance(userId, amount);
            } else {
                userMoneyBusiness.subBalance(userId, amount.abs());
            }
        }catch (Exception e){
            log.error("改变用户实时余额时报错，msg={}",e.getMessage());
        }
    }

    public GameRecord save(GameRecord gameRecord) throws Exception{
        User user = userService.findById(gameRecord.getUserId());
        if (user != null) {
            gameRecord.setFirstProxy(user.getFirstProxy());
            gameRecord.setSecondProxy(user.getSecondProxy());
            gameRecord.setThirdProxy(user.getThirdProxy());
        }
        GameRecord record = gameRecordService.save(gameRecord);
        return record;
    }

    @Data
    class StartTimeAndEndTime {
        private String startTime;
        private String endTime;
    }
}

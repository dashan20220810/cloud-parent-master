package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinoweb.job.GameRecordAsyncOper;
import com.qianyi.casinoweb.job.GameRecordObdjJob;
import com.qianyi.casinoweb.job.GameRecordObtyJob;
import com.qianyi.liveob.api.PublicObtyApi;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(tags = "测试")
@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Autowired
    private GameRecordAsyncOper gameRecordAsyncOper;
    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("sendMq")
    @ApiOperation("批量发送分润MQ")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "起始ID", required = true),
            @ApiImplicitParam(name = "platform", value = "平台:wm,PG,CQ9", required = true),
    })
    public ResponseEntity sendMq(Long id, String platform) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = format.format(new Date());
        List<GameRecord> list = gameRecordService.findByCreateByAndIdGreaterThanEqualOrderByIdAsc("0", id);
        redisUtil.set("sendMq::startTime::" + id, startTime);
        for (GameRecord gameRecord : list) {
            gameRecordAsyncOper.shareProfit(platform, gameRecord);
            redisUtil.set("sendMq::endId::" + id, gameRecord.getId());
            redisUtil.incr("sendMq::totalNum", 1);
        }
        String endTime = format.format(new Date());
        redisUtil.set("sendMq::endTime::" + id, endTime);
        return ResponseUtil.success();
    }

    @GetMapping("sendMqByBetId")
    @ApiOperation("根据注单ID发送消息")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "betId", value = "注单ID", required = true),
            @ApiImplicitParam(name = "platform", value = "平台:wm,PG,CQ9", required = true),
    })
    public ResponseEntity sendMqByBetId(String betId, String platform) {
        GameRecord gameRecord = gameRecordService.findByBetId(betId);
        gameRecordAsyncOper.shareProfit(platform, gameRecord);
        return ResponseUtil.success();
    }

    @GetMapping("sendMsgToTelegramBot")
    @ApiOperation("发送消息到TG机器人")
    @NoAuthentication
    @ApiImplicitParam(name = "msg", value = "消息", required = true)
    public ResponseEntity sendMsgToTelegramBot(String msg) {
        gameRecordAsyncOper.sendMsgToTelegramBot(msg);
        return ResponseUtil.success();
    }

    @GetMapping("requestTest")
    @ApiOperation("请求次数测试")
    @NoAuthentication
    @ApiImplicitParam(name = "msg", value = "消息", required = true)
    public ResponseEntity requestTest(String msg) {
        log.info("请求成功,消息={}", msg);
        return ResponseUtil.success();
    }

    @GetMapping("getVerificationCodeByPhone")
    @ApiOperation("根据手机号查询验证码，测试用")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "country", value = "区号，柬埔寨：855", required = true),
            @ApiImplicitParam(name = "phone", value = "手机号", required = true)
    })
    public ResponseEntity getVerificationCodeByPhone(String country, String phone) {
        String phoneKey = Constants.REDIS_SMSCODE + country + phone;
        Object val = redisUtil.get(phoneKey);
        return ResponseUtil.success(val);
    }

    @Autowired
    private GameRecordObdjJob gameRecordObdjJob;
    @Autowired
    private GameRecordObtyJob gameRecordObtyJob;

    @GetMapping("/getObTime")
    @ApiOperation("OB拉取时间测试")
    @NoAuthentication
    @ApiImplicitParam(name = "startTime", value = "开始时间", required = true)
    public ResponseEntity getObTime(Long startTime) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<GameRecordObdjJob.StartTimeAndEndTime> startTimeAndEndTime = gameRecordObdjJob.getStartTimeAndEndTime(startTime);
        SimpleDateFormat format = DateUtil.getSimpleDateFormat();
        for (GameRecordObdjJob.StartTimeAndEndTime time : startTimeAndEndTime) {
            Map<String, Object> map = new HashMap<>();
            map.put("startTime", format.format(time.getStartTime() * 1000));
            map.put("endTime", format.format(time.getEndTime() * 1000));
            list.add(map);
        }
        return ResponseUtil.success(list);
    }

    @GetMapping("/getObdjRecord")
    @ApiOperation("OB电竞注单测试")
    @NoAuthentication
    public ResponseEntity getObdjRecord() {
        gameRecordObdjJob.pullGameRecord();
        return ResponseUtil.success();
    }

    @GetMapping("/getObtyRecord")
    @ApiOperation("OB体育注单测试")
    @NoAuthentication
    public ResponseEntity getObtyRecord() {
        gameRecordObtyJob.pullGameRecord();
        return ResponseUtil.success();
    }

    @Autowired
    private PublicObtyApi obtyApi;

    @GetMapping("/obtyCreate")
    @ApiOperation("OB体育创建用户")
    @NoAuthentication
    @ApiImplicitParam(name = "userName", value = "用户名", required = true)
    public ResponseEntity obtyCreate(String userName) {
        obtyApi.create(userName, userName);
        return ResponseUtil.success();
    }

    @GetMapping("/obtyLogin")
    @ApiOperation("OB体育登录")
    @NoAuthentication
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true),
            @ApiImplicitParam(name = "balance", value = "余额", required = false)
    })
    public ResponseEntity obtyLogin(String userName, BigDecimal balance) {
        obtyApi.login(userName, "pc", balance);
        return ResponseUtil.success();
    }

    @GetMapping("/obtykickOutUser")
    @ApiOperation("OB体育退出登录")
    @NoAuthentication
    @ApiImplicitParam(name = "userName", value = "用户名", required = true)
    public ResponseEntity kickOutUser(String userName) {
        obtyApi.kickOutUser(userName);
        return ResponseUtil.success();
    }
}

package com.qianyi.casinoadmin.install;

import com.qianyi.casinocore.model.GameRecord;
import com.qianyi.casinocore.model.GameRecordGoldenF;
import com.qianyi.casinocore.model.ProxyGameRecordReport;
import com.qianyi.casinocore.service.GameRecordGoldenFService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.ProxyGameRecordReportService;
import com.qianyi.casinocore.util.UserPasswordUtil;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@Order(9)
public class AddData implements CommandLineRunner {
    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    @Qualifier("proxyGameRecordReportJob")
    private AsyncService asyncService;

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;

    private static final Integer pageSize = 1000;

    public static final Integer num = 50000;
    @Override
    public void run(String... args) throws Exception {
//        log.info("AddData转移数据开始");
//        List<ProxyGameRecordReport> all = proxyGameRecordReportService.findAll();
//        log.info("取到报表数据all{}",all);
//        if (all == null || all.isEmpty()){
//            Long gameRecordMaxId = gameRecordService.findMaxId();
//            log.info("取到gameRecordMaxId{}",gameRecordMaxId);
//            if (gameRecordMaxId != null && gameRecordMaxId.longValue() != 0L){
//                log.info("开始执行方法recursionWm");
//                new Thread(()->{
//                    recursionWm(1,pageSize,gameRecordMaxId);
//                }).start();
//            }
//
//            Long gameRecordGoldenFMaxId = gameRecordGoldenFService.findMaxId();
//            log.info("取到gameRecordGoldenFMaxId{}",gameRecordGoldenFMaxId);
//            if (gameRecordGoldenFMaxId != null && gameRecordGoldenFMaxId.longValue() != 0L){
//                log.info("开始执行方法recursionPg");
//                new Thread(()->{
//                    recursionPg(1,pageSize,gameRecordGoldenFMaxId);
//                }).start();
//            }
//
//        }
    }

    private void recursionWm(Integer pageCode,Integer pageSize,Long maxId){
        log.info("GameRecord pageCode {} pageSize {}",pageCode,pageSize);
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(pageCode-1, pageSize,sort);
        GameRecord game = new GameRecord();
        game.setId(maxId);
        Page<GameRecord> gameRecordPage = gameRecordService.findGameRecordPage(game, pageable,null,null,null,null);
        List<GameRecord> list = gameRecordPage.getContent();
        if (list == null || list.isEmpty()){
            return;
        }
        list.forEach(g->{
            ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
            vo.setOrderId(g.getBetId());
            vo.setFirstProxy(g.getFirstProxy());
            vo.setSecondProxy(g.getSecondProxy());
            vo.setThirdProxy(g.getThirdProxy());
            vo.setOrderTimes(g.getBetTime());
            vo.setUserId(g.getUserId());
            vo.setValidAmount(new BigDecimal(g.getValidbet()));
            vo.setWinLoss(new BigDecimal(g.getWinLoss()));
            vo.setBetAmount(new BigDecimal(g.getBet()));
            vo.setPlatform("wm");
            asyncService.executeAsync(vo);
        });
        log.info("GameRecord list.size {} pageCode {} pageSize {}",list.size(),pageCode,pageSize);
        if (list.size() == pageSize){
            pageCode++;
            recursionWm(pageCode,pageSize,maxId);
        }
    }

    private void recursionPg(Integer pageCode,Integer pageSize,Long maxId){
        log.info("GameRecordGoldenF pageCode {} pageSize {}",pageCode,pageSize);
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(pageCode-1, pageSize,sort);
        GameRecordGoldenF gameRecordGoldenF = new GameRecordGoldenF();
        gameRecordGoldenF.setId(maxId);
        Page<GameRecordGoldenF> gameRecordPage = gameRecordGoldenFService.findGameRecordGoldenFPage(gameRecordGoldenF, pageable,null,null);
        List<GameRecordGoldenF> list = gameRecordPage.getContent();
        if (list == null || list.isEmpty()){
            return;
        }
        list.forEach(g -> {
            ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
            vo.setOrderId(g.getTraceId());
            vo.setFirstProxy(g.getFirstProxy());
            vo.setSecondProxy(g.getSecondProxy());
            vo.setThirdProxy(g.getThirdProxy());
            vo.setOrderTimes(g.getCreateAtStr());
            vo.setUserId(g.getUserId());
            vo.setValidAmount(g.getBetAmount());
            vo.setWinLoss(g.getWinAmount().subtract(g.getBetAmount()));
            vo.setBetAmount(g.getBetAmount());
            vo.setPlatform(g.getVendorCode());
            asyncService.executeAsync(vo);
        });
        log.info("GameRecordGoldenF list.size {} pageCode {} pageSize {}",list.size(),pageCode,pageSize);
        if (list.size() == pageSize){
            pageCode++;
            recursionPg(pageCode,pageSize,maxId);
        }
    }

    private void beginWM1(){
        log.info("开始执行beginWM1");
        GameRecord byBetId = gameRecordService.findByBetId("179170088");
        log.info("beginWM1:byBetId{}",byBetId);
        for (int i=0;i<=num;i++){
            byBetId.setId(null);
            byBetId.setUserId(60951L);
            byBetId.setBetId(UserPasswordUtil.getRandomPwd()+ UserPasswordUtil.getRandomPwd());
            byBetId.setBetTime(DateUtil.dateToPatten(new Date()));
            gameRecordService.save(byBetId);
        }
    }
    private void beginWM2(){
        log.info("开始执行beginWM2");
        GameRecord byBetId = gameRecordService.findByBetId("179170087");
        for (int i=0;i<=num;i++){
            byBetId.setId(null);
            byBetId.setUserId(60952L);
            byBetId.setBetId(UserPasswordUtil.getRandomPwd()+ UserPasswordUtil.getRandomPwd());
            byBetId.setBetTime(DateUtil.dateToPatten(new Date()));
            gameRecordService.save(byBetId);
        }
    }
    private void beginPG1(){
        log.info("开始执行beginPG1");
        GameRecordGoldenF gameRecordById = gameRecordGoldenFService.findGameRecordById(6959L);
        log.info("beginPG1:gameRecordById{}",gameRecordById);
        Date date = new Date();
        String format = DateUtil.getSimpleDateFormat().format(date);
        for (int i=0;i<=num;i++){
            gameRecordById.setId(null);
            gameRecordById.setUserId(60835L);
            gameRecordById.setCreateAtStr(format);
            gameRecordById.setTraceId(UserPasswordUtil.getRandomPwd()+ UserPasswordUtil.getRandomPwd());
            gameRecordById.setCreateAtStr(DateUtil.dateToPatten(new Date()));
            gameRecordGoldenFService.save(gameRecordById);
        }
    }
    private void beginPG2(){
        GameRecordGoldenF gameRecordById = gameRecordGoldenFService.findGameRecordById(6927L);
        Date date = new Date();
        String format = DateUtil.getSimpleDateFormat().format(date);
        for (int i=0;i<=num;i++){
            gameRecordById.setId(null);
            gameRecordById.setUserId(60834L);
            gameRecordById.setCreateAtStr(format);
            gameRecordById.setTraceId(UserPasswordUtil.getRandomPwd()+ UserPasswordUtil.getRandomPwd());
            gameRecordById.setCreateAtStr(DateUtil.dateToPatten(new Date()));
            gameRecordGoldenFService.save(gameRecordById);
        }
    }
}

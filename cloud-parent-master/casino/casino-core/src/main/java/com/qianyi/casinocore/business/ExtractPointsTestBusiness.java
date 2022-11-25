package com.qianyi.casinocore.business;

import com.alibaba.fastjson.JSON;
import com.qianyi.casinocore.constant.GoldenFConstant;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ExtractPointsTestBusiness {

    @Autowired
    private ExtractPointsConfigBusiness extractPointsConfigBusiness;

    @Autowired
    UserService userService;

    @Autowired
    UserThirdService userThirdService;

    @Autowired
    PlatformConfigService platformConfigService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    AdGamesService adGamesService;

    public void testWM(){
        String result = "[{\"user\":\"9032e2ccd0164a6cb5adcef53d9385\",\"betId\":\"179122069\",\"betTime\":\"2022-02-28 12:57:47\",\"beforeCash\":\"4064.0000\",\"bet\":\"50.0000\",\"validbet\":\"50.0000\",\"water\":\"0.0000\",\"result\":\"50.0000\",\"betCode\":\"Player\",\"waterbet\":\"50.0000\",\"winLoss\":\"50.0000\",\"ip\":\"116.212.142.251\",\"gid\":\"101\",\"event\":\"112929592\",\"eventChild\":\"31\",\"round\":\"112929592\",\"subround\":\"31\",\"tableId\":\"206\",\"commission\":\"0\",\"settime\":\"2022-02-28 13:00:30\",\"reset\":\"N\",\"betResult\":\"闲\",\"gameResult\":\"庄:♠3♦K♣3 闲:♦3♥4\",\"gname\":\"百家乐\"}]";
        List<GameRecord> gameRecords = JSON.parseArray(result, GameRecord.class);
        for (GameRecord record : gameRecords) {
            record.setId(203166L);
            UserThird account = userThirdService.findByAccount(record.getUser());
            record.setUserId(account.getUserId());
            User user = userService.findById(record.getUserId());
            if (user != null) {
                record.setFirstProxy(user.getFirstProxy());
                record.setSecondProxy(user.getSecondProxy());
                record.setThirdProxy(user.getThirdProxy());
            }
            // 抽点
            extractPointsConfigBusiness.extractPoints(Constants.PLATFORM_WM, record);
        }
    }

    public void testPG(){
        String result = "[{\"player_name\":\"f26a3b90c16b40b29e03\",\"parent_bet_id\":\"1498252175305371648\",\"bet_id\":\"1498252175305371648\",\"trans_type\":\"Stake\",\"game_code\":\"mask-carnival\",\"currency\":\"USD\",\"bet_amount\":20,\"win_amount\":0,\"vendor_code\":\"PG\",\"wallet_code\":\"gf_main_balance\",\"created_at\":1646046129532,\"traceId\":\"f26a3b90c16b40b29e03_KK::PG::TRANSFEROUT::1498252175305371648-1498252175305371648-201-0\"},{\"player_name\":\"f26a3b90c16b40b29e03\",\"parent_bet_id\":\"1498252175305371648\",\"bet_id\":\"1498252175305371648\",\"trans_type\":\"Payoff\",\"game_code\":\"mask-carnival\",\"currency\":\"USD\",\"bet_amount\":0,\"win_amount\":0,\"vendor_code\":\"PG\",\"wallet_code\":\"gf_main_balance\",\"created_at\":1646046129596,\"traceId\":\"f26a3b90c16b40b29e03_KK::PG::TRANSFERIN::1498252175305371648-1498252175305371648-101-0\"}]";
        List<GameRecordGoldenF> recordGoldenFS = JSON.parseArray(result, GameRecordGoldenF.class);

        for (GameRecordGoldenF item: recordGoldenFS) {
            UserThird userThird = userThirdService.findByGoldenfAccount(item.getPlayerName());
            if(userThird == null)
                return;
            User user = userService.findById(userThird.getUserId());
            item.setUserId(userThird.getUserId());
            item.setFirstProxy(user.getFirstProxy());
            item.setSecondProxy(user.getSecondProxy());
            item.setThirdProxy(user.getThirdProxy());
            if(item.getCreatedAt()==null)
                log.info("{}",item);
            //item.setCreateAtStr(DateUtil.timeStamp2Date(item.getCreatedAt(),""));
            try {
                GameRecordGoldenF gameRecordGoldenF = gameRecordGoldenFService.findGameRecordGoldenFByTraceId(item.getTraceId());
                if(gameRecordGoldenF==null){
                    //gameRecordGoldenFService.save(item);
                }
                GameRecord gameRecord = combineGameRecord(gameRecordGoldenF==null ? item : gameRecordGoldenF);
                gameRecord.setFirstProxy(item.getFirstProxy());
                gameRecord.setSecondProxy(item.getSecondProxy());
                gameRecord.setThirdProxy(item.getThirdProxy());
                if(gameRecordGoldenF.getBetAmount().compareTo(BigDecimal.ZERO)==0)
                    return;
                if(!gameRecordGoldenF.getTransType().equals(GoldenFConstant.GOLDENF_STAKE))
                    return;
                // 抽点
                extractPointsConfigBusiness.extractPoints(gameRecordGoldenF.getVendorCode(), gameRecord);
                log.info("code: {}, record: {}", gameRecordGoldenF.getVendorCode(), gameRecord);
            }catch (Exception e){
                log.error("",e);
            }
        }
    }

    private GameRecord combineGameRecord(GameRecordGoldenF item) {
        AdGame adGame = adGamesService.findByGamePlatformNameAndGameCode(item.getVendorCode(),item.getGameCode());
        GameRecord gameRecord = new GameRecord();
        gameRecord.setBetId(item.getBetId());
        gameRecord.setValidbet(item.getBetAmount().toString());
        gameRecord.setUserId(item.getUserId());
        gameRecord.setGameCode(adGame.getGameCode());
        gameRecord.setGname(adGame.getGameName());
        gameRecord.setBetTime(item.getCreateAtStr());
        gameRecord.setId(item.getId());
        return gameRecord;
    }

}

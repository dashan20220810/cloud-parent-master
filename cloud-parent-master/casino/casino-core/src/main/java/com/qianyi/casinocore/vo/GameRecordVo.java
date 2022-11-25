package com.qianyi.casinocore.vo;

import com.qianyi.casinocore.model.GameRecord;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GameRecordVo implements Serializable {
    private static final long serialVersionUID = -6975317983240305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "我方会员账号")
    private String account;
    @ApiModelProperty(value = "我方账号")
    private Long userId;
    @ApiModelProperty(value = "三方会员账号")
    private String user;
    @ApiModelProperty(value = "注单号")
    private String betId;
    @ApiModelProperty(value = "下注時間")
    private String betTime;
    @ApiModelProperty(value = "下注前金额")
    private String beforeCash;
    @ApiModelProperty(value = "下注金额")
    private String bet;
    @ApiModelProperty(value = "有效下注")
    private String validbet;
    @ApiModelProperty(value = "退水金额")
    private String water;
    @ApiModelProperty(value = "下注结果")
    private String result;
    @ApiModelProperty(value = "下注代碼")
    private String betCode;
    @ApiModelProperty(value = "下注内容")
    private String betResult;
    @ApiModelProperty(value = "下注退水金额")
    private String waterbet;
    @ApiModelProperty(value = "输赢金额")
    private String winLoss;
    @ApiModelProperty(value = "ip")
    private String ip;
    @ApiModelProperty(value = "游戏类别编号")
    private Integer gid;
    @ApiModelProperty(value = "桌台编号")
    private String tableId;
    @ApiModelProperty(value = "牌型ex:庄:♦3♦3 闲:♥9♣10")
    private String gameResult;
    @ApiModelProperty(value = "游戏名称ex:百家乐")
    private String gname;
    @ApiModelProperty(value = "0:一般, 1:免佣")
    private Integer commission;
    @ApiModelProperty(value = "Y:有重对, N:非重对")
    private String reset;
    @ApiModelProperty(value = "结算时间")
    private String settime;
    @ApiModelProperty(value = "电子游戏代码")
    private String slotGameId;
    @ApiModelProperty(value = "洗码状态：0：成功，1：失败")
    private Integer washCodeStatus;
    @ApiModelProperty(value = "打码状态：0：成功，1：失败")
    private Integer codeNumStatus;
    @ApiModelProperty(value = "分润状态：0：成功，1：失败")
    private Integer shareProfitStatus;
    @ApiModelProperty("总代ID")
    private Long firstProxy;
    @ApiModelProperty("区域代理ID")
    private Long secondProxy;
    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;
    public GameRecordVo(GameRecord gameRecord){
        this.id = gameRecord.getId();
        this.userId = gameRecord.getUserId();
        this.user = gameRecord.getUser();
        this.betId = gameRecord.getBetId();
        this.betTime = gameRecord.getBetTime();
        this.beforeCash = gameRecord.getBeforeCash();
        this.bet = gameRecord.getBet();
        this.validbet = gameRecord.getValidbet();
        this.water = gameRecord.getWater();
        this.result = gameRecord.getResult();
        this.betCode = gameRecord.getBetCode();
        this.betResult = gameRecord.getBetResult();
        this.waterbet = gameRecord.getWaterbet();
        this.winLoss = gameRecord.getWinLoss();
        this.ip = gameRecord.getIp();
        this.gid = gameRecord.getGid();
        this.tableId = gameRecord.getTableId();
        this.gameResult = gameRecord.getGameResult();
        this.gname = gameRecord.getGname();
        this.commission = gameRecord.getCommission();
        this.reset = gameRecord.getReset();
        this.settime = gameRecord.getSettime();
        this.slotGameId = gameRecord.getSlotGameId();
        this.washCodeStatus = gameRecord.getWashCodeStatus();
        this.codeNumStatus = gameRecord.getCodeNumStatus();
        this.shareProfitStatus = gameRecord.getShareProfitStatus();
        this.firstProxy = gameRecord.getFirstProxy();
        this.secondProxy = gameRecord.getSecondProxy();
        this.thirdProxy = gameRecord.getThirdProxy();
    }

    public GameRecordVo() {
    }
}

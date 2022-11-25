package com.qianyi.casinoweb.vo;

import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
@ApiModel("OB游戏记录Vo")
public class GameRecordObdjVo {

    @ApiModelProperty(value = "注单号")
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long memberId;

    @ApiModelProperty(value = "用户账户")
    private String memberAccount;

    @ApiModelProperty(value = "商户ID")
    private Long merchantId;

    @ApiModelProperty(value = "商户账号")
    private String merchantAccount;

    @ApiModelProperty(value = "父商户ID")
    private Long parentMerchantId;

    @ApiModelProperty(value = "父商户账")
    private String parentMerchantAccount;

    @ApiModelProperty(value = "是否测试")
    private Integer tester;

    @ApiModelProperty(value = "注单类型1-普通注单2-串关注单 3-局内串关4-复合玩法")
    private Integer orderType;

    @ApiModelProperty(value = "串关类型1普通注单 2:2 串1 3:3 串1 .")
    private Integer parleyType;

    @ApiModelProperty(value = "游戏ID")
    private Long gameId;

    @ApiModelProperty(value = "联赛ID")
    private Long tournamentId;

    @ApiModelProperty(value = "赛事ID")
    private Long matchId;

    @ApiModelProperty(value = "赛事类型1-正常")
    private Integer matchType;

    @ApiModelProperty(value = "盘口ID")
    private Long marketId;

    @ApiModelProperty(value = "盘口中文名称")
    private String marketCnName;

    @ApiModelProperty(value = "队伍名称，主客队用,拼 接")
    private String teamNames;

    @ApiModelProperty(value = "投注项ID")
    private Long oddId;

    @ApiModelProperty(value = "投注项名称")
    private String oddName;

    @ApiModelProperty(value = "第几局")
    private Integer round;

    @ApiModelProperty(value = "赔率")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal odd;

    @ApiModelProperty(value = "投注金额（元）")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "派彩金额（元）")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal winAmount;

    @ApiModelProperty(value = "赛事阶段1-初盘 2-滚球")
    private Integer isLive;

    @ApiModelProperty(value = "注单状态3-待结算4-已取消5-赢(已中奖) 6-输(未中奖) 7-已撤销8-赢半9-输半10-走水")
    private Integer betStatus;

    @ApiModelProperty(value = "确认方式1-自动确认 2-手动待确认3-手动确认 4-手动拒绝")
    private Integer confirmType;

    @ApiModelProperty(value = "投注时间（毫秒）")
    private Long betTime;

    @ApiModelProperty(value = "投注时间（yyyy-MM-dd HH:mm:ss）")
    private String betStrTime;

    @ApiModelProperty(value = "结算时间（秒）")
    private Long settleTime;

    @ApiModelProperty(value = "结算时间（yyyy-MM-dd HH:mm:ss）")
    private String setStrTime;

    @ApiModelProperty(value = "赛事开始时间（秒）")
    private Long matchStartTime;

    @ApiModelProperty(value = "修改时间（秒）")
    private Long updateTime;

    @ApiModelProperty(value = "结算次数")
    private Integer settleCount;

    @ApiModelProperty(value = "战队id，主客队id 用,拼接")
    private String teamId;

    @ApiModelProperty(value = "投注ip")
    private Long betIp;

    @ApiModelProperty(value = "设备 1- PC 2-H5 3-Android 4-IOS")
    private Integer device;

    @ApiModelProperty(value = "队伍中文名称，主客队用,拼接")
    private String teamCnNames;

    @ApiModelProperty(value = "队伍英文名称，主客队用,拼接")
    private String teamEnNames;

    @ApiModelProperty(value = "基准比分")
    private String score;

    @ApiModelProperty(value = "币种编码")
    private Integer currencyCode;

    @ApiModelProperty(value = "币种编码")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal exchangeRate;

}

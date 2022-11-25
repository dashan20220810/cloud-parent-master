package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
@ApiModel("OB游戏记录明细Vo")
public class GameRecordObdjDetailVo {

    @ApiModelProperty(value = "串关注单明细ID")
    private Long id;

    @ApiModelProperty(value = "注单ID")
    private Long orderId;

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

    @ApiModelProperty(value = "第几局")
    private Integer round;

    @ApiModelProperty(value = "投注项ID")
    private Long oddId;

    @ApiModelProperty(value = "赛事阶段1-初盘 2-滚球")
    private Integer isLive;

    @ApiModelProperty(value = "投注项名称")
    private String oddName;

    @ApiModelProperty(value = "赔率")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal odd;

    @ApiModelProperty(value = "注单状态3-待结算4-已取消5-赢(已中奖) 6-输(未中奖) 7-已撤销8-赢半9-输半10-走水")
    private Integer status;

    @ApiModelProperty(value = "投注时间（毫秒）")
    private Long betTime;

    @ApiModelProperty(value = "赛事开始时间（秒）")
    private Long matchStartTime;

    @ApiModelProperty(value = "修改时间（秒）")
    private Long updateTime;

    @ApiModelProperty(value = "结算时间（秒）")
    private Long settleTime;

    @ApiModelProperty(value = "结算次数")
    private Integer settleCount;

    @ApiModelProperty(value = "战队id，主客队id 用,拼接")
    private String teamId;

    @ApiModelProperty(value = "队伍中文名称，主客队用,拼接")
    private String teamCnNames;

    @ApiModelProperty(value = "队伍英文名称，主客队用,拼接")
    private String teamEnNames;

}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="game_record_obty_detail",uniqueConstraints={@UniqueConstraint(columnNames={"betNo"})})
@ApiModel("OB体育游戏记录详情")
public class GameRecordObtyDetail extends BaseEntity{

    @ApiModelProperty(value = "注单Id")
    private Long betNo;

    @ApiModelProperty(value = "投注项Id")
    private Long playOptionsId;

    @ApiModelProperty(value = "赛事ID")
    private Long matchId;

    @ApiModelProperty(value = "比赛开始时间")
    private Long beginTime;

    @ApiModelProperty(value = "注单金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "联赛名称")
    private String matchName;

    @ApiModelProperty(value = "比赛对阵")
    private String matchInfo;

    @ApiModelProperty(value = "投注类型1 ：早盘赛事 ，2： 滚球盘赛事，3： 冠军盘赛事")
    private Integer matchType;

    @ApiModelProperty(value = "赛种ID")
    private Integer sportId;

    @ApiModelProperty(value = "游戏名称")
    private String sportName;

    @ApiModelProperty(value = "联赛ID")
    private Long tournamentId;

    @ApiModelProperty(value = "投注项名称")
    private String playOptionName;

    @ApiModelProperty(value = "玩法名称")
    private String playName;

    @ApiModelProperty(value = "投注项(如:主客队)")
    private String playOptions;

    @ApiModelProperty(value = "玩法ID")
    private Integer playId;

    @ApiModelProperty(value = "盘口类型")
    private String marketType;

    @ApiModelProperty(value = "盘口值")
    private String marketValue;

    @ApiModelProperty(value = "让球值")
    private String handicap;

    @ApiModelProperty(value = "结算比分(该字段谨慎使用,由于数据商未下发此数据,该字段为我方根据赛事事件处理后获取)")
    private String settleScore;

    @ApiModelProperty(value = "基准分")
    private String scoreBenchmark;

    @ApiModelProperty(value = "赔率")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal oddsValue;

    @ApiModelProperty(value = "原始赔率")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal oddsFinally;

    @ApiModelProperty(value = "注单结算结果0-无结果 2-走水 3-输 4-赢 5-赢一半 6-输一半")
    private String betResult;
}

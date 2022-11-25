package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(
        name="getStatisticsResult",
        classes = {
                @ConstructorResult(
                        targetClass = com.qianyi.casinocore.vo.CompanyOrderAmountVo.class,
                        columns={
                            @ColumnResult(name="first_proxy",type = Long.class),
                                @ColumnResult(name="second_proxy",type = Long.class),
                                @ColumnResult(name="third_proxy",type = Long.class),
                                @ColumnResult(name="player_num",type = Integer.class),
                                @ColumnResult(name="bet_time",type = String.class),
                                @ColumnResult(name="validbet",type = String.class)
                        }
                )
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyOrderAmountVo {

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty("玩家数")
    private Integer playerNum;


    @ApiModelProperty("用户ID")
    private Long userId;
    /**
     * 下注時間
     */
    @ApiModelProperty(value = "下注時間")
    private String betTime;


    /**
     * 有效下注
     */
    @ApiModelProperty(value = "有效下注")
    private String validbet;


    /**
     * 游戏类型：1:WM,2:PG,3:CQ9
     */
    @ApiModelProperty("游戏类型：1:WM,2:PG,3:CQ9")
    private Integer gameType;
}

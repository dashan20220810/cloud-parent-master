package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.id.IntegralDataTypeHolder;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ShareProfitBO {

    @ApiModelProperty(value = "源用户ID")
    private Long fromUserId;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "投注金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betAmount;
    @ApiModelProperty(value = "分润金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "是否第一次下注")
    private boolean isFirst;
    @ApiModelProperty(value = "下注时间")
    private String betTime;
    @ApiModelProperty(value = "是否直属")
    private boolean direct;
    @ApiModelProperty(value = "返佣比例")
    private BigDecimal commission;
    @ApiModelProperty(value = "上级层级")
    private Integer parentLevel;
    @ApiModelProperty(value = "用户账户")
    private String account;


    @ApiModelProperty(value = "游戏账户ID")
    private Long recordId;

    @ApiModelProperty(value = "游戏账户用户ID")
    private Long recordUserId;

    @ApiModelProperty(value = "游戏账户下注ID")
    private String recordBetId;

    @ApiModelProperty(value = "下注时间 年月日时分秒")
    private Date betDate;

    /**
     * 游戏类型：1:WM,2:PG,3:CQ9
     */
    @ApiModelProperty("游戏类型：1:WM,2:PG,3:CQ9")
    private Integer gameType;

}

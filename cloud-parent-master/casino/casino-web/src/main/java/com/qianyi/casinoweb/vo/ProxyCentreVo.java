package com.qianyi.casinoweb.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 代理中心佣金
 */
@Data
@ApiModel("代理中心")
public class ProxyCentreVo {

    @ApiModelProperty(value = "今日佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal todayCommission;

    @ApiModelProperty(value = "昨日佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal yesterdayCommission;

    @ApiModelProperty(value = "本周佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal weekCommission;

    @Data
    @ApiModel("我的团队")
    public static class MyTeam {

        @ApiModelProperty(value = "今日存款")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal deppositeAmount;

        @ApiModelProperty(value = "今日有效投注")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal betAmount;

        @ApiModelProperty("今日新增")
        private Integer newNum = 0;

        @ApiModelProperty("团队总人数")
        private Integer allGroupNum = 0;

        @ApiModelProperty("直属人数")
        private Integer directGroupNum = 0;
    }

    @Data
    @ApiModel("业绩查询")
    public static class ShareProfit {
        @ApiModelProperty("用户ID")
        private Long userId;
        @ApiModelProperty("账号")
        private String account;
        @ApiModelProperty("直属总业绩")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal directBetAmount = BigDecimal.ZERO;
        @ApiModelProperty("直属总贡献")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal directProfitAmount = BigDecimal.ZERO;
        @ApiModelProperty("附属总贡献")
        @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
        private BigDecimal otherProfitAmount = BigDecimal.ZERO;
    }
}

package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CompanyProxyDetailVo implements Serializable {
    private static final long serialVersionUID = -6975317983298432179L;
    @ApiModelProperty(value = "代理id")
    private Long proxyUserId;
    @ApiModelProperty(value = "账号")
    private String userName;
    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "统计时段")
    private String staticsTimes;
    @ApiModelProperty(value = "创造业绩的玩家数")
    private Integer playerNum;
    @ApiModelProperty(value = "团队业绩流水")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal groupBetAmount;
    @ApiModelProperty(value = "返佣级别")
    private String profitLevel;
    @ApiModelProperty(value = "返佣比例")
    private String profitRate;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "团队总返佣")
    private BigDecimal groupTotalprofit;
    @ApiModelProperty(value = "佣金分成比")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal benefitRate;
    @ApiModelProperty(value = "个人结算佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "结清状态")
    private String settleStatus;
}

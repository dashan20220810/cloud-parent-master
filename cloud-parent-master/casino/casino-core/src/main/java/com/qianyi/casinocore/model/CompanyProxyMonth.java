package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProxyMonth extends BaseEntity implements Cloneable{

    @ApiModelProperty(value = "用户id")
    private Long userId;

    private Long userIdTemp;
    @ApiModelProperty("代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "总代理")
    private Long firstProxy;
    @ApiModelProperty(value = "区域代理")
    private Long secondProxy;
    @ApiModelProperty(value = "基层代理")
    private Long thirdProxy;
    @ApiModelProperty(value = "统计时段")
    private String staticsTimes;
    @ApiModelProperty(value = "创造业绩的玩家数")
    private Integer playerNum;
    @ApiModelProperty(value = "团队业绩流水")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal groupBetAmount;


    @ApiModelProperty(value = "团队总返佣")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal groupTotalprofit;
    @ApiModelProperty(value = "佣金分成比")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal benefitRate;
    @ApiModelProperty(value = "个人结算佣金")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal profitAmount;

    @ApiModelProperty(value = "抽点金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal extractPointsAmount;

    @ApiModelProperty(value = "结清状态 0 未结清 1已结清")
    private Integer settleStatus;

    @Override
    public Object clone() {
        CompanyProxyMonth companyProxyMonth = null;
        try{
            companyProxyMonth = (CompanyProxyMonth)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return companyProxyMonth;
    }
}

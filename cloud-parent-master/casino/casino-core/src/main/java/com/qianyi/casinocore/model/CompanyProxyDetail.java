package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("公司代理明细")
public class CompanyProxyDetail extends BaseEntity implements Cloneable{

    @ApiModelProperty(value = "用户id")
    private Long userId;
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
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal groupBetAmount;
    @ApiModelProperty(value = "返佣级别")
    private String profitLevel;
    @ApiModelProperty(value = "返佣比例")
    private String profitRate;
    @ApiModelProperty(value = "返佣金额线")
    private String profitAmountLine;

    @ApiModelProperty(value = "团队总返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal groupTotalprofit;
    @ApiModelProperty(value = "佣金分成比")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal benefitRate;
    @ApiModelProperty(value = "个人结算佣金")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "结清状态 0 未结清 1已结清")
    private Integer settleStatus;
    @ApiModelProperty(value = "投注时间")
    private LocalDateTime betTime;
    /**
     * 游戏类型：1:WM,2:PG,3:CQ9
     */
    @ApiModelProperty("游戏类型：1:WM,2:PG,3:CQ9")
    private Integer gameType;

    @Override
    public Object clone() {
        CompanyProxyDetail companyProxyDetail = null;
        try{
            companyProxyDetail = (CompanyProxyDetail)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return companyProxyDetail;
    }

}

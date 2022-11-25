package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("代理返佣等级配置")
public class ProxyRebateConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 游戏类型：1:WM,2:PG,3:CQ9
     */
    @ApiModelProperty("游戏类型：1:WM,2:PG,3:CQ9,4:OBDJ, 5:OBTY, 6:SABA")
    private Integer gameType;

    @ApiModelProperty("总代理id")
    private Long proxyUserId;

    @ApiModelProperty("第一级业绩额度")
    private Integer firstMoney;

    @ApiModelProperty("第一级返佣金额线")
    private BigDecimal firstAmountLine;

    @ApiModelProperty("第一级返佣比例")
    private BigDecimal firstProfit;



    @ApiModelProperty("第二级业绩额度")
    private Integer secondMoney;

    @ApiModelProperty("第二级返佣金额线")
    private BigDecimal secondAmountLine;

    @ApiModelProperty("第二级返佣比例")
    private BigDecimal secondProfit;



    @ApiModelProperty("第三级业绩额度")
    private Integer thirdMoney;

    @ApiModelProperty("第三级返佣金额线")
    private BigDecimal thirdAmountLine;

    @ApiModelProperty("第三级返佣比例")
    private BigDecimal thirdProfit;



    @ApiModelProperty("第四级业绩额度")
    private Integer fourMoney;

    @ApiModelProperty("第四级返佣金额线")
    private BigDecimal fourAmountLine;

    @ApiModelProperty("第四级返佣比例")
    private BigDecimal fourProfit;



    @ApiModelProperty("第五级业绩额度")
    private Integer fiveMoney;

    @ApiModelProperty("第五级返佣金额线")
    private BigDecimal fiveAmountLine;

    @ApiModelProperty("第五级返佣比例")
    private BigDecimal fiveProfit;



    @ApiModelProperty("第六级业绩额度")
    private Integer sixMoney;

    @ApiModelProperty("第六级返佣金额线")
    private BigDecimal sixAmountLine;

    @ApiModelProperty("第六级返佣比例")
    private BigDecimal sixProfit;



    @ApiModelProperty("第七级业绩额度")
    private Integer sevenMoney;

    @ApiModelProperty("第七级返佣金额线")
    private BigDecimal sevenAmountLine;

    @ApiModelProperty("第七级返佣比例")
    private BigDecimal sevenProfit;



    @ApiModelProperty("第八级业绩额度")
    private Integer eightMoney;


    @ApiModelProperty("第八级返佣金额线")
    private BigDecimal eightAmountLine;

    @ApiModelProperty("第八级返佣比例")
    private BigDecimal eightProfit;
}

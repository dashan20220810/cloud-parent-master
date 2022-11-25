package com.qianyi.casinocore.vo;

import com.qianyi.modulecommon.executor.JobSuperVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
public class ProxyGameRecordReportVo extends JobSuperVo{

    @ApiModelProperty(value = "注单唯一标识")
    private String orderId;

    @ApiModelProperty(value = "会员id")
    private Long userId;

    @ApiModelProperty(value = "游戏记录表ID")
    private Long gameRecordId;
    /**
     * 投注金额
     */
    @ApiModelProperty(value = "投注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal betAmount;
    /**
     * 有效下注
     */
    @ApiModelProperty(value = "有效下注")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal validAmount;
    /**
     * 输赢金额
     */
    @ApiModelProperty(value = "输赢金额")
    private BigDecimal winLoss;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty(value = "下注时间yyyy-MM-dd HH:mm:ss")
    private String orderTimes;

    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;
}

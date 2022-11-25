package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordObtyVo implements Serializable {

    private static final long serialVersionUID = -6485296312379L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "我方会员账号")
    private String account;
    @ApiModelProperty(value = "我方账号")
    private Long userId;
    @ApiModelProperty(value = "用户名称")
    private String userName;
    @ApiModelProperty(value = "订单Id")
    private String orderNo;
    @ApiModelProperty(value = "投注时间(yyyy-MM-dd)")
    private String betStrTime;
    @ApiModelProperty(value = "结算时间(yyyy-MM-dd)")
    private String settleStrTime;
    @ApiModelProperty(value = "订单实际投注金额")
    private BigDecimal orderAmount;
    @ApiModelProperty(value = "结算金额")
    private BigDecimal settleAmount;
    @ApiModelProperty(value = "盈利金额")
    private BigDecimal profitAmount;
    @ApiModelProperty(value = "订单结算结果0-无结果 2-走水 3-输 4-赢 5-赢一半 6-输一半")
    private Integer outcome;
}

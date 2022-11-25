package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GameRecordObdjVo implements Serializable {

    private static final long serialVersionUID = -6974485123379L;

    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "我方会员账号")
    private String account;
    @ApiModelProperty(value = "我方账号")
    private Long userId;
    @ApiModelProperty(value = "用户账户")
    private String memberAccount;
    @ApiModelProperty(value = "投注项ID")
    private Long oddId;
    @ApiModelProperty(value = "投注项名称")
    private String oddName;
    @ApiModelProperty(value = "第几局")
    private Integer round;
    @ApiModelProperty(value = "注单号")
    private Long betId;
    @ApiModelProperty(value = "赔率")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal odd;
    @ApiModelProperty(value = "投注金额（元）")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal betAmount;
    @ApiModelProperty(value = "派彩金额（元）")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal winAmount;
    @ApiModelProperty(value = "输赢金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal winLoss;
    @ApiModelProperty(value = "注单状态3-待结算4-已取消5-赢(已中奖) 6-输(未中奖) 7-已撤销8-赢半9-输半10-走水")
    private Integer betStatus;
    @ApiModelProperty(value = "投注时间（yyyy-MM-dd HH:mm:ss）")
    private String betStrTime;
    @ApiModelProperty(value = "结算时间（yyyy-MM-dd HH:mm:ss）")
    private String setStrTime;

}

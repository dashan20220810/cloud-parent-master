package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("WM交易记录")
public class WmMemberTradeReportVo {

    @ApiModelProperty(value = "该次交易单号")
    private String orderid;

    @ApiModelProperty(value = "该次交易纪录时间")
    private String addtime;

    @ApiModelProperty(value = "该次交易金额")
    private String money;

    @ApiModelProperty(value = "加扣点代码:加点(121)扣点(122)")
    private Integer op_code;

    @ApiModelProperty(value = "此用户目前余额")
    private String subtotal;

    @ApiModelProperty(value = "该次交易贵公司自订单号")
    private String ordernum;

    @ApiModelProperty(value = "该次交易的帐号")
    private String user;
}

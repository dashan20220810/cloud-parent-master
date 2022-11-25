package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SendMessageVo implements Serializable {
    private static final long serialVersionUID = -7075321368092385978L;

    @ApiModelProperty("短信余额警报值")
    private BigDecimal sendMessageWarning;

    @ApiModelProperty("名称")
    private String name;
}

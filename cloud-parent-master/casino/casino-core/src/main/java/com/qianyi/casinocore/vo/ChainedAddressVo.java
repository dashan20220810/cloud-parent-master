package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChainedAddressVo implements Serializable {

    private static final long serialVersionUID = -15696455605179L;

    @ApiModelProperty("地址")
    private String url;

    @ApiModelProperty("二维码")
    private String qrCode;
}

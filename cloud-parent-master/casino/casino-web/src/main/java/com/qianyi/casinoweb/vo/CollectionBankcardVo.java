package com.qianyi.casinoweb.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class CollectionBankcardVo{

    private Long id;

    /**
     * 银行卡账号
     */
    @ApiModelProperty(value = "银行账号")
    private String bankNo;
    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    /**
     * 开户名
     */
    @ApiModelProperty(value = "开户名")
    private String accountName;
}

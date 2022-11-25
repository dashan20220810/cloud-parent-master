package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("代理抽点默认配置")
public class ExtractPointsConfig extends BaseEntity{

    @ApiModelProperty(value = "游戏ID")
    private String gameId;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "游戏英文名称")
    private String gameEnName;

    @ApiModelProperty(value = "平台")
    private String platform;

    @ApiModelProperty(value = "抽点比例: 比例限制范围 0%~5%")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal rate = BigDecimal.ZERO;

    @ApiModelProperty(value = "状态：0:禁用，1:启用")
    private Integer state;
}

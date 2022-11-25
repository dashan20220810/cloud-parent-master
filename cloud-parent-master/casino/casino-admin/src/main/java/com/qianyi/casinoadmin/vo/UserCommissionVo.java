package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 玩家代理返佣设置
 */
@Data
@Builder
public class UserCommissionVo implements Serializable {
    private static final long serialVersionUID = -260020079409291257L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("一级玩家返佣")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal firstCommission;

    @ApiModelProperty("二级玩家返佣")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal secondCommission;

    @ApiModelProperty("三级玩家返佣")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal thirdCommission;
}

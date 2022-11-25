package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "`order`")
public class Order extends BaseEntity {
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("订单号")
    private String no;
    @ApiModelProperty("金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal money;
    //1.未确认。 2.成功   3.失败
    @ApiModelProperty("1.未确认。 2.成功   3.失败")
    private Integer state;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("平台名称: WM, PG/CQ9, OBDJ, OBTY, SABA")
    @Column(columnDefinition = "varchar(16) default 'WM'")
    private String gamePlatformName;

    /**
     * 0.转入，1.转出
     */
    @ApiModelProperty("0.转入，1.转出")
    private Integer type;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;
}

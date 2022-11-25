package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(indexes = {@Index(columnList = "userId"),@Index(columnList = "fromUserId"),@Index(columnList = "betTime")})
public class ShareProfitChange extends BaseEntity {
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty("会员账号")
    private String account;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "账变类型 1:分润,2:充值")
    private Integer type;

    @ApiModelProperty(value = "额度变化")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal amount;

    @ApiModelProperty(value = "额度变化前")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal amountBefore;

    @ApiModelProperty(value = "额度变化后")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal amountAfter;

    @ApiModelProperty("返佣比例")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal profitRate;

    @ApiModelProperty("贡献者")
    private Long fromUserId;

    @ApiModelProperty("返佣等级")
    private Integer parentLevel;

    @ApiModelProperty("有效投注金额")
    private BigDecimal validbet;


    @ApiModelProperty(value = "下注时间")
    private Date betTime;

    /**
     * 游戏类型：1:WM,2:PG,3:CQ9
     */
    @ApiModelProperty("游戏类型：1:WM,2:PG,3:CQ9,4:OBDJ,5:OBTY,6:SABA")
    private Integer gameType;
    public ShareProfitChange() {
    }

    public ShareProfitChange(Long userId,Long fromUserId, BigDecimal amount, BigDecimal validbet) {
        this.userId = userId;
        this.fromUserId = fromUserId;
        this.amount = amount;
        this.validbet = validbet;
    }
}

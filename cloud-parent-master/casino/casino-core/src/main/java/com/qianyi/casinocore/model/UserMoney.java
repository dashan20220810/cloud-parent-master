package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="user_money",uniqueConstraints={@UniqueConstraint(columnNames={"userId"})})
public class UserMoney extends BaseEntity{

    private Long userId;

    @ApiModelProperty("中心余额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal money=BigDecimal.ZERO;

    @ApiModelProperty("打码量")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal codeNum=BigDecimal.ZERO;

    @ApiModelProperty("洗码金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal washCode = BigDecimal.ZERO;

    @ApiModelProperty("冻结余额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal freezeMoney = BigDecimal.ZERO;

    @ApiModelProperty("分润余额")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal shareProfit = BigDecimal.ZERO;

    @ApiModelProperty("实时余额仅用于打码量清零点计算")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal balance = BigDecimal.ZERO;

    @ApiModelProperty("是否首充 0 是 1 不是")
    private Integer isFirst;

    /**
     * 没有这个字段下面的计算可提现金额方法，redis缓存取的时候会报找不到withdrawMoney字段
     */
    @Transient
    @ApiModelProperty("可提现余额")
    private BigDecimal withdrawMoney = BigDecimal.ZERO;

    //计算可提现金额
    public BigDecimal getWithdrawMoney(){
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        //打码量为0时才有可提现金额
        if (this.codeNum != null && BigDecimal.ZERO.compareTo(this.codeNum) == 0) {
            BigDecimal money = this.money == null ? defaultVal : this.money;
            return money.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return defaultVal;
    }
}

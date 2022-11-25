package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("会员每日流水表")
@Table(indexes = {@Index(name="identity_index",columnList = "userId",unique=true),@Index(name="identity_index",columnList = "staticsTimes",unique=true)
        ,@Index(columnList = "firstProxy"),@Index(columnList = "secondProxy"),@Index(columnList = "thirdProxy")})
public class UserRunningWater extends BaseEntity {
    @ApiModelProperty(value = "会员id")
    private Long userId;

    @ApiModelProperty(value = "当日流水金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal amount;

    @ApiModelProperty(value = "当日贡献佣金")
    @Column(columnDefinition = "Decimal(15,6) default '0.00'")
    private BigDecimal commission;

    @ApiModelProperty(value = "统计时间段")
    private String staticsTimes;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    public UserRunningWater(){

    }
    public UserRunningWater(Long userId){
        this.userId = userId;
    }
}

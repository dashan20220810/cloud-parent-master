package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("洗码明细表")
@NoArgsConstructor
@Table(name ="extract_points_change",uniqueConstraints={@UniqueConstraint(columnNames={"platform","gameRecordId"})})
public class ExtractPointsChange extends BaseEntity{

    @ApiModelProperty(value = "抽点金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal amount;

    @ApiModelProperty(value = "游戏ID")
    private String gameId;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "游戏记录ID")
    private Long gameRecordId;

    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;

    @ApiModelProperty(value = "抽点比例: 比例限制范围 0%~5%")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal rate = BigDecimal.ZERO;

    @ApiModelProperty(value = "基层代代理Id")
    private Long poxyId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;

}

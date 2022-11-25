package com.qianyi.casinocore.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("资金详情")
public class AccountChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "订单号")
	private String orderNo;

	@ApiModelProperty(value = "账变类型:0.洗码领取,7.转入wm,8.一键回收(转出WM),9.代理佣金领取," +
			"10.转入PC/CQ9,11.转出PC/CQ9,13.OB电竞转入 14.OB电竞转出,15.OB体育转入，16.OB体育转出, 17.沙巴体育转入，18.沙巴体育转出")
	private Integer type;

	@ApiModelProperty(value = "额度变化")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	@JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
	private BigDecimal amount;

	@ApiModelProperty(value = "额度变化前")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	@JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
	private BigDecimal amountBefore;

	@ApiModelProperty(value = "额度变化后")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	@JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
	private BigDecimal amountAfter;

	@ApiModelProperty("总代ID")
	private Long firstProxy;

	@ApiModelProperty("区域代理ID")
	private Long secondProxy;

	@ApiModelProperty("基层代理ID")
	private Long thirdProxy;
}

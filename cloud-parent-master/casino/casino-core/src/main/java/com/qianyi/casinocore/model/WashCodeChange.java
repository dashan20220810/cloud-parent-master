package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("洗码明细表")
@Table(name ="wash_code_change",uniqueConstraints={@UniqueConstraint(columnNames={"platform","gameRecordId"})},indexes = {@Index(columnList = "userId,platform,createTime")})
public class WashCodeChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "游戏记录ID")
	private Long gameRecordId;

	@ApiModelProperty(value = "平台:wm,PG,CQ9")
	private String platform;

	@ApiModelProperty(value = "游戏ID")
	private String gameId;

	@ApiModelProperty(value = "游戏名称")
	private String gameName;

	@ApiModelProperty(value = "有效投注额")
	private BigDecimal validbet;

	@ApiModelProperty(value = "返水比例")
	private BigDecimal rate;

	@ApiModelProperty(value = "洗码金额")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	private BigDecimal amount;

	public WashCodeChange(String platform, String gameId, String gameName, BigDecimal amount,BigDecimal validbet) {
		this.platform = platform;
		this.gameId = gameId;
		this.gameName = gameName;
		this.amount = amount == null ? BigDecimal.ZERO : amount;
		this.validbet = validbet == null ? BigDecimal.ZERO : validbet;
	}

	public WashCodeChange() {
	}
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("打码明细表")
@Table(name ="code_num_change",uniqueConstraints={@UniqueConstraint(columnNames={"platform","gameRecordId"})})
public class CodeNumChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "游戏记录ID")
	private Long gameRecordId;

	@ApiModelProperty(value = "平台:wm,PG,CQ9")
	private String platform;

	@ApiModelProperty(value = "注单号")
	private String betId;

	@ApiModelProperty(value = "打码量")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	private BigDecimal amount;

	@ApiModelProperty(value = "打码量变化前")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	private BigDecimal amountBefore;

	@ApiModelProperty(value = "打码量变化后")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	private BigDecimal amountAfter;

	@ApiModelProperty(value = "清零值")
	@Column(columnDefinition = "Decimal(19,6) default '0.00'")
	private BigDecimal clearCodeNum;

	@ApiModelProperty(value = "0:有效投注，1:清0点，2充值 3总控上分 4代理上分")
	private Integer type;

	public static CodeNumChange setCodeNumChange(Long userId, GameRecord record, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter) {
		CodeNumChange codeNumChange = new CodeNumChange();
		codeNumChange.setUserId(userId);
		if (record != null) {
			codeNumChange.setGameRecordId(record.getId());
			codeNumChange.setBetId(record.getBetId());
		}
		codeNumChange.setAmount(amount);
		codeNumChange.setAmountBefore(amountBefore);
		codeNumChange.setAmountAfter(amountAfter);
		return codeNumChange;
	}
}

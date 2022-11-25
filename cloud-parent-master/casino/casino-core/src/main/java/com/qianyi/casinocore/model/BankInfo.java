package com.qianyi.casinocore.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("银行列表")
public class BankInfo extends BaseEntity {
	/**
	 * 银行名
	 */
	@Column(unique = true)
	@ApiModelProperty(value = "银行名称")
	private String bankName;
	@ApiModelProperty(value = "银行图标")
	private String bankLogo;
	/** 银行类型：默认0:银行卡，1：支付宝，2：微信，3：QQ */
	@ApiModelProperty(value = "银行类型 默认0:银行卡")
	private Integer bankType;

	@ApiModelProperty(value = "0:未禁用 1：禁用")
	private Integer disable;

	@ApiModelProperty(value = "/** 备注 */")
	private String remark;

}

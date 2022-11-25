package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.RegexEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("用户银行卡")
public class Bankcards extends BaseEntity{

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 银行卡id
     */
    @ApiModelProperty(value = "银行卡id")
    private String bankId;

    /**
     * 用户的银行/支付宝账号
     */
    @ApiModelProperty(value = "用户的银行账号")
    private String bankAccount;

    /**
     * 开户地址
     */
    @ApiModelProperty(value = "开户地址")
    private String address;

    /**
     * 开户名
     */
    @ApiModelProperty(value = "开户名")
    private String realName;
    /**
     * 默认卡，主卡
     */
    @ApiModelProperty(value = "默认卡，主卡= 1")
    private Integer defaultCard;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    /**
     *  针对绑定银行卡接口的参数合法性校验
     * @param bankId
     * @param bankAccount
     * @param address
     * @return
     */
    public static String checkParamFroBound(String bankId, String bankAccount,String address) {
//        if(!StringUtils.hasLength(accountName)){
//            return "持卡人不能为空";
//        }
        if (bankId == null) {
            return "银行id不能为空";
        }
        if (!StringUtils.hasLength(address)) {
            return "开户地址不能为空";
        }
        if (!StringUtils.hasLength(bankAccount)) {
            return "银行账号不能为空";
        }
        if (!bankAccount.matches(RegexEnum.BANK_ACCOUNT.getRegex())) {
            return "银行账号" + RegexEnum.BANK_ACCOUNT.getDesc();
        }
        return null;
    }
}

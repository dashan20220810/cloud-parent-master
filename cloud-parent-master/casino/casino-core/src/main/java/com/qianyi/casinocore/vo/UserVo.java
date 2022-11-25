package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.User;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserVo implements Serializable , Comparable<BigDecimal> {
    private static final long serialVersionUID = -6875617998456387632L;
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("名字")
    private String name;
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("语言")
    private Integer language;
    @ApiModelProperty("头像")
    private String headImg;
    //帐号状态（1：启用，其他：禁用）
    @ApiModelProperty("帐号状态")
    private Integer state;
    @ApiModelProperty("注册ip")
    private String registerIp;
    @ApiModelProperty("收款卡张数")
    private Integer creditCard;
    @ApiModelProperty("配置收款卡等级")//null 完全随机  或者 A|B|C|D
    private String cardLevel;
    @ApiModelProperty("收款卡张数")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal money = BigDecimal.ZERO;

    @ApiModelProperty("待领取洗码金额")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal notCodeWashingAmount = BigDecimal.ZERO;

    @ApiModelProperty("剩余打码量")
    private BigDecimal codeNum = BigDecimal.ZERO;
    @ApiModelProperty("冻结余额")
    private BigDecimal freezeMoney = BigDecimal.ZERO;
    @ApiModelProperty("可提现余额")
    private BigDecimal withdrawMoney = BigDecimal.ZERO;
//    @ApiModelProperty("WM余额")
//    private BigDecimal wmMoney;
    @ApiModelProperty("提款密码")
    private String withdrawPassword;
    @ApiModelProperty("email")
    private String email;
    @ApiModelProperty("qq")
    private String qq;
    @ApiModelProperty("webChat")
    private String webChat;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("直属父级账号")
    private String firstPidAccount;
    @ApiModelProperty("所属基层代理")
    private String thirdProxyAccount;
    @ApiModelProperty("所属基层id")
    private Long thirdProxyId;
    @ApiModelProperty("所属区域代理")
    private String secondProxyAccount;
    @ApiModelProperty("所属总代")
    private String firstProxyAccount;
    @ApiModelProperty("个人业绩流水")
    private BigDecimal performance;

    @ApiModelProperty("注册域名")
    private String registerDomainName;

    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("最后修改人")
    private String updateBy;
    public UserVo(){

    }
    public UserVo(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.account = user.getAccount();
        this.password = user.getPassword();
        this.phone = user.getPhone();
        this.language = user.getLanguage();
        this.headImg = user.getHeadImg();
        this.state = user.getState();
        this.registerIp = user.getRegisterIp();
        this.creditCard = user.getCreditCard();
        this.cardLevel = user.getCardLevel();
        this.withdrawPassword = user.getWithdrawPassword();
        this.email = user.getEmail();
        this.qq = user.getQq();
        this.webChat = user.getWebChat();
        this.realName = user.getRealName();
        this.createTime = user.getCreateTime();
        this.updateBy = user.getUpdateBy();
        this.updateTime = user.getUpdateTime();
        this.createBy = user.getCreateBy();
    }

    @Override
    public int compareTo(BigDecimal o) {
        return 0;
    }
}

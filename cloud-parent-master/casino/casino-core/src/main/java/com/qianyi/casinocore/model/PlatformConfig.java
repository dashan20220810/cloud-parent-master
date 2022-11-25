package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@ApiModel("平台配置表")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("最低金额清除打码量")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal clearCodeNum;

    @ApiModelProperty("打码倍率")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal betRate;

    @ApiModelProperty("每笔最低充值")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal chargeMinMoney;

    @ApiModelProperty("每笔最高充值")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal chargeMaxMoney;

    @ApiModelProperty("充值服务费")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal chargeServiceMoney;

    @ApiModelProperty("充值手续费百分比")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal chargeRate;

    @ApiModelProperty("每笔最低提现额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal withdrawMinMoney;

    @ApiModelProperty("每笔最高提现额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal withdrawMaxMoney;

    @ApiModelProperty("提现服务费")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal withdrawServiceMoney;

    @ApiModelProperty("提现手续费百分比")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal withdrawRate;

    @ApiModelProperty("ip最大注册量")
    private Integer ipMaxNum;

    @ApiModelProperty("WM余额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal wmMoney;

    @ApiModelProperty("WM余额警戒线")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal wmMoneyWarning;
    @ApiModelProperty("PG/CQ9余额警戒线")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal electronicsMoneyWarning;

    @ApiModelProperty("一级玩家返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal firstCommission;

    @ApiModelProperty("二级玩家返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal secondCommission;

    @ApiModelProperty("三级玩家返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal thirdCommission;

    @ApiModelProperty("玩家返佣配置修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commissionUpdate;

    @ApiModelProperty("首页域名配置")
    private String domainNameConfiguration;

    @ApiModelProperty("推广注册域名配置")
    private String proxyConfiguration;

    @ApiModelProperty("web项目域名配置")
    private String webConfiguration;

    @ApiModelProperty("注册开关 0 关闭 1 开启")
    private Integer registerSwitch = 1;

    @ApiModelProperty("短信余额警戒线")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal sendMessageWarning;

    @ApiModelProperty("人人代直属下级最大个数")
    private Integer directlyUnderTheLower;

    @ApiModelProperty("公司推广邀请码")
    private String companyInviteCode;

    @ApiModelProperty("保存文件、图片服务器地址")
    private String uploadUrl;

    @ApiModelProperty("读取文件、图片服务器地址")
    private String readUploadUrl;

    @ApiModelProperty("金钱符号")
    private String moneySymbol;

    @ApiModelProperty("网站icon")
    private String websiteIcon;

    @ApiModelProperty("(PC)log图片地址")
    private String logImageUrlPc;

    @ApiModelProperty("(APP)log图片地址")
    private String logImageUrlApp;

    @ApiModelProperty("(APP)登录注册页log图片地址")
    private String loginRegisterLogImageUrlApp;

    @ApiModelProperty("客服脚本的代号")
    private String customerCode;

    @ApiModelProperty("人人代开关 0:关闭，1:开启")
    private Integer peopleProxySwitch;

    @ApiModelProperty("银行卡绑定同名只能绑定一个账号校验开关 0:关闭，1:开启")
    private Integer bankcardRealNameSwitch;

    @ApiModelProperty("平台维护开关 0:关闭维护 1:开启维护")
    private Integer platformMaintenance;

    @ApiModelProperty("维护起始时间")
    private Date maintenanceStart;

    @ApiModelProperty("维护结束时间")
    private Date maintenanceEnd;

    @ApiModelProperty("平台总额度")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal totalPlatformQuota;

    @ApiModelProperty("平台总额度初始化标识")
    private Integer historicalDataId;

    @ApiModelProperty("验证码开关 0 关闭 1 开启")
    private Integer verificationCode ;



    //得到充值手续费用
    public BigDecimal getChargeServiceCharge(BigDecimal money){
        if (this.chargeRate == null){
            this.chargeRate = BigDecimal.ZERO;
        }
        if (this.chargeServiceMoney == null){
            this.chargeServiceMoney = BigDecimal.ZERO;
        }
        return money.multiply(this.chargeRate).add(this.chargeServiceMoney);
    }
    //得到提现手续费用
    public BigDecimal getWithdrawServiceCharge(BigDecimal money){
        if (this.withdrawRate == null){
            this.withdrawRate = BigDecimal.ZERO;
        }
        if (this.withdrawServiceMoney == null){
            this.withdrawServiceMoney = BigDecimal.ZERO;
        }
        return money.multiply(this.withdrawRate).add(this.withdrawServiceMoney);
    }

    /**
     * 检查人人代开关
     * @param config
     * @return
     */
    public static boolean checkPeopleProxySwitch(PlatformConfig config) {
        if (config == null) {
            return false;
        }
        boolean proxySwitch = config.getPeopleProxySwitch() == Constants.open ? true : false;
        return proxySwitch;
    }

    /**
     * 银行卡绑定同名只能绑定一个账号校验开关 默认开
     * @param config
     * @return
     */
    public static boolean checkBankcardRealNameSwitch(PlatformConfig config) {
        if (config == null) {
            return true;
        }
        boolean bankcardRealNameSwitch = config.getBankcardRealNameSwitch() == Constants.close ? false : true;
        return bankcardRealNameSwitch;
    }
}

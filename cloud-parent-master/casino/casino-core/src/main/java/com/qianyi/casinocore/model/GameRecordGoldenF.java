package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.Constants;
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
@ApiModel("资金详情")
@Table(name = "game_record_goldenf",indexes = {
        @Index(name = "goldenf_traceId",columnList = "traceId",unique = true)
})
public class GameRecordGoldenF extends BaseEntity {

    @ApiModelProperty(value = "用户姓名")
    private String playerName;

    @ApiModelProperty(value = "父主单号")
    private String parentBetId;

    @ApiModelProperty(value = "下注编号")
    private String betId;

    @ApiModelProperty(value = "交易类型")
    private String transType;

    @ApiModelProperty(value = "游戏代码")
    private String gameCode;

    @ApiModelProperty(value = "币别")
    private String currency;

    @ApiModelProperty(value = "下注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal betAmount;

    @ApiModelProperty(value = "派彩或退回金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.000000'")
    private BigDecimal winAmount;

    @ApiModelProperty(value = "产品代码")
    private String vendorCode;

    @ApiModelProperty(value = "钱包代码")
    private String walletCode;

    @ApiModelProperty(value = "创建时间")
    private Long createdAt;

    @ApiModelProperty(value = "创建时间字符串")
    private String createAtStr;

    @ApiModelProperty(value = "交易编号")
    private String traceId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "一级代理")
    private Long firstProxy;

    @ApiModelProperty(value = "二级代理")
    private Long secondProxy;

    @ApiModelProperty(value = "三级代理")
    private Long thirdProxy;

    @ApiModelProperty(value = "洗码状态：1：成功")
    private Integer washCodeStatus;

    @ApiModelProperty(value = "返利状态：1：成功")
    private Integer rebateStatus;

    @ApiModelProperty(value = "抽点状态: 0: 否 1: 是")
    private Integer extractStatus;

    @ApiModelProperty(value = "打码状态：1：成功")
    private Integer codeNumStatus;

    @ApiModelProperty(value = "分润状态：0:失败，>0：成功")
    private Integer shareProfitStatus = Constants.no;

    @ApiModelProperty(value = "游戏报表状态：0:失败，1：成功")
    private Integer gameRecordStatus = Constants.no;

    public GameRecordGoldenF(BigDecimal betAmount,BigDecimal validbet){
        this.betAmount = betAmount==null?BigDecimal.ZERO:betAmount;
        this.winAmount = validbet==null?BigDecimal.ZERO:validbet;
    }

    public GameRecordGoldenF(){

    }
}

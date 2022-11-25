package com.qianyi.casinocore.model;

import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="game_record_obty",uniqueConstraints={@UniqueConstraint(columnNames={"orderNo"})})
@ApiModel("OB体育游戏记录")
public class GameRecordObty extends BaseEntity{

    @ApiModelProperty(value = "我方账号")
    private Long userId;

    @ApiModelProperty(value = "用户名称")
    private String userName;

    @ApiModelProperty(value = "商户编码")
    private String merchantCode;

    @ApiModelProperty(value = "订单Id")
    private String orderNo;

    @ApiModelProperty(value = "订单状态:0.待处理,1.已结算,2.取消(人工),3.待确认,4.风控拒单,5.撤单(赛事取消)")
    private String orderStatus;

    @ApiModelProperty(value = "投注时间(13位时间戳)")
    private Long betTime;

    @ApiModelProperty(value = "投注时间(yyyy-MM-dd)")
    private String betStrTime;

    @ApiModelProperty(value = "订单实际投注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "该订单下的注单数(单关为1,串关为n)")
    private String betCount;

    @ApiModelProperty(value = "结算时间")
    private Long settleTime;

    @ApiModelProperty(value = "结算时间(yyyy-MM-dd)")
    private String settleStrTime;

    @ApiModelProperty(value = "结算金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal settleAmount;

    @ApiModelProperty(value = "提前结算投注金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal preBetAmount;

    @ApiModelProperty(value = "盈利金额")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal profitAmount;

    @ApiModelProperty(value = "订单结算结果0-无结果 2-走水 3-输 4-赢 5-赢一半 6-输一半")
    private Integer outcome;

    @ApiModelProperty(value = "串关类型")
    private Integer seriesType;

    @ApiModelProperty(value = "串关值")
    private String seriesValue;

    @ApiModelProperty(value = "汇率")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    private BigDecimal exchangeRate;

    @ApiModelProperty(value = "设备类型 1:H5，2：PC,3:Android,4:IOS")
    private String deviceType;

    @ApiModelProperty(value = "结算次数")
    private Integer settleTimes;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
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

    public GameRecordObty(BigDecimal profitAmount,BigDecimal orderAmount,BigDecimal settleAmount){
        this.profitAmount = profitAmount==null?BigDecimal.ZERO:profitAmount;
        this.orderAmount = orderAmount==null?BigDecimal.ZERO:orderAmount;
        this.settleAmount = settleAmount==null?BigDecimal.ZERO:settleAmount;
    }

    public GameRecordObty(){

    }
}

package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.Order;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderVo  implements Serializable {
    private static final long serialVersionUID = -6875617929250305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty(value = "会员账号")
    private String account;
    @ApiModelProperty("订单号")
    private String no;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty("金额")
    private BigDecimal money;
    //1.未确认。 2.成功   3.失败
    @ApiModelProperty("1.未确认。 2.成功   3.失败")
    private Integer state;
    @ApiModelProperty("备注")
    private String remark;
    /**
     * 0.转入，1.转出
     */
    @ApiModelProperty("0.转入，1.转出")
    private Integer type;
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
    public OrderVo(){

    }
    public OrderVo(Order order){
        this.id = order.getId();
        this.userId = order.getUserId();
        this.no = order.getNo();
        this.money = order.getMoney();
        this.state = order.getState();
        this.remark = (order.getType() == CommonConst.NUMBER_0? "转入" : "转出") + order.getGamePlatformName();
        this.type = order.getType();
        this.createTime = order.getCreateTime();
        this.createBy = order.getCreateBy();
        this.updateBy = order.getUpdateBy();
        this.updateTime = order.getUpdateTime();
    }
}

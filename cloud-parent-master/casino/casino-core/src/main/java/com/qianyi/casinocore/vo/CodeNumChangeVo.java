package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.model.CodeNumChange;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CodeNumChangeVo implements Serializable {
    private static final long serialVersionUID = -6875617998456305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "会员账号")
    private String account;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "游戏记录ID")
    private Long gameRecordId;
    @ApiModelProperty(value = "注单号")
    private String betId;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "打码量")
    private BigDecimal amount;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "打码量变化前")
    private BigDecimal amountBefore;
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    @ApiModelProperty(value = "打码量变化后")
    private BigDecimal amountAfter;
    @ApiModelProperty(value = "平台:wm,PG,CQ9")
    private String platform;
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
    @ApiModelProperty(value = "0:消码，1:清0点，2上分")
    private Integer type;
    public CodeNumChangeVo(CodeNumChange codeNumChange){
        this.id = codeNumChange.getId();
        this.userId = codeNumChange.getUserId();
        this.gameRecordId = codeNumChange.getGameRecordId();
        this.betId = codeNumChange.getBetId();
        this.amount = codeNumChange.getAmount();
        this.amountBefore = codeNumChange.getAmountBefore();
        this.amountAfter = codeNumChange.getAmountAfter();
        this.createTime = codeNumChange.getCreateTime();
        this.createBy = codeNumChange.getCreateBy();
        this.updateBy = codeNumChange.getUpdateBy();
        this.updateTime = codeNumChange.getUpdateTime();
        this.platform = codeNumChange.getPlatform();
        int codeType = null == codeNumChange.getType()? 2 : codeNumChange.getType();
        this.type = codeType;

    }
}

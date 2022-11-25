package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@ApiModel("平台总额度记录表")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalPlatformQuotaRecord extends BaseEntity {

    @ApiModelProperty("平台总额度")
    @Column(columnDefinition = "Decimal(19,6) default '0.00'")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal totalPlatformQuota;


    @ApiModelProperty("时间")
    @Column(unique = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

}

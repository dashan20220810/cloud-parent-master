package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@ApiModel("ip黑名单")
@Table(name ="ip_black",uniqueConstraints={@UniqueConstraint(columnNames={"ip"})})
public class IpBlack extends BaseEntity{
    @ApiModelProperty(value = "ip")
    private String ip;

    /** 0:未禁用 1：禁用 */
    @ApiModelProperty(value = "0:未禁用 1：禁用")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    public IpBlack() {
    }

    public IpBlack(String ip, Integer status, String remark) {
        this.ip = ip;
        this.status = status;
        this.remark = remark;
    }
}

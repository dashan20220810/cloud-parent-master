package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * 此处不要继承BaseEntity
 * 配置表等，后续可以用elk来做操作人记录追踪
 */
@Data
@Entity
@ApiModel("客户风险配置表")
public class SysConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 组别，根据组别判断是什么类型的值 1、财务 2、ip
     */
    @ApiModelProperty("组别")
    private Integer sysGroup;

    /**
     * 配置名
     */
    @ApiModelProperty("配置名")
    private String name;

    /**
     * 配置值
     * 不可以用json数据
     */
    @ApiModelProperty("配置值，不可以用json数据")
    private String value;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;
}

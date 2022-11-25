package com.qianyi.casinoadmin.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;

@Data
public class ProxyReportVo implements Serializable, Comparator<ProxyReportVo> {

    private static final long serialVersionUID = -6875619842312305179L;
    @ApiModelProperty("id")
    private Long userId;
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("层级 0 当前 1 一级 2 二级 3 三级")
    private Integer tier;
    @ApiModelProperty("直属父级账号")
    private String firstPidAccount;
    @ApiModelProperty("直属父级ID")
    private Long firstPid;
    @ApiModelProperty("第二级ID")
    private Long secondPid;
    @ApiModelProperty("第三级ID")
    private Long thirdPid;
    @ApiModelProperty("团队人数")
    private Integer allGroupNum = CommonConst.NUMBER_0;

    @ApiModelProperty("个人业绩流水")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal performance = BigDecimal.ZERO;

    @ApiModelProperty("团队业绩流水")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal allPerformance = BigDecimal.ZERO;

    @ApiModelProperty("贡献佣金")
    @JsonSerialize(using = Decimal2Serializer.class, nullsUsing = Decimal2Serializer.class)
    private BigDecimal contribution;

    @ApiModelProperty("统计时段")
    private String staticsTimes;
    @ApiModelProperty("返佣比例")
    private String commission;

    private Integer sort;

    private Integer level;

    @Override
    public int compare(ProxyReportVo s1, ProxyReportVo s2) {
        if(s1.getSort()>s2.getSort()){	//greater
            return 1;
        }else if(s1.getSort()==s2.getSort()){	//equals
            if (s1.getUserId() > s2.getUserId()){
                return 1;
            }else if (s1.getUserId() < s2.getUserId()){
                return -1;
            }
            return 0;
        }else{	//less
            return -1;
        }
    }
}

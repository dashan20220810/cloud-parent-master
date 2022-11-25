package com.qianyi.casinocore.co.charge;

import lombok.Data;

import java.util.Date;

@Data
public class ChargeOrderCo {

    // 查询开始时间
    private Date startDate;

    // 查询结束时间
    private Date endDate;

    private Long firstProxy;

    private Long secondProxy;

    private Long thirdProxy;

}

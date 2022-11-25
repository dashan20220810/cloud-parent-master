package com.qianyi.casinocore.co.withdrwa;

import lombok.Data;

import java.util.Date;

@Data
public class WithdrawOrderCo {
    // 查询开始时间
    private Date startDate;

    // 查询结束时间
    private Date endDate;

    private Long firstProxy;

    private Long secondProxy;

    private Long thirdProxy;
}

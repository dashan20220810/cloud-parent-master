package com.qianyi.casinoadmin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HistoryTotal {

    private BigDecimal all_profit_amount;
    private BigDecimal service_charge;
    private BigDecimal total_amount;
    private Integer num;
    private BigDecimal bet_amount;
    private BigDecimal avg_benefit;
    private BigDecimal win_loss;
    private BigDecimal validbet;
    private BigDecimal wash_amount;
}

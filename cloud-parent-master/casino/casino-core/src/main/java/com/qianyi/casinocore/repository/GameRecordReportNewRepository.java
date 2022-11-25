package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordReportNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface GameRecordReportNewRepository  extends JpaRepository<GameRecordReportNew,Long>, JpaSpecificationExecutor<GameRecordReportNew> {

    @Modifying
    @Query(value = "INSERT INTO game_record_report_new (game_record_report_id,statics_times,bet_amount,valid_amount,"
        + "win_loss_amount,amount,betting_number,first_proxy,second_proxy,third_proxy,platform,surplus_amount,user_amount) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13) ON DUPLICATE KEY UPDATE bet_amount=bet_amount + ?3,"
        + "valid_amount=valid_amount + ?4,win_loss_amount=win_loss_amount + ?5,amount=amount + ?6,betting_number=betting_number + ?7 ,surplus_amount=surplus_amount + ?12,user_amount=user_amount + ?13 ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,String staticsTimes,BigDecimal betAmount,BigDecimal validAmount,BigDecimal winLossAmount,
        BigDecimal amount,Integer bettingNumber,Long firstProxy,Long secondProxy,Long thirdProxy,String platform,BigDecimal surplusAmount,BigDecimal userAmount);

}

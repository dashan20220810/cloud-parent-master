package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserGameRecordReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserGameRecordReportRepository extends JpaRepository<UserGameRecordReport,Long>,
    JpaSpecificationExecutor<UserGameRecordReport> {

    @Modifying
    @Query(value = "INSERT INTO user_game_record_report (user_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "betting_number,bet_amount,platform) " +
        "VALUES (?1,?2,?3,?4,?5,1,?6,?7) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +1,bet_amount=bet_amount + ?6 ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount, BigDecimal winLoss,
        BigDecimal betAmount, String platform);

    @Query(value = "select u.user_id userId,SUM(u.valid_amount) validbet from user_game_record_report u where u.order_times >= ?1 and u.order_times <= ?2 GROUP BY u.user_id;",nativeQuery = true)
    List<Map<String, Object>> sumUserRunningWater(String startTime, String endTime);

    @Query(value = "select ifnull(SUM(u.valid_amount),0) validbet from user_game_record_report u where u.user_id = ?3 and u.order_times >= ?1 and u.order_times <= ?2 ;",nativeQuery = true)
    BigDecimal sumUserRunningWaterByUserId(String startTime, String endTime, Long userId);
}

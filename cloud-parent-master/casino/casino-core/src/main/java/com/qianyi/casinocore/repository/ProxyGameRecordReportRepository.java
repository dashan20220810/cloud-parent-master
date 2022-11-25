package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.ProxyGameRecordReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProxyGameRecordReportRepository extends JpaRepository<ProxyGameRecordReport,Long>,
    JpaSpecificationExecutor<ProxyGameRecordReport> {

    @Modifying
    @Query(value = "INSERT INTO proxy_game_record_report (proxy_game_record_report_id,user_id,order_times,valid_amount,win_loss,"
        + "first_proxy,second_proxy,third_proxy,betting_number,bet_amount) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,1,?9) ON DUPLICATE KEY UPDATE valid_amount=valid_amount + ?4,"
        + "win_loss=win_loss + ?5,betting_number = betting_number +1,bet_amount=bet_amount + ?9 ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId, Long userId, String orderTimes, BigDecimal validAmount, BigDecimal winLoss,
        Long firstProxy, Long secondProxy, Long thirdProxy, BigDecimal betAmount);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.first_proxy= ?3 and p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByFirst(String startTime, String endTime, Long firstProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.second_proxy= ?3 and p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossBySecond(String startTime, String endTime, Long secondProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.third_proxy= ?3 and p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByThird(String startTime, String endTime, Long thirdProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.first_proxy= ?1 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByFirst(Long firstProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.second_proxy= ?1  GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossBySecond(Long secondProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.third_proxy= ?1  GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLossByThird(Long thirdProxy);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p where p.order_times "
        + "BETWEEN ?1 and ?2 GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLoss(String startTime, String endTime);

    @Query(value = "select COUNT(1) num,ifnull(SUM(validAmount),0) validAmount,ifnull(SUM(winLoss),0) winLoss from (select SUM(p.valid_amount) "
        + "validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p GROUP BY p.user_id) a",nativeQuery = true)
    Map<String, Object> findSumBetAndWinLoss();

    @Query(value = "select p.order_times as orderTimes ,SUM(p.valid_amount) validAmount,SUM(p.win_loss) winLoss from proxy_game_record_report p "
        + "where p.order_times BETWEEN ?1 and ?2 GROUP BY p.order_times",nativeQuery = true)
    List<Map<String, Object>> findBetAndWinLoss(String startTime,String endTime);
}

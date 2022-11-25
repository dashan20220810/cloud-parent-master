package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CompanyProxyMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CompanyProxyMonthRepository extends JpaRepository<CompanyProxyMonth,Long>, JpaSpecificationExecutor<CompanyProxyMonth> {

    void deleteByStaticsTimes(String staticsTimes);

    /**
     * 按staticsTimes与代理角色查询所有记录
     * @param staticsTimes
     * @return
     */
    List<CompanyProxyMonth> findAllByStaticsTimesAndProxyRole(String staticsTimes, Integer proxyRole);

    @Query(value = "select user_id,max(proxy_role) proxy_role ,\n" +
        "max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,max(third_proxy) third_proxy ,\n" +
        "max(substr( ?1 ,1,7)) statics_times, \n" +
        "sum(player_num) player_num ,sum(group_bet_amount) group_bet_amount ,max(profit_level) profit_level ,\n" +
        "max(profit_rate) profit_rate ,sum(group_totalprofit) group_totalprofit ,max(benefit_rate)benefit_rate ,\n" +
        "sum(profit_amount) profit_amount ,max(settle_status) settle_status \n" +
        "from company_proxy_detail cpd \n" +
        "where bet_time between ?1 and ?2\n" +
        "group by user_id",nativeQuery = true)
    List<Map<String,Object>> queryMonthData(String startTime, String endTime);

    @Query(value = "select \n" +
        "u.account , \n" +
        "u.third_proxy , \n" +
        " u.id,\n" +
        " ifnull(main_t.num,0) num,\n" +
        " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
        " ifnull(main_t.validbet,0) validbet ,\n" +
        " ifnull(main_t.win_loss,0) win_loss ,\n" +
        " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
        " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
        " ifnull(pr.amount,0) all_profit_amount, \n" +
        " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)) avg_benefit, \n" +
        " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0) total_amount\n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?1 and bet_time <= ?2\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?1 and create_time <= ?2\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
        "  from withdraw_order wo  \n" +
        "  where update_time >= ?1 and update_time <= ?2\n" +
        "  group by user_id \n" +
        " ) withdraw_t on u.id = withdraw_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
        "  where create_time >= ?1 and create_time <= ?2\n" +
        "  group by user_id  \n" +
        " ) pr on u.id=pr.user_id \n" +
        " where 1=1\n" +
        "limit ?3,?4",nativeQuery = true)
    List<Map<String,Object>> queryAllPersonReport(String startTime,String endTime,int page,int count);

    @Query(value = "select \n" +
        "u.account , \n" +
        "u.third_proxy , \n" +
        " u.id,\n" +
        " ifnull(main_t.num,0) num,\n" +
        " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
        " ifnull(main_t.validbet,0) validbet ,\n" +
        " ifnull(main_t.win_loss,0) win_loss ,\n" +
        " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
        " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
        " ifnull(pr.amount,0) all_profit_amount, \n" +
        " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)) avg_benefit, \n" +
        " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0) total_amount\n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?2 and bet_time <= ?3\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?2 and create_time <= ?3\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
        "  from withdraw_order wo  \n" +
        "  where update_time >= ?2 and update_time <= ?3\n" +
        "  group by user_id \n" +
        " ) withdraw_t on u.id = withdraw_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
        "  where create_time >= ?2 and create_time <= ?3\n" +
        "  group by user_id  \n" +
        " ) pr on u.id=pr.user_id \n" +
        " where 1=1 and u.id =?1"
        ,nativeQuery = true)
    List<Map<String,Object>> queryPersonReport(long user_id ,String startTime,String endTime);

    @Query(value = "select \n" +
        " sum(ifnull(main_t.num,0)) num,\n" +
        " sum(ifnull(main_t.bet_amount,0)) bet_amount ,\n" +
        " sum(ifnull(main_t.validbet,0)) validbet ,\n" +
        " sum(ifnull(main_t.win_loss,0)) win_loss ,\n" +
        " sum(ifnull(wash_t.wash_amount,0)) wash_amount, \n" +
        " sum(ifnull(withdraw_t.service_charge,0)) service_charge, \n" +
        " sum(ifnull(pr.amount,0)) all_profit_amount, \n" +
        " sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))) avg_benefit, \n" +
        " sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0)) total_amount\n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?1 and bet_time <= ?2\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?1 and create_time <= ?2\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
        "  from withdraw_order wo  \n" +
        "  where update_time >= ?1 and update_time <= ?2\n" +
        "  group by user_id \n" +
        " ) withdraw_t on u.id = withdraw_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
        "  where create_time >= ?1 and create_time <= ?2\n" +
        "  group by user_id  \n" +
        " ) pr on u.id=pr.user_id \n" +
        " where 1=1",nativeQuery = true)
    Map<String,Object> queryAllTotal(String startTime,String endTime);

    @Query(value = "select count(1) from user",nativeQuery = true)
    int queryAllTotalElement(String startTime,String endTime);

    @Query(value = "select count(1) from user where first_proxy = ?1",nativeQuery = true)
    int queryAllByFirst(Long first);

    @Query(value = "select count(1) from user where second_proxy = ?1",nativeQuery = true)
    int queryAllBySecond(Long second);

    @Query(value = "select count(1) from user where third_proxy = ?1",nativeQuery = true)
    int queryAllByThird(Long third);

    @Query(value = "select sum(num) num,sum(bet_amount) bet_amount ,sum(validbet) validbet,sum(win_loss) win_loss,sum(wash_amount) wash_amount from (\n" +
        "select \n" +
        " ifnull(main_t.num,0) num,\n" +
        " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
        " ifnull(main_t.validbet,0) validbet ,\n" +
        " ifnull(main_t.win_loss,0) win_loss ,\n" +
        " ifnull(wash_t.wash_amount,0) wash_amount \n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?2 and bet_time <= ?3\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?2 and create_time <= ?3\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " where u.third_proxy =?1 ) a"
        ,nativeQuery = true)
    Map<String,Object> queryReportByThird(long proxyUserId ,String startTime,String endTime);

    @Query(value = "select sum(num) num,sum(bet_amount) bet_amount ,sum(validbet) validbet,sum(win_loss) win_loss,sum(wash_amount) wash_amount from (\n" +
        "select \n" +
        " ifnull(main_t.num,0) num,\n" +
        " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
        " ifnull(main_t.validbet,0) validbet ,\n" +
        " ifnull(main_t.win_loss,0) win_loss ,\n" +
        " ifnull(wash_t.wash_amount,0) wash_amount \n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?1 and bet_time <= ?2\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?1 and create_time <= ?2\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " where u.third_proxy is null) a"
        ,nativeQuery = true)
    Map<String,Object> queryReportByCompany(String startTime,String endTime);

    @Query(value = "select sum(num) num,sum(bet_amount) bet_amount ,sum(validbet) validbet,sum(win_loss) win_loss,sum(wash_amount) wash_amount from (\n" +
        "select \n" +
        " ifnull(main_t.num,0) num,\n" +
        " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
        " ifnull(main_t.validbet,0) validbet ,\n" +
        " ifnull(main_t.win_loss,0) win_loss ,\n" +
        " ifnull(wash_t.wash_amount,0) wash_amount \n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?2 and bet_time <= ?3\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?2 and create_time <= ?3\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " where u.second_proxy =?1 ) a"
        ,nativeQuery = true)
    Map<String,Object> queryReportBySecond(long proxyUserId ,String startTime,String endTime);

    @Query(value = "select sum(num) num,sum(bet_amount) bet_amount ,sum(validbet) validbet,sum(win_loss) win_loss,sum(wash_amount) wash_amount from (\n" +
        "select \n" +
        " ifnull(main_t.num,0) num,\n" +
        " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
        " ifnull(main_t.validbet,0) validbet ,\n" +
        " ifnull(main_t.win_loss,0) win_loss ,\n" +
        " ifnull(wash_t.wash_amount,0) wash_amount \n" +
        "from user u\n" +
        "left join ( \n" +
        "  select user_id , \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?2 and bet_time <= ?3\n" +
        "  group by user_id \n" +
        " ) main_t on u.id = main_t.user_id\n" +
        " left join ( \n" +
        "  select user_id , sum(amount) wash_amount  \n" +
        "  from wash_code_change wcc  \n" +
        "  where create_time >= ?2 and create_time <= ?3\n" +
        "  group by user_id \n" +
        " ) wash_t on u.id = wash_t.user_id\n" +
        " where u.first_proxy =?1 ) a"
        ,nativeQuery = true)
    Map<String,Object> queryReportByFirst(long proxyUserId ,String startTime,String endTime);


    @Query(value = "  select \n" +
        "  count(1) num, \n" +
        "  sum(bet) bet_amount, \n" +
        "  sum(validbet) validbet , \n" +
        "  sum(win_loss) win_loss  \n" +
        "  from game_record gr \n" +
        "  where bet_time >= ?1 and bet_time <= ?2\n"
        ,nativeQuery = true)
    Map<String,Object> queryReportAll(String startTime,String endTime);
}

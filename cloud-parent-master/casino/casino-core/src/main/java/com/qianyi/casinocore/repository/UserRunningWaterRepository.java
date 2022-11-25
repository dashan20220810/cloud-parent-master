package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.UserRunningWater;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface UserRunningWaterRepository extends JpaRepository<UserRunningWater, Long>, JpaSpecificationExecutor<UserRunningWater> {
    @Modifying
    @Query(value = "INSERT INTO user_running_water (user_id,statics_times,amount,commission,create_time,first_proxy,second_proxy,third_proxy) " +
            "VALUES (?1,?2,?3,?4,sysdate(),?5,?6,?7) ON DUPLICATE KEY UPDATE amount=amount + ?3,commission=commission + ?4 ;",nativeQuery = true)
    void updateKey(Long userId,String staticsTimes ,BigDecimal amount,BigDecimal commission,Long firstProxy,Long secondProxy,Long thirdProxy);

    List<UserRunningWater> findGroupByUserId(Specification<UserRunningWater> condition);

    List<UserRunningWater> findByStaticsTimes(String staticsTimes);

    @Modifying
    @Query("update UserRunningWater u set u.firstProxy= ?2 where u.userId=?1")
    void updateFirstProxy(Long userId, Long firstProxy);

    @Modifying
    @Query("update UserRunningWater u set u.secondProxy= ?2 where u.userId=?1")
    void updateSecondProxy(Long userId, Long secondProxy);

    @Modifying
    @Query("update UserRunningWater u set u.thirdProxy= ?2 where u.userId=?1")
    void updatetThirdProxy(Long userId, Long secondProxy);
}

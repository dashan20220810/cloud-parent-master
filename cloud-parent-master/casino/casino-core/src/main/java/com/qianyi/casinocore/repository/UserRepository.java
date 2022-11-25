package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.User;
import org.springframework.data.jpa.repository.*;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByAccount(String account);

    Integer countByRegisterIp(String ip);

    User getByName(String userName);

//    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select * from User u where u.id = ? for update",nativeQuery = true)
    User findUserByUserIdUseLock(Long userId);

    @Query(value = "select * from User u where u.id = ? ",nativeQuery = true)
    User findUserByUserIdUse(Long userId);

    @Query(value = "SELECT * FROM user u WHERE UPPER(u.account) = UPPER(?)",nativeQuery = true)
    List<User> findByAccountUpper(String account);

    @Modifying
    @Query("update User u set u.isFirstBet= ?2 where u.id=?1")
    void updateIsFirstBet(Long id, Integer washCodeStatus);

    @Modifying
    @Query("update User u set u.password= ?2 where u.id=?1")
    void updatePassword(Long id, String password);

    List<User> findByRegisterIp(String ip);

    User findByInviteCode(String inviteCode);

    List<User> findByStateAndFirstPid(Integer state, Long firstPid);

    List<User> findBySecondPid(Long firstPid);

    List<User> findByThirdPid(Long firstPid);

    List<User> findByFirstPid(Long id);

    Integer countByFirstPid(Long userId);

    List<User> findByPhone(String phone);

    User findByFirstPidAndAccount(Long userId, String account);

    @Query(value = "select u.id  from user u  where u.register_domain_name = ?1 and u.create_time >= ?2 and u.create_time <= ?3",nativeQuery = true)
    Set<Long> findUserByRegisterDomainName(String registerDomainName,String startTime,String endTime);


}

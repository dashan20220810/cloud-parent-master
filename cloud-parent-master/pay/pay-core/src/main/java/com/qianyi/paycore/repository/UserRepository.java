package com.qianyi.paycore.repository;

import com.qianyi.paycore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByAccount(String account);

    @Query("update User u set u.secret=?2 where u.id=?1")
    @Modifying
    void setSecretById(Long id, String secret);
}

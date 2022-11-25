package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.AccountChange;
import com.qianyi.casinocore.model.AdGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AccountChangeRepository extends JpaRepository<AccountChange,Long>, JpaSpecificationExecutor<AccountChange> {

     List<AccountChange> findByType(Integer type);
}

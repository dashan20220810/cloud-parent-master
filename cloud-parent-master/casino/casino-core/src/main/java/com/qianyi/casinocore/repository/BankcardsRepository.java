package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Bankcards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface BankcardsRepository extends JpaRepository<Bankcards,Long> , JpaSpecificationExecutor<Bankcards> {

    List<Bankcards> findBankcardsByUserIdOrderByDefaultCardDesc(Long userId);

    Bankcards findFirstByUserId(Long userId);

    int countByUserId(Long userId);

    List<Bankcards> findByUserId(Long userId);

    Bankcards findByUserIdAndBankAccount(Long userId, String bankAccount);

    List<Bankcards> findByRealName(String realName);

    List<Bankcards> findByBankAccount(String bankAccount);

    List<Bankcards> findByBankId(String bankId);
}

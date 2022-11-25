package com.qianyi.casinocore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qianyi.casinocore.model.BankInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BankInfoRepository extends JpaRepository<BankInfo,Long> , JpaSpecificationExecutor<BankInfo> {
    BankInfo findByBankName(String bankName);
}

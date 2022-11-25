package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.BankcardsDel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BankcardsDelRepository extends JpaRepository<BankcardsDel,Long>, JpaSpecificationExecutor<BankcardsDel> {
    List<BankcardsDel> findByUserId(Long userId);

}

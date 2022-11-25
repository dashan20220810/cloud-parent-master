package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.DepositSendActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DepositSendActivityRepository extends JpaRepository<DepositSendActivity,Long>, JpaSpecificationExecutor<DepositSendActivity> {

    List<DepositSendActivity> findAllByActivityNameAndDel(String actName,boolean del);

    List<DepositSendActivity> findAllByDelFalse();

}

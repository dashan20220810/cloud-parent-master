package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.IpBlack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IpBlackRepository extends JpaRepository<IpBlack,Long> , JpaSpecificationExecutor<IpBlack> {

    IpBlack findByIp(String ip);
}

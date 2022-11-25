package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompanyProxyDetailRepository extends JpaRepository<CompanyProxyDetail,Long>, JpaSpecificationExecutor<CompanyProxyDetail> {

    CompanyProxyDetail getCompanyProxyDetailByUserIdAndStaticsTimes(Long uid,String staticsTime);

    void deleteByStaticsTimes(String staticsTime);
}

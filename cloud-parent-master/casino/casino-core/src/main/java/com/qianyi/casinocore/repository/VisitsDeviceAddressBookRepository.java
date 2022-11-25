package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.VisitsDeviceAddressBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VisitsDeviceAddressBookRepository extends JpaRepository<VisitsDeviceAddressBook, Long>, JpaSpecificationExecutor<VisitsDeviceAddressBook> {

    void deleteByVisitsDeviceId(Long visitsDeviceId);
}

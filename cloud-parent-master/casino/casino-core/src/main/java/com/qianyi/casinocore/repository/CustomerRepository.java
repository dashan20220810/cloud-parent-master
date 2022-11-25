package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
}

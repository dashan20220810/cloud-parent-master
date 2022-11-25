package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Cacheable(cacheNames = "customer")
    public Customer findFirst() {
        List<Customer> all = customerRepository.findAll();
        if (all == null || all.size() == 0) {
            return null;
        }
        return all.get(0);
    }

    @CacheEvict(cacheNames = "customer", allEntries = true)
    public Customer save(Customer customer){
        return customerRepository.save(customer);
    }
}

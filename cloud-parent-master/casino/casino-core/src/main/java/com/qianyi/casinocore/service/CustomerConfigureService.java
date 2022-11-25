package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CustomerConfigure;
import com.qianyi.casinocore.model.ShareProfitChange;
import com.qianyi.casinocore.repository.CustomerConfigureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CustomerConfigureService {
    @Autowired
    CustomerConfigureRepository customerConfigureRepository;

    public List<CustomerConfigure> findAll() {
        return customerConfigureRepository.findAll();
    }

    @CacheEvict(cacheNames = "customerConfigure", allEntries = true)
    public CustomerConfigure save(CustomerConfigure customerConfigure){
        return customerConfigureRepository.save(customerConfigure);
    }

    public List<CustomerConfigure> saveAll(List<CustomerConfigure> customerConfigure){
        return customerConfigureRepository.saveAll(customerConfigure);
    }


    public CustomerConfigure getById(Long id){
        return customerConfigureRepository.getById(id);
    }

    @Cacheable(cacheNames = "customerConfigure")
    public List<CustomerConfigure> findByState(Integer state){
        return customerConfigureRepository.findByState(state);
    }

    public int countCustomerConfigure(Integer state){
        return customerConfigureRepository.countCustomerConfigure(state);
    }

    public void deleteCustomerConfigureAll(){
        customerConfigureRepository.deleteAllInBatch();
    }

}

//package com.qianyi.casinocore.repository;
//
//import com.qianyi.casinocore.model.Bankcards;
//import com.qianyi.casinocore.service.BankcardsService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.Date;
//
//@SpringBootTest
//@RunWith(SpringJUnit4ClassRunner.class)
//public class BankcardsRepositoryTest {
//
//    @Autowired
//    BankcardsRepository bankcardsRepository;
//
//    @Autowired
//    BankcardsService bankcardsService;
//
//    @Test
//    public void should_save_banck(){
//
//        Bankcards bankcards = new Bankcards();
//        bankcards.setUserId(1l);
//        bankcards.setBankId("1");
//        bankcards.setBankAccount("bankAccount");
//        bankcards.setAddress("address");
//        bankcards.setRealName("jjjj");
//        bankcards.setDisable(0);
//        bankcards.setDefaultCard(1);
//        bankcardsRepository.save(bankcards);
//
//    }
//
//    @Test
//    public void should_save_banck_by_service(){
//
//        Bankcards bankcards = new Bankcards();
//        bankcards.setUserId(1l);
//        bankcards.setBankId("1");
//        bankcards.setBankAccount("bankAccount");
//        bankcards.setAddress("address");
//        bankcards.setRealName("jjjj");
//        bankcards.setDisable(0);
//        bankcards.setDefaultCard(1);
//        bankcardsService.boundCard(bankcards);
//
//    }
//
//}
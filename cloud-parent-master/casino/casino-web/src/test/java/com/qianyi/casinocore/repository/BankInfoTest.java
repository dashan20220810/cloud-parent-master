package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.BankInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class BankInfoTest {

    @Autowired
    BankInfoRepository bankInfoRepository;

//    @Test
//    public void should_add_bank_info(){
//        BankInfo bankInfo = new BankInfo();
//        bankInfo.setBankCode("diiw");
//        bankInfo.setBankName("xxxxx银行");
//        bankInfo.setDisable(0);
//
//        bankInfoRepository.save(bankInfo);
//    }
}

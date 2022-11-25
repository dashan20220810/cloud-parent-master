package com.qianyi.casinoreport.business.usergroup;

import com.qianyi.casinocore.model.ConsumerError;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserRepository;
import com.qianyi.casinocore.service.ConsumerErrorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserGroupNumBusinessTest {

    @Autowired
    UserGroupNumBusiness userGroupNumBusiness;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ConsumerErrorService consumerErrorService;

    @Test
    public void should_process_correct(){
        Optional<User> user =userRepository.findById(1534l);

        userGroupNumBusiness.processUser(user.get());

    }

    @Test
    public void should_correct_process_correct(){

        List<ConsumerError> consumerErrors = consumerErrorService.findAllToRepair("user");

        consumerErrors.forEach(item -> {
            Optional<User> user =userRepository.findById(item.getMainId());

            userGroupNumBusiness.processUser(user.get());
            item.setRepairStatus(1);
            consumerErrorService.save(item);
        });



    }

}
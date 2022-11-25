package com.qianyi.paycore.service;

import com.qianyi.paycore.model.User;
import com.qianyi.paycore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User findById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }

        return null;
    }

    public User findByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public void setSecretById(Long id,String secret) {
        userRepository.setSecretById(id, secret);
    }
}

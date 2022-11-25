package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.UserRepository;
import com.qianyi.modulecommon.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"user"})
public class UserReportService {

    @Autowired
    UserRepository userRepository;

    public User findUserByUserIdUse(Long userId) {
        return userRepository.findUserByUserIdUse(userId);
    }
}

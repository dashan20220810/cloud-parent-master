package com.qianyi.casinocore.util;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulespringcacheredis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 批量初始化100个邀请码
 */
@Component
@Order(3)
public class GenerateInviteCodeRunner implements CommandLineRunner {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Qualifier("asyncExecutor")
    private Executor executor;

    private final static Integer initNum = 100;

    @Override
    public void run(String... args) throws Exception {
        boolean hasKey = redisUtil.hasKey(Constants.REDIS_INVITECODELIST);
        if (!hasKey) {
            batchGenerateInviteCode();
        }
    }

    /**
     * 批量生成邀请码
     */
    public synchronized void batchGenerateInviteCode() {
        boolean hasKey = redisUtil.hasKey(Constants.REDIS_INVITECODELIST);
        if (hasKey){
            return;
        }
        Set<String> sets = new HashSet<>();
        //防止生成的100个里面有重复的
        for (int i = 0; i < initNum; i++) {
            sets.add(createInviteCode());
        }
        for (String inviteCode : sets) {
            redisUtil.lSet(Constants.REDIS_INVITECODELIST, inviteCode);
        }
    }

    /**
     * 生成邀请码
     *
     * @return
     */
    public String createInviteCode() {
        User user = null;
        String inviteCode = null;
        do {
            inviteCode = InviteCodeUtil.randomCode6();
            user = userService.findByInviteCode(inviteCode);
        } while (user != null && !ObjectUtils.isEmpty(inviteCode));
        return inviteCode;
    }

    /**
     * 获取邀请码
     *
     * @return
     */
    public String getInviteCode() {
        Object val = redisUtil.lleftPop(Constants.REDIS_INVITECODELIST);
        if (!ObjectUtils.isEmpty(val)) {
            return val.toString();
        }
        //先生成一个
        String inviteCode = createInviteCode();
        //再异步批量生成
        CompletableFuture.runAsync(() -> {
            batchGenerateInviteCode();
        }, executor);
        return inviteCode;
    }
}

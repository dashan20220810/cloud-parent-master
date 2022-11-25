package com.qianyi.casinocore.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
public class RedisLockUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String PROXY_GAME_RECORD_REPORT_BUSINESS ="proxyGameRecordReportBusiness:{0}";

    //分布式锁过期时间s可以根据自己的业务调整
    private static final Long LOCK_REDIS_TIMEOUT = 10L;

    //分布式锁休眠 至 再次尝试获取的 等待时间 ms 可以根据自己的业务调整
    public static final Long LOCK_REDIS_WAIT = 500L;

    public Boolean getLock(String key, String value){
        Boolean lockStatus = this.redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(LOCK_REDIS_TIMEOUT));
        return lockStatus;
    }

    /**
     * 释放锁
     *
     * @param key
     * @param value
     * @return
     */
    public Long releaseLock(String key, String value){
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript,Long.class);
        Long releaseStatus = (Long)this.redisTemplate.execute(redisScript, Collections.singletonList(key),value);
        return releaseStatus;
    }

}

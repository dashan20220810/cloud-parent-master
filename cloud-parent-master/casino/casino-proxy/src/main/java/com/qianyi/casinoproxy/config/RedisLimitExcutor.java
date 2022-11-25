package com.qianyi.casinoproxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * IP限流
 */
@Component
public class RedisLimitExcutor {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 单位时间次数
     */
    private int limitCount = 50;

    /**
     * 单位时间 秒
     */
    private int seconds = 1;

    /**
     * 令牌的
     *
     * @param key        key值
     * @return
     */
    public boolean tryAccess(String key) {
        String luaScript = buildLuaScript();
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        List<String> keys = new ArrayList<>();
        keys.add(key);
        Object obj = redisTemplate.execute(redisScript, keys, limitCount, seconds);
        if (obj == null) {
            return true;
        }
        int count = Integer.parseInt(obj.toString());
        if (count != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 脚本
     *
     * @return
     */
    private static final String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append(" local key = KEYS[1]");
        lua.append("\nlocal limit = tonumber(ARGV[1])");
        lua.append("\nlocal curentLimit = tonumber(redis.call('get', key) or \"0\")");
        lua.append("\nif curentLimit + 1 > limit then");
        lua.append("\nreturn 0");
        lua.append("\nelse");
        lua.append("\n redis.call(\"INCRBY\", key, 1)");
        lua.append("\nredis.call(\"EXPIRE\", key, ARGV[2])");
        lua.append("\nreturn curentLimit + 1");
        lua.append("\nend");
        return lua.toString();
    }
}



package com.qianyi.modulespringcacheredis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Component("cusKeyGenerator")
public class CusKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder builder = new StringBuilder();
        String sp = ".";
        builder.append(target.getClass().getSimpleName()).append(sp);
        builder.append(method.getName()).append(sp);

        if(params.length == 0){
            log.info("1{}",builder.toString());
            return builder.toString();
        }
        builder.append(StringUtils.arrayToCommaDelimitedString(params));
        Object param = params[0];

        if(param instanceof Map){
            Map<String,Object> map = (Map<String, Object>) param;
            if(map.isEmpty()){
                log.info(2+builder.toString());
                return builder.toString();
            }

            for(String key:map.keySet()){
                builder.append(key).append("-").append(map.get(key)).append(sp);
            }
            log.info(3+builder.toString());
            return builder.toString();
        }
        return builder.toString();
    }
}

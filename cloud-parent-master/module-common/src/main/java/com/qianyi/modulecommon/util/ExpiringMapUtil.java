package com.qianyi.modulecommon.util;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

public class ExpiringMapUtil {

    // maxSize: 设置最大值,添加第11个entry时，会导致第1个立马过期(即使没到过期时间)
    // expiration：设置每个key有效时间10s, 如果key不设置过期时间，key永久有效。
    // variableExpiration: 允许更新过期时间值,如果不设置variableExpiration，不允许后面更改过期时间,一旦执行更改过期时间操作会抛异常UnsupportedOperationException
    // policy:
    //        CREATED: 只在put和replace方法清零过期时间
    //        ACCESSED: 在CREATED策略基础上增加, 在还没过期时get方法清零过期时间。
    //        清零过期时间也就是重置过期时间，重新计算过期时间.
    private static ExpiringMap<String, String> map = ExpiringMap.builder()
            .maxSize(Integer.MAX_VALUE)
            .expiration(80, TimeUnit.SECONDS)
//            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.ACCESSED).build();


    public static ExpiringMap<String, String> putMap(String key, String value) {
        map.put(key, value);
        return map;
    }

    public static ExpiringMap<String, String> getMap() {
        return map;
    }

}

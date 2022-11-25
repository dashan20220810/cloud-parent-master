package com.qianyi.modulecommon.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RequestLimit {

    //限制次数 默认5次
    int limit() default 5;

    //超时时长(秒级）
    int timeout() default 1;

    //超时单位
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

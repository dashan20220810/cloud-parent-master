package com.qianyi.modulecommon.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ExecutorConfig implements AsyncConfigurer {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {

        int cpuNum = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();


//        核心线程数
        executor.setCorePoolSize(cpuNum + 1);
        //最大线程数
        executor.setMaxPoolSize(cpuNum + 1);
        //队列大小
        executor.setQueueCapacity(cpuNum * 10);
        //线程前缀
        executor.setThreadNamePrefix("asyncExecutor->");

        //当pool达到MAX size时，如何处理任务
        //caller_runs: 不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler((new ThreadPoolExecutor.CallerRunsPolicy()));
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return AsyncConfigurer.super.getAsyncUncaughtExceptionHandler();
    }

    class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            if (throwable instanceof Exception) {
                Exception exception = (Exception) throwable;
                System.out.println(exception.getMessage());
            }
            throwable.printStackTrace();
        }
    }
}

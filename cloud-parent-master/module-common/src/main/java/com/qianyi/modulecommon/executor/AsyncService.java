package com.qianyi.modulecommon.executor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public interface AsyncService<T extends JobSuperVo> {

    /**
     * 线程任务实现此方法
     */
    @Async("asyncExecutor")
    void executeAsync(T  t);
}

package com.qianyi.casinocore.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BillThreadPool {
    private static final Logger logger = LogManager.getLogger(BillThreadPool.class);
    private volatile int threadNum;

    private AtomicInteger threadAlive = new AtomicInteger();

    private ReentrantLock reentrantLock = new ReentrantLock();

    private Condition condition = reentrantLock.newCondition();

    private ExecutorService executorService;

    public BillThreadPool(int threadNum) {
        this.threadNum = threadNum;
        this.executorService = Executors.newFixedThreadPool(threadNum);
    }

    /**
     * 使用无界队列
     * @param runnable
     */
    public void executeLinkedBlockingQueue(Runnable runnable){
        executorService.execute(runnable);
    }
    public void execute(final Runnable runnable) {
        if (threadAlive.get() >= threadNum) {
            try {
                reentrantLock.lock();
                while (threadAlive.get() >= threadNum) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        logger.error("新增线程异常", e);
                    }
                }
            } finally {
                reentrantLock.unlock();
            }
        }
        threadAlive.incrementAndGet();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    try {
                        reentrantLock.lock();
                        threadAlive.decrementAndGet();
                        condition.signal();
                    } finally {
                        reentrantLock.unlock();
                    }
                }
            }
        });
    }



    /**
     * 挂起该线程
     *
     * @param reentrantLock 重入锁
     * @param condition     条件
     * @param atomicInteger 计数器
     */
    public static void toWaiting(ReentrantLock reentrantLock, Condition condition, AtomicInteger atomicInteger) {
        if (atomicInteger.get() >= 1) {
            try {
                reentrantLock.lock();
                while (atomicInteger.get() >= 1) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        logger.error("挂起线程异常", e);
                    }
                }
            } finally {
                reentrantLock.unlock();
            }
        }
    }
    /**
     * 唤醒该凭证库的线程
     *
     * @param reentrantLock
     * @param condition
     */
    public static void toResume(ReentrantLock reentrantLock, Condition condition) {
        try {
            reentrantLock.lock();
            condition.signal();
        } finally {
            reentrantLock.unlock();
        }
    }

    public AtomicInteger getThreadAlive() {
        return threadAlive;
    }

    public int getThreadNum() {
        return threadNum;
    }
}

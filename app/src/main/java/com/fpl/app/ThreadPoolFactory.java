package com.fpl.app;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by rocky on 2017/12/19.
 */

public class ThreadPoolFactory {

    private static ThreadPoolFactory instance;

    private final int CORE_POOL_SIZE = 1;//核心线程数
    private final int MAX_POOL_SIZE = 3;//最大线程数
    private final int BLOCK_SIZE = 2;//阻塞队列大小
    private final long KEEP_ALIVE_TIME = 2;//空闲线程超时时间
    private ThreadPoolExecutor executorPool;


    private ThreadPoolFactory() {
    }

    public static ThreadPoolFactory getInstance() {
        if (null == instance) {
            synchronized (ThreadPoolFactory.class) {
                if (null == instance) {
                    instance = new ThreadPoolFactory();
                }
            }
        }
        return instance;
    }

    public ThreadPoolExecutor getExecutorPool() {

        if (null == executorPool) {
            // 创建一个核心线程数为3、最大线程数为8，缓存队列大小为5的线程池
            executorPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(BLOCK_SIZE),
                    Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

            executorPool.allowCoreThreadTimeOut(true);
        }

        return executorPool;

    }

}

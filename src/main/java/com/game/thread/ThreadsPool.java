package com.game.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池
 */
public class ThreadsPool
{
    // 线程池实例
    private final ExecutorService executorService;

    // 创建一个可伸缩的线程池
    public ThreadsPool()
    {
        executorService=Executors.newCachedThreadPool();
    }

    // 提交任务到线程池
    public void submitTask(Runnable task)
    {
        executorService.submit(task);
    }

    // 关闭线程池
    public void shutdown()
    {
        executorService.shutdown();
    }

    // 判断线程池是否关闭
    public boolean isShutdown()
    {
        return executorService.isShutdown() || executorService.isTerminated();
    }
}

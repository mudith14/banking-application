package com.example.bankingdemo.concurrency;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom ThreadFactory that creates named daemon threads for banking operations.
 * Thread names follow the pattern "{namePrefix}{sequentialNumber}" (e.g., "banking-worker-1").
 */
public class BankingThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;
    private final String namePrefix;
    private final AtomicInteger counter = new AtomicInteger(1);
    private final boolean daemon = true;

    /**
     * Creates a factory with the given name prefix and a default thread group.
     *
     * @param namePrefix prefix for thread names (e.g., "banking-worker-")
     */
    public BankingThreadFactory(String namePrefix) {
        this(namePrefix, new ThreadGroup(namePrefix + "group"));
    }

    /**
     * Creates a factory with the given name prefix and thread group.
     *
     * @param namePrefix prefix for thread names
     * @param group      thread group for created threads
     */
    public BankingThreadFactory(String namePrefix, ThreadGroup group) {
        this.namePrefix = namePrefix;
        this.threadGroup = group;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup, r, namePrefix + counter.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}

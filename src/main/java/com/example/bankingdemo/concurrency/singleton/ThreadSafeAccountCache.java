package com.example.bankingdemo.concurrency.singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton using double-checked locking with a volatile instance.
 * Caches account balances in a ConcurrentHashMap.
 */
public class ThreadSafeAccountCache {

    private static volatile ThreadSafeAccountCache instance;

    private final ConcurrentHashMap<String, Double> cache = new ConcurrentHashMap<>();

    private ThreadSafeAccountCache() {
        // private constructor to prevent external instantiation
    }

    /**
     * Get the singleton instance using double-checked locking.
     *
     * @return the singleton ThreadSafeAccountCache instance
     */
    public static ThreadSafeAccountCache getInstance() {
        if (instance == null) {
            synchronized (ThreadSafeAccountCache.class) {
                if (instance == null) {
                    instance = new ThreadSafeAccountCache();
                }
            }
        }
        return instance;
    }

    /**
     * Cache a balance for the given account number.
     *
     * @param accountNumber the account number
     * @param balance       the balance to cache
     */
    public void put(String accountNumber, double balance) {
        cache.put(accountNumber, balance);
    }

    /**
     * Retrieve a cached balance.
     *
     * @param accountNumber the account number
     * @return the cached balance, or null if not present
     */
    public Double get(String accountNumber) {
        return cache.get(accountNumber);
    }
}

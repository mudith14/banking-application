package com.example.bankingdemo.concurrency.lock;

import com.example.bankingdemo.concurrency.exception.LockAcquisitionTimeoutException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Manages per-account ReentrantLock and ReadWriteLock instances for fine-grained locking.
 * Locks are created lazily via computeIfAbsent and stored in ConcurrentHashMaps.
 */
@Component
public class AccountLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> readWriteLocks = new ConcurrentHashMap<>();

    /**
     * Acquire the ReentrantLock for the given account, execute the action, and release.
     *
     * @param accountNumber the account to lock
     * @param action        the action to run while holding the lock
     */
    public void executeWithLock(String accountNumber, Runnable action) {
        ReentrantLock lock = locks.computeIfAbsent(accountNumber, k -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Acquire the read lock for the given account, execute the reader, and return the result.
     *
     * @param accountNumber the account to read-lock
     * @param reader        the supplier to execute while holding the read lock
     * @param <T>           the return type
     * @return the result of the reader
     */
    public <T> T executeWithReadLock(String accountNumber, Supplier<T> reader) {
        ReentrantReadWriteLock rwLock = readWriteLocks.computeIfAbsent(accountNumber, k -> new ReentrantReadWriteLock());
        rwLock.readLock().lock();
        try {
            return reader.get();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Acquire the write lock for the given account, execute the writer, and release.
     *
     * @param accountNumber the account to write-lock
     * @param writer        the action to run while holding the write lock
     */
    public void executeWithWriteLock(String accountNumber, Runnable writer) {
        ReentrantReadWriteLock rwLock = readWriteLocks.computeIfAbsent(accountNumber, k -> new ReentrantReadWriteLock());
        rwLock.writeLock().lock();
        try {
            writer.run();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Try to acquire the ReentrantLock within the given timeout. If acquired, run the action
     * and return true. If the timeout is exceeded, throw LockAcquisitionTimeoutException.
     *
     * @param accountNumber the account to lock
     * @param timeout       the maximum time to wait
     * @param unit          the time unit
     * @param action        the action to run while holding the lock
     * @return true if the lock was acquired and the action executed
     * @throws LockAcquisitionTimeoutException if the lock could not be acquired within the timeout
     */
    public boolean tryExecuteWithLock(String accountNumber, long timeout, TimeUnit unit, Runnable action) {
        ReentrantLock lock = locks.computeIfAbsent(accountNumber, k -> new ReentrantLock());
        boolean acquired;
        try {
            acquired = lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionTimeoutException(
                    "Interrupted while waiting for lock on account " + accountNumber, e);
        }
        if (!acquired) {
            throw new LockAcquisitionTimeoutException(
                    "Failed to acquire lock on account " + accountNumber + " within " + timeout + " " + unit);
        }
        try {
            action.run();
            return true;
        } finally {
            lock.unlock();
        }
    }
}

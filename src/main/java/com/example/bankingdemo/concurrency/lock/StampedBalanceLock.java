package com.example.bankingdemo.concurrency.lock;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock-based balance access supporting optimistic reads,
 * pessimistic reads, writes, and lock upgrades.
 */
@Component
public class StampedBalanceLock {

    private final ConcurrentHashMap<String, StampedLock> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> balances = new ConcurrentHashMap<>();

    private StampedLock getLock(String accountNumber) {
        return locks.computeIfAbsent(accountNumber, k -> new StampedLock());
    }

    /**
     * Read a balance using optimistic read. Falls back to a pessimistic read lock
     * if the optimistic stamp is invalidated by a concurrent write.
     *
     * @param accountNumber the account number
     * @return the balance, or 0.0 if the account has no stored balance
     */
    public double optimisticReadBalance(String accountNumber) {
        StampedLock lock = getLock(accountNumber);

        long stamp = lock.tryOptimisticRead();
        double balance = balances.getOrDefault(accountNumber, 0.0);

        if (!lock.validate(stamp)) {
            // Fallback to pessimistic read lock
            stamp = lock.readLock();
            try {
                balance = balances.getOrDefault(accountNumber, 0.0);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return balance;
    }

    /**
     * Write a balance using a write lock.
     *
     * @param accountNumber the account number
     * @param balance       the new balance
     */
    public void writeBalance(String accountNumber, double balance) {
        StampedLock lock = getLock(accountNumber);
        long stamp = lock.writeLock();
        try {
            balances.put(accountNumber, balance);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Read the current balance, then apply a delta using lock upgrade.
     * Acquires a read lock first, attempts to convert to a write lock.
     * If conversion fails, releases the read lock and acquires a write lock directly.
     *
     * @param accountNumber the account number
     * @param delta         the amount to add (positive) or subtract (negative)
     */
    public void readThenWrite(String accountNumber, double delta) {
        StampedLock lock = getLock(accountNumber);
        long stamp = lock.readLock();
        try {
            double currentBalance = balances.getOrDefault(accountNumber, 0.0);
            long writeStamp = lock.tryConvertToWriteLock(stamp);
            if (writeStamp != 0L) {
                stamp = writeStamp;
                balances.put(accountNumber, currentBalance + delta);
            } else {
                lock.unlockRead(stamp);
                stamp = lock.writeLock();
                // Re-read after acquiring write lock since balance may have changed
                currentBalance = balances.getOrDefault(accountNumber, 0.0);
                balances.put(accountNumber, currentBalance + delta);
            }
        } finally {
            lock.unlock(stamp);
        }
    }
}

package com.example.bankingdemo.concurrency.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * Semaphore-based rate limiter that restricts the maximum number of
 * concurrent banking operations.
 */
@Component
public class RateLimiter {

    private final Semaphore semaphore;

    public RateLimiter(@Value("${banking.rate-limiter.permits:10}") int permits) {
        this.semaphore = new Semaphore(permits);
    }

    /**
     * Execute an operation after acquiring a semaphore permit.
     * The permit is always released in a finally block.
     *
     * @param operation the callable to execute
     * @param <T>       the return type
     * @return the result of the operation
     * @throws Exception if the operation throws or the thread is interrupted
     */
    public <T> T executeWithPermit(Callable<T> operation) throws Exception {
        semaphore.acquire();
        try {
            return operation.call();
        } finally {
            semaphore.release();
        }
    }

    /**
     * @return the number of currently available permits
     */
    public int availablePermits() {
        return semaphore.availablePermits();
    }
}

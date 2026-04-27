package com.example.bankingdemo.concurrency.atomic;

import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.service.TransactionLog;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Concurrency-aware wrapper around TransactionLog that tracks the total
 * transaction count atomically using an AtomicLong counter.
 * Does not modify the existing TransactionLog.
 */
@Component
public class AtomicTransactionCounter {

    private final TransactionLog transactionLog;
    private final AtomicLong transactionCount = new AtomicLong(0);

    public AtomicTransactionCounter(TransactionLog transactionLog) {
        this.transactionLog = transactionLog;
    }

    /**
     * Records a transaction in the underlying TransactionLog and atomically
     * increments the transaction counter.
     *
     * @param transaction the transaction to record
     */
    public void recordAndCount(Transaction transaction) {
        transactionLog.addTransaction(transaction);
        transactionCount.incrementAndGet();
    }

    /**
     * @return the total number of transactions recorded through this wrapper
     */
    public long getTransactionCount() {
        return transactionCount.get();
    }
}

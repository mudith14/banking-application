package com.example.bankingdemo.concurrency.queue;

import com.example.bankingdemo.concurrency.exception.QueueFullException;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import com.example.bankingdemo.repository.AccountRepository;
import com.example.bankingdemo.service.TransactionLog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Bounded blocking queue implementing the producer-consumer pattern
 * for transaction processing. A background consumer thread continuously
 * takes transactions from the queue and processes them.
 * Supports poison-pill shutdown (transaction with amount == -1).
 */
@Component
public class TransactionQueue {

    private static final Logger logger = LoggerFactory.getLogger(TransactionQueue.class);

    private final LinkedBlockingQueue<Transaction> queue;
    private final AccountRepository accountRepository;
    private final TransactionLog transactionLog;

    private Thread consumerThread;
    private volatile boolean running = true;

    public TransactionQueue(AccountRepository accountRepository,
                            TransactionLog transactionLog,
                            @Value("${banking.queue.capacity:100}") int capacity) {
        this.accountRepository = accountRepository;
        this.transactionLog = transactionLog;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * Enqueue a transaction. Uses offer with a 2-second timeout.
     *
     * @param tx the transaction to enqueue
     * @return true if successfully enqueued
     * @throws QueueFullException if the queue is full after the timeout
     */
    public boolean enqueue(Transaction tx) {
        try {
            boolean offered = queue.offer(tx, 2, TimeUnit.SECONDS);
            if (!offered) {
                throw new QueueFullException("Transaction queue is full, try again later");
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QueueFullException("Interrupted while enqueuing transaction", e);
        }
    }

    /**
     * @return the current number of transactions waiting in the queue
     */
    public int getQueueSize() {
        return queue.size();
    }

    @PostConstruct
    public void startConsumer() {
        consumerThread = new Thread(this::consumeLoop, "tx-queue-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
        logger.info("Transaction queue consumer thread started");
    }

    private void consumeLoop() {
        while (running) {
            try {
                Transaction tx = queue.take();

                // Poison pill check: amount == -1 signals shutdown
                if (tx.getAmount() == -1) {
                    logger.info("Poison pill received, consumer shutting down");
                    break;
                }

                processTransaction(tx);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Consumer thread interrupted, exiting");
                break;
            } catch (Exception e) {
                logger.error("Error processing queued transaction: {}", e.getMessage(), e);
            }
        }
    }

    private void processTransaction(Transaction tx) {
        if (tx.getTransactionType() == TransactionType.DEPOSIT
                && tx.getSourceAccountNumber() != null) {
            accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .ifPresent(account -> {
                        account.deposit(tx.getAmount());
                        transactionLog.addTransaction(tx);
                    });
        } else if (tx.getTransactionType() == TransactionType.WITHDRAWAL
                && tx.getSourceAccountNumber() != null) {
            accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .ifPresent(account -> {
                        account.withdraw(tx.getAmount());
                        transactionLog.addTransaction(tx);
                    });
        } else if (tx.getTransactionType() == TransactionType.TRANSFER) {
            Account source = accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .orElse(null);
            Account target = accountRepository.findByAccountNumber(tx.getTargetAccountNumber())
                    .orElse(null);
            if (source != null && target != null) {
                source.withdraw(tx.getAmount());
                target.deposit(tx.getAmount());
                transactionLog.addTransaction(tx);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down TransactionQueue...");
        running = false;
        // Enqueue poison pill to unblock the consumer
        try {
            queue.offer(new Transaction(null, -1, java.time.LocalDateTime.now(), null, null),
                    1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Wait for consumer thread to finish
        if (consumerThread != null) {
            try {
                consumerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

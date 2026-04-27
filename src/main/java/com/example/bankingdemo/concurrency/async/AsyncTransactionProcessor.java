package com.example.bankingdemo.concurrency.async;

import com.example.bankingdemo.concurrency.BankingThreadFactory;
import com.example.bankingdemo.concurrency.model.TransactionResult;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import com.example.bankingdemo.repository.AccountRepository;
import com.example.bankingdemo.service.TransactionLog;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Processes transactions asynchronously using CompletableFuture chains.
 * Pipeline: validate → execute → log → return result.
 */
@Service
public class AsyncTransactionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTransactionProcessor.class);

    private final AccountRepository accountRepository;
    private final TransactionLog transactionLog;
    private final ExecutorService executorService;

    private volatile boolean shutdownRequested = false;
    private volatile long lastProcessedTimestamp = 0L;

    public AsyncTransactionProcessor(AccountRepository accountRepository,
                                     TransactionLog transactionLog) {
        this.accountRepository = accountRepository;
        this.transactionLog = transactionLog;
        this.executorService = Executors.newFixedThreadPool(4,
                new BankingThreadFactory("async-tx-"));
    }

    /**
     * Process a single transaction asynchronously.
     * Chain: validate → execute → log → return TransactionResult.
     * Failures are caught with exceptionally() and returned as failed results.
     */
    public CompletableFuture<TransactionResult> processAsync(Transaction tx) {
        long startTime = System.currentTimeMillis();
        String txId = UUID.randomUUID().toString();

        return CompletableFuture
                .supplyAsync(() -> validate(tx), executorService)
                .thenApply(validTx -> execute(validTx))
                .thenApply(executedTx -> {
                    transactionLog.addTransaction(executedTx);
                    lastProcessedTimestamp = System.currentTimeMillis();
                    long elapsed = System.currentTimeMillis() - startTime;
                    return new TransactionResult(txId, true,
                            "Transaction processed successfully", elapsed);
                })
                .exceptionally(ex -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    logger.error("Async transaction failed: {}", ex.getMessage());
                    return new TransactionResult(txId, false,
                            ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(),
                            elapsed);
                });
    }

    /**
     * Process all transactions concurrently and return a combined future
     * that completes when every transaction has been processed.
     */
    public CompletableFuture<List<TransactionResult>> processAllAsync(List<Transaction> transactions) {
        List<CompletableFuture<TransactionResult>> futures = transactions.stream()
                .map(this::processAsync)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    public long getLastProcessedTimestamp() {
        return lastProcessedTimestamp;
    }

    // --- internal helpers ---

    private Transaction validate(Transaction tx) {
        if (tx.getSourceAccountNumber() != null) {
            accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Source account not found: " + tx.getSourceAccountNumber()));
        }
        if (tx.getTargetAccountNumber() != null) {
            accountRepository.findByAccountNumber(tx.getTargetAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Target account not found: " + tx.getTargetAccountNumber()));
        }
        return tx;
    }

    private Transaction execute(Transaction tx) {
        if (tx.getTransactionType() == TransactionType.DEPOSIT
                && tx.getSourceAccountNumber() != null) {
            Account account = accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .orElseThrow();
            account.deposit(tx.getAmount());
        } else if (tx.getTransactionType() == TransactionType.WITHDRAWAL
                && tx.getSourceAccountNumber() != null) {
            Account account = accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .orElseThrow();
            account.withdraw(tx.getAmount());
        } else if (tx.getTransactionType() == TransactionType.TRANSFER) {
            Account source = accountRepository.findByAccountNumber(tx.getSourceAccountNumber())
                    .orElseThrow();
            Account target = accountRepository.findByAccountNumber(tx.getTargetAccountNumber())
                    .orElseThrow();
            source.withdraw(tx.getAmount());
            target.deposit(tx.getAmount());
        }
        return tx;
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down AsyncTransactionProcessor...");
        shutdownRequested = true;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}

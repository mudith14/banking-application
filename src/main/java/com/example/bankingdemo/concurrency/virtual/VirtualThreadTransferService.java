package com.example.bankingdemo.concurrency.virtual;

import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import com.example.bankingdemo.repository.AccountRepository;
import com.example.bankingdemo.service.TransactionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service that executes batch fund transfers using virtual threads when available,
 * falling back to a fixed platform thread pool on Java 17.
 * <p>
 * Virtual threads (Project Loom) are available natively from Java 21. On Java 17,
 * this service attempts to create a virtual-thread executor via reflection and
 * gracefully falls back to platform threads if unavailable.
 */
@Service
public class VirtualThreadTransferService {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadTransferService.class);

    private final AccountRepository accountRepository;
    private final TransactionLog transactionLog;

    public VirtualThreadTransferService(AccountRepository accountRepository,
                                        TransactionLog transactionLog) {
        this.accountRepository = accountRepository;
        this.transactionLog = transactionLog;
    }

    /**
     * Execute a batch of transfers, one task per transfer.
     * Tries virtual threads first; falls back to platform threads.
     *
     * @param transfers list of transfer maps, each with "sourceAccount", "targetAccount", "amount"
     * @return result map with "transferCount", "successCount", "executionTimeMs", "threadType"
     */
    public Map<String, Object> executeBatchTransfers(List<Map<String, Object>> transfers) {
        long startTime = System.currentTimeMillis();

        String threadType;
        ExecutorService executor = createVirtualThreadExecutor();
        if (executor != null) {
            threadType = "virtual";
            log.info("Using virtual thread executor for {} transfers", transfers.size());
        } else {
            int poolSize = Math.min(transfers.size(), Runtime.getRuntime().availableProcessors());
            executor = Executors.newFixedThreadPool(Math.max(1, poolSize));
            threadType = "platform";
            log.warn("Virtual threads not available, falling back to platform thread pool (size={})", poolSize);
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Map<String, Object> transfer : transfers) {
            futures.add(executor.submit(() -> executeSingleTransfer(transfer)));
        }

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            try {
                if (Boolean.TRUE.equals(future.get())) {
                    successCount++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Transfer task interrupted", e);
            } catch (ExecutionException e) {
                log.error("Transfer task failed: {}", e.getCause().getMessage());
            }
        }

        executor.shutdown();

        long executionTimeMs = System.currentTimeMillis() - startTime;

        log.info("Batch transfers complete: {}/{} succeeded in {}ms using {} threads",
                successCount, transfers.size(), executionTimeMs, threadType);

        Map<String, Object> result = new HashMap<>();
        result.put("transferCount", transfers.size());
        result.put("successCount", successCount);
        result.put("executionTimeMs", executionTimeMs);
        result.put("threadType", threadType);
        return result;
    }

    /**
     * Execute a single transfer between two accounts.
     */
    private boolean executeSingleTransfer(Map<String, Object> transfer) {
        String sourceAccNum = (String) transfer.get("sourceAccount");
        String targetAccNum = (String) transfer.get("targetAccount");
        double amount = ((Number) transfer.get("amount")).doubleValue();

        Account source = accountRepository.findByAccountNumber(sourceAccNum).orElse(null);
        Account target = accountRepository.findByAccountNumber(targetAccNum).orElse(null);

        if (source == null || target == null) {
            log.warn("Transfer skipped: account not found (source={}, target={})",
                    sourceAccNum, targetAccNum);
            return false;
        }

        // Lock ordering by account number to prevent deadlocks
        Account firstLock = sourceAccNum.compareTo(targetAccNum) < 0 ? source : target;
        Account secondLock = sourceAccNum.compareTo(targetAccNum) < 0 ? target : source;

        synchronized (firstLock) {
            synchronized (secondLock) {
                try {
                    source.withdraw(amount);
                    target.deposit(amount);
                    transactionLog.addTransaction(new Transaction(
                            TransactionType.TRANSFER, amount, LocalDateTime.now(),
                            sourceAccNum, targetAccNum));
                    return true;
                } catch (Exception e) {
                    log.warn("Transfer failed {}->{} amount {}: {}",
                            sourceAccNum, targetAccNum, amount, e.getMessage());
                    return false;
                }
            }
        }
    }

    /**
     * Attempt to create a virtual thread executor via reflection.
     * Returns null if virtual threads are not available (e.g., Java 17).
     */
    private ExecutorService createVirtualThreadExecutor() {
        try {
            // Try Executors.newVirtualThreadPerTaskExecutor() (Java 21+)
            Method method = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            return (ExecutorService) method.invoke(null);
        } catch (NoSuchMethodException e) {
            log.debug("Executors.newVirtualThreadPerTaskExecutor() not available");
        } catch (Exception e) {
            log.debug("Failed to create virtual thread executor: {}", e.getMessage());
        }
        return null;
    }
}

package com.example.bankingdemo.concurrency;

import com.example.bankingdemo.concurrency.lock.AccountLockManager;
import com.example.bankingdemo.exception.AccountNotFoundException;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import com.example.bankingdemo.repository.AccountRepository;
import com.example.bankingdemo.service.TransactionLog;
import org.springframework.stereotype.Service;

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
 * Service that executes concurrent fund transfers with proper lock ordering
 * to prevent deadlocks. Locks are acquired in lexicographic order of account numbers.
 */
@Service
public class ConcurrentTransferService {

    private final AccountRepository accountRepository;
    private final TransactionLog transactionLog;
    private final AccountLockManager accountLockManager;

    public ConcurrentTransferService(AccountRepository accountRepository,
                                     TransactionLog transactionLog,
                                     AccountLockManager accountLockManager) {
        this.accountRepository = accountRepository;
        this.transactionLog = transactionLog;
        this.accountLockManager = accountLockManager;
    }

    /**
     * Transfer funds from source to target with deadlock-free lock ordering.
     * Locks are acquired in lexicographic order (lower account number first).
     * Uses synchronized blocks on Account objects for thread safety.
     *
     * @param sourceAccNum the source account number
     * @param targetAccNum the target account number
     * @param amount       the amount to transfer
     */
    public void concurrentTransfer(String sourceAccNum, String targetAccNum, double amount) {
        Account source = accountRepository.findByAccountNumber(sourceAccNum)
                .orElseThrow(() -> new AccountNotFoundException(sourceAccNum));
        Account target = accountRepository.findByAccountNumber(targetAccNum)
                .orElseThrow(() -> new AccountNotFoundException(targetAccNum));

        // Determine lock ordering by lexicographic comparison to prevent deadlocks
        Account firstLock;
        Account secondLock;
        if (sourceAccNum.compareTo(targetAccNum) < 0) {
            firstLock = source;
            secondLock = target;
        } else {
            firstLock = target;
            secondLock = source;
        }

        synchronized (firstLock) {
            synchronized (secondLock) {
                source.withdraw(amount);
                target.deposit(amount);
                transactionLog.addTransaction(new Transaction(
                        TransactionType.TRANSFER, amount, LocalDateTime.now(),
                        sourceAccNum, targetAccNum));
            }
        }
    }

    /**
     * Execute multiple transfers concurrently using an ExecutorService.
     * Each transfer map must contain "sourceAccount", "targetAccount", and "amount".
     *
     * @param transfers list of transfer descriptors
     * @return list of result maps, each containing "sourceAccount", "targetAccount",
     *         "amount", "success", and optionally "error"
     */
    public List<Map<String, Object>> executeConcurrentTransfers(List<Map<String, Object>> transfers) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(transfers.size(), Runtime.getRuntime().availableProcessors()));
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        for (Map<String, Object> transfer : transfers) {
            futures.add(executor.submit(() -> {
                String sourceAcc = (String) transfer.get("sourceAccount");
                String targetAcc = (String) transfer.get("targetAccount");
                double amt = ((Number) transfer.get("amount")).doubleValue();

                Map<String, Object> result = new HashMap<>();
                result.put("sourceAccount", sourceAcc);
                result.put("targetAccount", targetAcc);
                result.put("amount", amt);

                try {
                    concurrentTransfer(sourceAcc, targetAcc, amt);
                    result.put("success", true);
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("error", e.getMessage());
                }
                return result;
            }));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Future<Map<String, Object>> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", "Transfer interrupted");
                results.add(errorResult);
            } catch (ExecutionException e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", e.getCause().getMessage());
                results.add(errorResult);
            }
        }

        executor.shutdown();
        return results;
    }
}

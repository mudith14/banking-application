package com.example.bankingdemo.concurrency;

import com.example.bankingdemo.concurrency.async.AsyncTransactionProcessor;
import com.example.bankingdemo.concurrency.detection.DeadlockDetector;
import com.example.bankingdemo.concurrency.forkjoin.FraudDetectionService;
import com.example.bankingdemo.concurrency.leaderboard.BalanceLeaderboard;
import com.example.bankingdemo.concurrency.lock.AccountLockManager;
import com.example.bankingdemo.concurrency.lock.StampedBalanceLock;
import com.example.bankingdemo.concurrency.model.AuditReport;
import com.example.bankingdemo.concurrency.model.FraudScanResult;
import com.example.bankingdemo.concurrency.model.RateLimiter;
import com.example.bankingdemo.concurrency.model.TransactionResult;
import com.example.bankingdemo.concurrency.notification.AccountNotificationQueue;
import com.example.bankingdemo.concurrency.phaser.AuditEventBus;
import com.example.bankingdemo.concurrency.pool.BatchInterestService;
import com.example.bankingdemo.concurrency.queue.TransactionQueue;
import com.example.bankingdemo.concurrency.singleton.AuditLogger;
import com.example.bankingdemo.concurrency.singleton.BankingConfiguration;
import com.example.bankingdemo.concurrency.singleton.ThreadSafeAccountCache;
import com.example.bankingdemo.concurrency.virtual.VirtualThreadTransferService;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import com.example.bankingdemo.repository.AccountRepository;
import com.example.bankingdemo.service.TransactionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * REST controller exposing endpoints for all concurrency demonstrations.
 */
@RestController
@RequestMapping("/api/concurrency")
public class ConcurrencyDemoController {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyDemoController.class);

    private final ConcurrentTransferService concurrentTransferService;
    private final BatchInterestService batchInterestService;
    private final AsyncTransactionProcessor asyncTransactionProcessor;
    private final TransactionQueue transactionQueue;
    private final RateLimiter rateLimiter;
    private final FraudDetectionService fraudDetectionService;
    private final AuditEventBus auditEventBus;
    private final VirtualThreadTransferService virtualThreadTransferService;
    private final DeadlockDetector deadlockDetector;
    private final StampedBalanceLock stampedBalanceLock;
    private final AccountLockManager accountLockManager;
    private final AccountRepository accountRepository;
    private final TransactionLog transactionLog;
    private final AccountNotificationQueue accountNotificationQueue;
    private final BalanceLeaderboard balanceLeaderboard;

    public ConcurrencyDemoController(ConcurrentTransferService concurrentTransferService,
                                     BatchInterestService batchInterestService,
                                     AsyncTransactionProcessor asyncTransactionProcessor,
                                     TransactionQueue transactionQueue,
                                     RateLimiter rateLimiter,
                                     FraudDetectionService fraudDetectionService,
                                     AuditEventBus auditEventBus,
                                     VirtualThreadTransferService virtualThreadTransferService,
                                     DeadlockDetector deadlockDetector,
                                     StampedBalanceLock stampedBalanceLock,
                                     AccountLockManager accountLockManager,
                                     AccountRepository accountRepository,
                                     TransactionLog transactionLog,
                                     AccountNotificationQueue accountNotificationQueue,
                                     BalanceLeaderboard balanceLeaderboard) {
        this.concurrentTransferService = concurrentTransferService;
        this.batchInterestService = batchInterestService;
        this.asyncTransactionProcessor = asyncTransactionProcessor;
        this.transactionQueue = transactionQueue;
        this.rateLimiter = rateLimiter;
        this.fraudDetectionService = fraudDetectionService;
        this.auditEventBus = auditEventBus;
        this.virtualThreadTransferService = virtualThreadTransferService;
        this.deadlockDetector = deadlockDetector;
        this.stampedBalanceLock = stampedBalanceLock;
        this.accountLockManager = accountLockManager;
        this.accountRepository = accountRepository;
        this.transactionLog = transactionLog;
        this.accountNotificationQueue = accountNotificationQueue;
        this.balanceLeaderboard = balanceLeaderboard;
    }

    // ---- 1. POST /batch-deposit ----

    /**
     * Batch deposits via Runnable threads using BankingThreadFactory.
     * Accepts {"accountNumbers": ["ACC1","ACC2"], "amount": 100}.
     */
    @PostMapping("/batch-deposit")
    public ResponseEntity<Map<String, Object>> batchDeposit(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> accountNumbers = (List<String>) body.get("accountNumbers");
        double amount = ((Number) body.get("amount")).doubleValue();

        BankingThreadFactory factory = new BankingThreadFactory("batch-deposit-");
        List<Map<String, Object>> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(accountNumbers.size());

        for (String accNum : accountNumbers) {
            Runnable task = () -> {
                Thread current = Thread.currentThread();
                log.info("Deposit thread: name={}, state={}, id={}", current.getName(), current.getState(), current.getId());

                Map<String, Object> result = new HashMap<>();
                result.put("accountNumber", accNum);
                result.put("threadName", current.getName());
                result.put("threadId", current.getId());
                result.put("threadState", current.getState().toString());

                try {
                    Account account = accountRepository.findByAccountNumber(accNum).orElse(null);
                    if (account != null) {
                        synchronized (account) {
                            account.deposit(amount);
                        }
                        result.put("success", true);
                        result.put("newBalance", account.getBalance());
                    } else {
                        result.put("success", false);
                        result.put("error", "Account not found: " + accNum);
                    }
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("error", e.getMessage());
                } finally {
                    results.add(result);
                    latch.countDown();
                }
            };

            Thread thread = factory.newThread(task);
            thread.start();
        }

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("totalDeposits", accountNumbers.size());
        return ResponseEntity.ok(response);
    }

    // ---- 2. POST /batch-interest ----

    /**
     * Parallel interest calculation.
     * Accepts {"accountNumbers": ["ACC1","ACC2"]} (optional, if empty use all accounts).
     */
    @PostMapping("/batch-interest")
    public ResponseEntity<Map<String, Object>> batchInterest(@RequestBody(required = false) Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> accountNumbers = (body != null && body.containsKey("accountNumbers"))
                ? (List<String>) body.get("accountNumbers")
                : null;

        double totalInterest;
        int accountCount;

        if (accountNumbers == null || accountNumbers.isEmpty()) {
            totalInterest = batchInterestService.calculateAllAccountsInterest();
            accountCount = accountRepository.findAll().size();
        } else {
            totalInterest = batchInterestService.calculateBatchInterest(accountNumbers);
            accountCount = accountNumbers.size();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalInterest", totalInterest);
        response.put("accountCount", accountCount);
        return ResponseEntity.ok(response);
    }

    // ---- 3. POST /concurrent-transfers ----

    /**
     * Concurrent transfers with lock ordering.
     * Accepts {"transfers": [{"sourceAccount":"ACC1","targetAccount":"ACC2","amount":100}]}.
     */
    @PostMapping("/concurrent-transfers")
    public ResponseEntity<Map<String, Object>> concurrentTransfers(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> transfers = (List<Map<String, Object>>) body.get("transfers");
        List<Map<String, Object>> results = concurrentTransferService.executeConcurrentTransfers(transfers);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("totalTransfers", transfers.size());
        return ResponseEntity.ok(response);
    }

    // ---- 4. POST /async-transfer ----

    /**
     * Async transfer via CompletableFuture.
     * Accepts {"sourceAccount":"ACC1","targetAccount":"ACC2","amount":100}.
     */
    @PostMapping("/async-transfer")
    public ResponseEntity<TransactionResult> asyncTransfer(@RequestBody Map<String, Object> body) {
        String sourceAccount = (String) body.get("sourceAccount");
        String targetAccount = (String) body.get("targetAccount");
        double amount = ((Number) body.get("amount")).doubleValue();

        Transaction tx = new Transaction(
                TransactionType.TRANSFER, amount, LocalDateTime.now(),
                sourceAccount, targetAccount);

        try {
            TransactionResult result = asyncTransactionProcessor.processAsync(tx).get(10, TimeUnit.SECONDS);
            return ResponseEntity.ok(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(
                    new TransactionResult(null, false, "Thread interrupted", 0));
        } catch (ExecutionException e) {
            return ResponseEntity.badRequest().body(
                    new TransactionResult(null, false, e.getCause().getMessage(), 0));
        } catch (TimeoutException e) {
            return ResponseEntity.status(409).body(
                    new TransactionResult(null, false, "Async transfer timed out", 0));
        }
    }

    // ---- 5. POST /batch-transfer ----

    /**
     * CyclicBarrier coordinated settlement.
     * Accepts {"transfers": [{"sourceAccount":"ACC1","targetAccount":"ACC2","amount":100}]}.
     */
    @PostMapping("/batch-transfer")
    public ResponseEntity<Map<String, Object>> batchTransfer(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> transfers = (List<Map<String, Object>>) body.get("transfers");
        int n = transfers.size();

        long startTime = System.currentTimeMillis();
        CyclicBarrier barrier = new CyclicBarrier(n);
        List<Map<String, Object>> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch completionLatch = new CountDownLatch(n);

        BankingThreadFactory factory = new BankingThreadFactory("barrier-transfer-");

        for (Map<String, Object> transfer : transfers) {
            Thread thread = factory.newThread(() -> {
                Map<String, Object> result = new HashMap<>();
                String sourceAcc = (String) transfer.get("sourceAccount");
                String targetAcc = (String) transfer.get("targetAccount");
                double amt = ((Number) transfer.get("amount")).doubleValue();
                result.put("sourceAccount", sourceAcc);
                result.put("targetAccount", targetAcc);
                result.put("amount", amt);

                try {
                    barrier.await(10, TimeUnit.SECONDS);
                    concurrentTransferService.concurrentTransfer(sourceAcc, targetAcc, amt);
                    result.put("success", true);
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("error", e.getMessage());
                } finally {
                    results.add(result);
                    completionLatch.countDown();
                }
            });
            thread.start();
        }

        try {
            completionLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long elapsed = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("totalTransfers", n);
        response.put("executionTimeMs", elapsed);
        return ResponseEntity.ok(response);
    }

    // ---- 6. POST /queue/submit ----

    /**
     * Enqueue transaction to BlockingQueue.
     * Accepts {"type":"DEPOSIT","accountNumber":"ACC1","amount":100}.
     */
    @PostMapping("/queue/submit")
    public ResponseEntity<Map<String, Object>> queueSubmit(@RequestBody Map<String, Object> body) {
        String type = (String) body.get("type");
        String accountNumber = (String) body.get("accountNumber");
        double amount = ((Number) body.get("amount")).doubleValue();

        TransactionType txType = TransactionType.valueOf(type);
        Transaction tx = new Transaction(txType, amount, LocalDateTime.now(), accountNumber, null);

        transactionQueue.enqueue(tx);

        Map<String, Object> response = new HashMap<>();
        response.put("queued", true);
        response.put("queueSize", transactionQueue.getQueueSize());
        return ResponseEntity.ok(response);
    }

    // ---- 7. POST /fraud-scan ----

    /**
     * Fork/Join fraud detection.
     * Accepts {"thresholdAmount": 1000}.
     */
    @PostMapping("/fraud-scan")
    public ResponseEntity<FraudScanResult> fraudScan(@RequestBody Map<String, Object> body) {
        double thresholdAmount = ((Number) body.get("thresholdAmount")).doubleValue();
        FraudScanResult result = fraudDetectionService.scanForFraud(thresholdAmount);
        return ResponseEntity.ok(result);
    }

    // ---- 8. POST /audit ----

    /**
     * Phaser multi-phase audit. No body needed.
     */
    @PostMapping("/audit")
    public ResponseEntity<AuditReport> audit() {
        AuditReport report = auditEventBus.runAudit();
        return ResponseEntity.ok(report);
    }

    // ---- 9. POST /virtual-transfer ----

    /**
     * Virtual thread transfers.
     * Accepts {"transfers": [{"sourceAccount":"ACC1","targetAccount":"ACC2","amount":100}]}.
     */
    @PostMapping("/virtual-transfer")
    public ResponseEntity<Map<String, Object>> virtualTransfer(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> transfers = (List<Map<String, Object>>) body.get("transfers");
        Map<String, Object> result = virtualThreadTransferService.executeBatchTransfers(transfers);
        return ResponseEntity.ok(result);
    }

    // ---- 10. GET /rate-limiter/status ----

    /**
     * Returns the number of available semaphore permits.
     */
    @GetMapping("/rate-limiter/status")
    public ResponseEntity<Map<String, Object>> rateLimiterStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("availablePermits", rateLimiter.availablePermits());
        return ResponseEntity.ok(response);
    }

    // ---- 11. GET /deadlock/status ----

    /**
     * Deadlock detection result.
     */
    @GetMapping("/deadlock/status")
    public ResponseEntity<Map<String, Object>> deadlockStatus() {
        Map<String, Object> result = deadlockDetector.detectDeadlocks();
        return ResponseEntity.ok(result);
    }

    // ---- 12. GET /singletons ----

    /**
     * Returns identity hash codes of the three singleton instances.
     */
    @GetMapping("/singletons")
    public ResponseEntity<Map<String, Object>> singletons() {
        Map<String, Object> response = new HashMap<>();
        response.put("ThreadSafeAccountCache", System.identityHashCode(ThreadSafeAccountCache.getInstance()));
        response.put("BankingConfiguration", System.identityHashCode(BankingConfiguration.INSTANCE));
        response.put("AuditLogger", System.identityHashCode(AuditLogger.getInstance()));
        return ResponseEntity.ok(response);
    }

    // ---- 13. GET /thread-info ----

    /**
     * Returns info about active threads with "banking" in the name.
     */
    @GetMapping("/thread-info")
    public ResponseEntity<List<Map<String, Object>>> threadInfo() {
        List<Map<String, Object>> threadInfoList = new ArrayList<>();

        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread t : threads) {
            if (t.getName().toLowerCase().contains("banking")) {
                Map<String, Object> info = new HashMap<>();
                info.put("name", t.getName());
                info.put("state", t.getState().toString());
                info.put("daemon", t.isDaemon());
                threadInfoList.add(info);
            }
        }

        return ResponseEntity.ok(threadInfoList);
    }

    // ---- 14. GET /stamped-balance/{accountNumber} ----

    /**
     * Optimistic read balance via StampedLock.
     */
    @GetMapping("/stamped-balance/{accountNumber}")
    public ResponseEntity<Map<String, Object>> stampedBalance(@PathVariable String accountNumber) {
        double balance = stampedBalanceLock.optimisticReadBalance(accountNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("accountNumber", accountNumber);
        response.put("balance", balance);
        response.put("readType", "optimistic");
        return ResponseEntity.ok(response);
    }
}

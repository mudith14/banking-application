package com.example.bankingdemo.concurrency.phaser;

import com.example.bankingdemo.concurrency.model.AuditReport;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Phaser-based three-phase audit coordinator.
 * <p>
 * Phase 0: Collect account snapshots (each worker reads its account balance)
 * Phase 1: Validate balances (each worker checks balance >= 0)
 * Phase 2: Generate report (coordinator aggregates results)
 * <p>
 * Supports dynamic registration/deregistration via register()/arriveAndDeregister().
 */
@Component
public class AuditEventBus {

    private static final Logger log = LoggerFactory.getLogger(AuditEventBus.class);

    private final AccountRepository accountRepository;

    public AuditEventBus(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Run a three-phase audit across all accounts in the repository.
     *
     * @return an AuditReport with snapshots, validation result, summary, and timing
     */
    public AuditReport runAudit() {
        long startTime = System.currentTimeMillis();

        List<Account> accounts = accountRepository.findAll();

        // Shared state for collecting results across phases
        Map<String, Double> snapshots = new ConcurrentHashMap<>();
        AtomicBoolean allValid = new AtomicBoolean(true);

        // Create Phaser with 1 party (the coordinator)
        Phaser phaser = new Phaser(1) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                switch (phase) {
                    case 0 -> log.info("Phase 0 complete: Account snapshots collected. Parties: {}", registeredParties);
                    case 1 -> log.info("Phase 1 complete: Balance validation done. Parties: {}", registeredParties);
                    case 2 -> log.info("Phase 2 complete: Report generation done. Parties: {}", registeredParties);
                    default -> log.info("Phase {} complete. Parties: {}", phase, registeredParties);
                }
                // Terminate after phase 2
                return phase >= 2;
            }
        };

        // For each account, register a worker thread that participates in all 3 phases
        for (Account account : accounts) {
            phaser.register(); // dynamic registration
            Thread worker = new Thread(() -> {
                try {
                    // Phase 0: Collect snapshot
                    double balance = account.getBalance();
                    snapshots.put(account.getAccountNumber(), balance);
                    phaser.arriveAndAwaitAdvance();

                    // Phase 1: Validate balance
                    if (balance < 0) {
                        allValid.set(false);
                    }
                    phaser.arriveAndAwaitAdvance();

                    // Phase 2: Arrive and deregister (coordinator aggregates)
                    phaser.arriveAndDeregister();
                } catch (Exception e) {
                    log.error("Audit worker error for account {}: {}",
                            account.getAccountNumber(), e.getMessage());
                    // Deregister on failure so phaser doesn't hang
                    phaser.arriveAndDeregister();
                }
            }, "audit-worker-" + account.getAccountNumber());
            worker.setDaemon(true);
            worker.start();
        }

        // Coordinator waits for Phase 0 (snapshot collection)
        phaser.arriveAndAwaitAdvance();

        // Coordinator waits for Phase 1 (validation)
        phaser.arriveAndAwaitAdvance();

        // Phase 2: Coordinator generates report, then deregisters to terminate phaser
        long auditTimeMs = System.currentTimeMillis() - startTime;
        boolean balancesValid = allValid.get();

        String summary = String.format("Audit complete: %d accounts audited, balances %s, time %dms",
                snapshots.size(),
                balancesValid ? "all valid" : "some invalid",
                auditTimeMs);

        phaser.arriveAndDeregister();

        log.info(summary);

        return new AuditReport(
                Map.copyOf(snapshots),
                balancesValid,
                summary,
                auditTimeMs
        );
    }
}

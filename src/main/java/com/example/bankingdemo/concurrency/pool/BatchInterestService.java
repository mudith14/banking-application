package com.example.bankingdemo.concurrency.pool;

import com.example.bankingdemo.concurrency.BankingThreadFactory;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.repository.AccountRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Calculates interest for multiple accounts in parallel using a fixed thread pool.
 * Uses CountDownLatch to wait for all calculations and Future to collect results.
 */
@Service
public class BatchInterestService {

    private static final Logger logger = LoggerFactory.getLogger(BatchInterestService.class);

    private final AccountRepository accountRepository;
    private final ExecutorService executorService;

    public BatchInterestService(AccountRepository accountRepository,
                                @Value("${banking.pool.size:4}") int poolSize) {
        this.accountRepository = accountRepository;
        this.executorService = Executors.newFixedThreadPool(poolSize,
                new BankingThreadFactory("interest-pool-"));
    }

    /**
     * Calculate interest for the given account numbers in parallel.
     * Uses a CountDownLatch to wait for all calculations to complete,
     * then aggregates the results from Future objects.
     *
     * @param accountNumbers list of account numbers to calculate interest for
     * @return total aggregated interest across all accounts
     */
    public double calculateBatchInterest(List<String> accountNumbers) {
        CountDownLatch latch = new CountDownLatch(accountNumbers.size());
        List<Future<Double>> futures = new ArrayList<>();

        for (String accountNumber : accountNumbers) {
            Callable<Double> task = () -> {
                try {
                    return accountRepository.findByAccountNumber(accountNumber)
                            .map(Account::calculateInterest)
                            .orElse(0.0);
                } finally {
                    latch.countDown();
                }
            };
            futures.add(executorService.submit(task));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while waiting for batch interest calculations", e);
        }

        double totalInterest = 0.0;
        for (Future<Double> future : futures) {
            try {
                totalInterest += future.get();
            } catch (ExecutionException e) {
                logger.error("Error calculating interest for an account: {}", e.getCause().getMessage(), e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while collecting interest result", e);
            }
        }
        return totalInterest;
    }

    /**
     * Calculate interest for all accounts in the repository.
     *
     * @return total aggregated interest across all accounts
     */
    public double calculateAllAccountsInterest() {
        List<String> accountNumbers = accountRepository.findAll().stream()
                .map(Account::getAccountNumber)
                .toList();
        return calculateBatchInterest(accountNumbers);
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down BatchInterestService executor...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in 10s, forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}

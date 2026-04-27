package com.example.bankingdemo.concurrency.forkjoin;

import com.example.bankingdemo.concurrency.model.FraudScanResult;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.service.TransactionLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Fraud detection service using Fork/Join framework.
 * Splits the transaction list into sub-tasks for parallel scanning.
 */
@Service
public class FraudDetectionService {

    private static final int DEFAULT_SPLIT_THRESHOLD = 10;

    private final TransactionLog transactionLog;
    private final ForkJoinPool forkJoinPool;

    public FraudDetectionService(TransactionLog transactionLog) {
        this.transactionLog = transactionLog;
        this.forkJoinPool = new ForkJoinPool();
    }

    /**
     * Scan all transactions for fraud, flagging those whose amount exceeds the threshold.
     *
     * @param thresholdAmount transactions with amount greater than this are flagged
     * @return a FraudScanResult with flagged transactions, total scanned, and scan time
     */
    public FraudScanResult scanForFraud(double thresholdAmount) {
        long startTime = System.currentTimeMillis();
        List<Transaction> allTransactions = transactionLog.getAllTransactions();

        List<Transaction> flagged;
        if (allTransactions.isEmpty()) {
            flagged = List.of();
        } else {
            FraudScanTask task = new FraudScanTask(allTransactions, thresholdAmount, 0, allTransactions.size());
            flagged = forkJoinPool.invoke(task);
        }

        long scanTimeMs = System.currentTimeMillis() - startTime;
        return new FraudScanResult(flagged, allTransactions.size(), scanTimeMs);
    }

    /**
     * RecursiveTask that splits the transaction list when its size exceeds the threshold,
     * then merges results from sub-tasks.
     */
    static class FraudScanTask extends RecursiveTask<List<Transaction>> {

        private final List<Transaction> transactions;
        private final double thresholdAmount;
        private final int start;
        private final int end;

        FraudScanTask(List<Transaction> transactions, double thresholdAmount, int start, int end) {
            this.transactions = transactions;
            this.thresholdAmount = thresholdAmount;
            this.start = start;
            this.end = end;
        }

        @Override
        protected List<Transaction> compute() {
            int size = end - start;
            if (size <= DEFAULT_SPLIT_THRESHOLD) {
                return scanSequentially();
            }

            int mid = start + size / 2;
            FraudScanTask leftTask = new FraudScanTask(transactions, thresholdAmount, start, mid);
            FraudScanTask rightTask = new FraudScanTask(transactions, thresholdAmount, mid, end);

            leftTask.fork();
            List<Transaction> rightResult = rightTask.compute();
            List<Transaction> leftResult = leftTask.join();

            List<Transaction> merged = new ArrayList<>(leftResult.size() + rightResult.size());
            merged.addAll(leftResult);
            merged.addAll(rightResult);
            return merged;
        }

        private List<Transaction> scanSequentially() {
            List<Transaction> flagged = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Transaction t = transactions.get(i);
                if (t.getAmount() > thresholdAmount) {
                    flagged.add(t);
                }
            }
            return flagged;
        }
    }
}

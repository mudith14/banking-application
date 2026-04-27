package com.example.bankingdemo.concurrency.model;

import com.example.bankingdemo.model.Transaction;

import java.util.List;

/**
 * Result of a parallel fraud detection scan.
 *
 * @param flaggedTransactions transactions flagged as suspicious
 * @param totalScanned        total number of transactions scanned
 * @param scanTimeMs          time taken to complete the scan in milliseconds
 */
public record FraudScanResult(
        List<Transaction> flaggedTransactions,
        int totalScanned,
        long scanTimeMs
) {}

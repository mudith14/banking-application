package com.example.bankingdemo.concurrency.model;

/**
 * Result of an asynchronous transaction processing operation.
 *
 * @param transactionId    unique identifier for the transaction
 * @param success          whether the transaction completed successfully
 * @param message          descriptive message about the outcome
 * @param processingTimeMs time taken to process in milliseconds
 */
public record TransactionResult(
        String transactionId,
        boolean success,
        String message,
        long processingTimeMs
) {}

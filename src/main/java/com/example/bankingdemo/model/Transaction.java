package com.example.bankingdemo.model;

import java.time.LocalDateTime;

/**
 * Represents a financial transaction (deposit, withdrawal, or transfer).
 * Encapsulates the transaction type, amount, timestamp, and involved accounts.
 */
public class Transaction {

    private final TransactionType transactionType;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String sourceAccountNumber;
    private final String targetAccountNumber;

    /**
     * Create a new Transaction.
     *
     * @param transactionType      the type of transaction
     * @param amount               the transaction amount
     * @param timestamp            when the transaction occurred
     * @param sourceAccountNumber  the account initiating the transaction
     * @param targetAccountNumber  the destination account (null for non-transfer transactions)
     */
    public Transaction(TransactionType transactionType, double amount,
                       LocalDateTime timestamp, String sourceAccountNumber,
                       String targetAccountNumber) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.timestamp = timestamp;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    @Override
    public String toString() {
        if (targetAccountNumber != null) {
            return String.format("Transaction[type=%s, amount=%.2f, timestamp=%s, source=%s, target=%s]",
                    transactionType, amount, timestamp, sourceAccountNumber, targetAccountNumber);
        }
        return String.format("Transaction[type=%s, amount=%.2f, timestamp=%s, account=%s]",
                transactionType, amount, timestamp, sourceAccountNumber);
    }
}

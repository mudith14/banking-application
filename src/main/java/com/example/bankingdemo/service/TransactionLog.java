package com.example.bankingdemo.service;

import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton Spring component that stores and queries transaction records.
 * Uses a CopyOnWriteArrayList for thread-safe ordered storage.
 */
@Component
public class TransactionLog {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    /**
     * Record a transaction.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Get all transactions where the given account number appears as source or target.
     *
     * @param accountNumber the account number to filter by
     * @return matching transactions in insertion order
     */
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return transactions.stream()
                .filter(t -> accountNumber.equals(t.getSourceAccountNumber())
                        || accountNumber.equals(t.getTargetAccountNumber()))
                .toList();
    }

    /**
     * Get all transactions whose timestamp falls within the given range (inclusive).
     *
     * @param from the start of the range
     * @param to   the end of the range
     * @return matching transactions in insertion order
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactions.stream()
                .filter(t -> !t.getTimestamp().isBefore(from) && !t.getTimestamp().isAfter(to))
                .toList();
    }

    /**
     * Get all transactions of a specific type.
     *
     * @param type the transaction type to filter by
     * @return matching transactions in insertion order
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getTransactionType() == type)
                .toList();
    }

    /**
     * Get all recorded transactions in insertion order.
     *
     * @return all transactions
     */
    public List<Transaction> getAllTransactions() {
        return List.copyOf(transactions);
    }
}

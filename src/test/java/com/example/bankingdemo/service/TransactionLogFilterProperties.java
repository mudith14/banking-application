package com.example.bankingdemo.service;

import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: TransactionLog filtering correctness.
 * Feature: banking-oops-demo, Property 14: TransactionLog filtering correctness
 *
 * Validates: Requirements 6.3
 */
class TransactionLogFilterProperties {

    /**
     * Property 14: For any set of transactions in the log and any filter
     * (by account number, date range, or transaction type), every returned
     * transaction shall match the filter criteria, and no matching transaction
     * shall be omitted.
     *
     * Validates: Requirements 6.3
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 14: TransactionLog filtering correctness")
    void filteringShouldReturnExactlyMatchingTransactions(
            @ForAll("transactionTypeArb") TransactionType type,
            @ForAll("accountNumberArb") String filterAccount) {

        TransactionLog log = new TransactionLog();
        LocalDateTime baseTime = LocalDateTime.of(2024, 6, 15, 10, 0);

        // Add a mix of transactions with different accounts, types, and times
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 100.0,
                baseTime, "ACC-A", null));
        log.addTransaction(new Transaction(TransactionType.WITHDRAWAL, 50.0,
                baseTime.plusHours(1), "ACC-B", null));
        log.addTransaction(new Transaction(TransactionType.TRANSFER, 200.0,
                baseTime.plusHours(2), "ACC-A", "ACC-B"));
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 300.0,
                baseTime.plusDays(1), "ACC-B", null));
        log.addTransaction(new Transaction(TransactionType.WITHDRAWAL, 75.0,
                baseTime.plusDays(2), "ACC-A", null));

        List<Transaction> all = log.getAllTransactions();

        // Filter by type: every returned tx matches, no matching tx omitted
        List<Transaction> byType = log.getTransactionsByType(type);
        for (Transaction t : byType) {
            assertThat(t.getTransactionType()).isEqualTo(type);
        }
        long expectedTypeCount = all.stream()
                .filter(t -> t.getTransactionType() == type).count();
        assertThat(byType).hasSize((int) expectedTypeCount);

        // Filter by account number: every returned tx matches, no matching tx omitted
        List<Transaction> byAccount = log.getTransactionsByAccountNumber(filterAccount);
        for (Transaction t : byAccount) {
            assertThat(filterAccount.equals(t.getSourceAccountNumber())
                    || filterAccount.equals(t.getTargetAccountNumber())).isTrue();
        }
        long expectedAccountCount = all.stream()
                .filter(t -> filterAccount.equals(t.getSourceAccountNumber())
                        || filterAccount.equals(t.getTargetAccountNumber()))
                .count();
        assertThat(byAccount).hasSize((int) expectedAccountCount);

        // Filter by date range: every returned tx matches, no matching tx omitted
        LocalDateTime from = baseTime;
        LocalDateTime to = baseTime.plusHours(2);
        List<Transaction> byDate = log.getTransactionsByDateRange(from, to);
        for (Transaction t : byDate) {
            assertThat(t.getTimestamp()).isAfterOrEqualTo(from);
            assertThat(t.getTimestamp()).isBeforeOrEqualTo(to);
        }
        long expectedDateCount = all.stream()
                .filter(t -> !t.getTimestamp().isBefore(from) && !t.getTimestamp().isAfter(to))
                .count();
        assertThat(byDate).hasSize((int) expectedDateCount);
    }

    @Provide
    Arbitrary<TransactionType> transactionTypeArb() {
        return Arbitraries.of(TransactionType.values());
    }

    @Provide
    Arbitrary<String> accountNumberArb() {
        return Arbitraries.of("ACC-A", "ACC-B", "ACC-NONEXISTENT");
    }
}

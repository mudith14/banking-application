package com.example.bankingdemo.model;

import com.example.bankingdemo.service.TransactionLog;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: generateStatement contains transaction details.
 * Feature: banking-oops-demo, Property 15: generateStatement contains transaction details
 *
 * Validates: Requirements 6.4
 */
class GenerateStatementProperties {

    /**
     * Property 15: For any Reportable account with N transactions,
     * generateStatement() shall return a string containing the type, amount,
     * and timestamp of each transaction.
     *
     * Validates: Requirements 6.4
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 15: generateStatement contains transaction details")
    void statementShouldContainAllTransactionDetails(
            @ForAll @DoubleRange(min = 1000.0, max = 50000.0) double initialBalance,
            @ForAll @IntRange(min = 1, max = 5) int operationCount) {

        TransactionLog log = new TransactionLog();
        CurrentAccount account = new CurrentAccount("Owner", initialBalance, 0);
        account.setTransactionLog(log);

        // Perform a series of deposits and withdrawals
        for (int i = 0; i < operationCount; i++) {
            account.deposit(10.0 + i);
        }

        String statement = account.generateStatement();

        // Verify statement contains details for each transaction
        List<Transaction> transactions = log.getTransactionsByAccountNumber(
                account.getAccountNumber());
        assertThat(transactions).hasSize(operationCount);

        for (Transaction tx : transactions) {
            assertThat(statement).contains(tx.getTransactionType().name());
            assertThat(statement).contains(String.format("%.2f", tx.getAmount()));
            assertThat(statement).contains(tx.getTimestamp().toString());
        }
    }
}

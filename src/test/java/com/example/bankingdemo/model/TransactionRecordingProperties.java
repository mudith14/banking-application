package com.example.bankingdemo.model;

import com.example.bankingdemo.service.TransactionLog;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: operations create Transaction records.
 * Feature: banking-oops-demo, Property 13: Operations create Transaction records
 *
 * Validates: Requirements 6.2
 */
class TransactionRecordingProperties {

    /**
     * Property 13: For any Account and any successful deposit or withdrawal,
     * a corresponding Transaction object with the correct type, amount, and
     * account number shall be added to the TransactionLog.
     *
     * Validates: Requirements 6.2
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 13: Operations create Transaction records")
    void successfulOperationsShouldCreateTransactionRecords(
            @ForAll @DoubleRange(min = 100.0, max = 10000.0) double initialBalance,
            @ForAll @DoubleRange(min = 0.01, max = 50.0) double depositAmount,
            @ForAll @DoubleRange(min = 0.01, max = 50.0) double withdrawAmount) {

        TransactionLog log = new TransactionLog();
        CurrentAccount account = new CurrentAccount("Owner", initialBalance, 0);
        account.setTransactionLog(log);

        // Perform deposit
        account.deposit(depositAmount);

        List<Transaction> afterDeposit = log.getAllTransactions();
        assertThat(afterDeposit).hasSize(1);
        Transaction depositTx = afterDeposit.get(0);
        assertThat(depositTx.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(depositTx.getAmount()).isCloseTo(depositAmount,
                org.assertj.core.data.Offset.offset(0.001));
        assertThat(depositTx.getSourceAccountNumber()).isEqualTo(account.getAccountNumber());

        // Perform withdrawal
        account.withdraw(withdrawAmount);

        List<Transaction> afterWithdraw = log.getAllTransactions();
        assertThat(afterWithdraw).hasSize(2);
        Transaction withdrawTx = afterWithdraw.get(1);
        assertThat(withdrawTx.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(withdrawTx.getAmount()).isCloseTo(withdrawAmount,
                org.assertj.core.data.Offset.offset(0.001));
        assertThat(withdrawTx.getSourceAccountNumber()).isEqualTo(account.getAccountNumber());
    }
}

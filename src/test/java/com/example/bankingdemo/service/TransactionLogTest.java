package com.example.bankingdemo.service;

import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionLog component.
 */
class TransactionLogTest {

    private TransactionLog log;

    @BeforeEach
    void setUp() {
        log = new TransactionLog();
    }

    @Test
    void getAllTransactionsShouldReturnEmptyListInitially() {
        assertTrue(log.getAllTransactions().isEmpty());
    }

    @Test
    void addTransactionShouldStoreTransaction() {
        Transaction tx = new Transaction(TransactionType.DEPOSIT, 100.0,
                LocalDateTime.now(), "ACC1", null);
        log.addTransaction(tx);

        List<Transaction> all = log.getAllTransactions();
        assertEquals(1, all.size());
        assertEquals(tx, all.get(0));
    }

    @Test
    void getTransactionsByAccountNumberShouldMatchSourceOrTarget() {
        LocalDateTime now = LocalDateTime.now();
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 100.0, now, "ACC1", null));
        log.addTransaction(new Transaction(TransactionType.TRANSFER, 50.0, now, "ACC2", "ACC1"));
        log.addTransaction(new Transaction(TransactionType.WITHDRAWAL, 30.0, now, "ACC3", null));

        List<Transaction> acc1Txns = log.getTransactionsByAccountNumber("ACC1");
        assertEquals(2, acc1Txns.size());

        List<Transaction> acc3Txns = log.getTransactionsByAccountNumber("ACC3");
        assertEquals(1, acc3Txns.size());
        assertEquals(TransactionType.WITHDRAWAL, acc3Txns.get(0).getTransactionType());
    }

    @Test
    void getTransactionsByDateRangeShouldFilterInclusive() {
        LocalDateTime day1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime day2 = LocalDateTime.of(2025, 1, 2, 10, 0);
        LocalDateTime day3 = LocalDateTime.of(2025, 1, 3, 10, 0);

        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 100.0, day1, "ACC1", null));
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 200.0, day2, "ACC1", null));
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 300.0, day3, "ACC1", null));

        List<Transaction> range = log.getTransactionsByDateRange(day1, day2);
        assertEquals(2, range.size());
    }

    @Test
    void getTransactionsByTypeShouldFilterCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 100.0, now, "ACC1", null));
        log.addTransaction(new Transaction(TransactionType.WITHDRAWAL, 50.0, now, "ACC1", null));
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 200.0, now, "ACC2", null));

        List<Transaction> deposits = log.getTransactionsByType(TransactionType.DEPOSIT);
        assertEquals(2, deposits.size());

        List<Transaction> withdrawals = log.getTransactionsByType(TransactionType.WITHDRAWAL);
        assertEquals(1, withdrawals.size());

        List<Transaction> transfers = log.getTransactionsByType(TransactionType.TRANSFER);
        assertTrue(transfers.isEmpty());
    }

    @Test
    void getAllTransactionsShouldReturnDefensiveCopy() {
        LocalDateTime now = LocalDateTime.now();
        log.addTransaction(new Transaction(TransactionType.DEPOSIT, 100.0, now, "ACC1", null));

        List<Transaction> all = log.getAllTransactions();
        assertThrows(UnsupportedOperationException.class, () ->
                all.add(new Transaction(TransactionType.DEPOSIT, 50.0, now, "ACC2", null)));
    }
}

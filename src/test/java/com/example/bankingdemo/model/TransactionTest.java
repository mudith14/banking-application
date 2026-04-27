package com.example.bankingdemo.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionType enum and Transaction class.
 */
class TransactionTest {

    @Test
    void transactionTypeShouldHaveThreeValues() {
        TransactionType[] values = TransactionType.values();
        assertEquals(3, values.length);
        assertEquals(TransactionType.DEPOSIT, TransactionType.valueOf("DEPOSIT"));
        assertEquals(TransactionType.WITHDRAWAL, TransactionType.valueOf("WITHDRAWAL"));
        assertEquals(TransactionType.TRANSFER, TransactionType.valueOf("TRANSFER"));
    }

    @Test
    void transactionShouldStoreAllFields() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 30);
        Transaction tx = new Transaction(TransactionType.DEPOSIT, 500.0, now, "ACC1", null);

        assertEquals(TransactionType.DEPOSIT, tx.getTransactionType());
        assertEquals(500.0, tx.getAmount());
        assertEquals(now, tx.getTimestamp());
        assertEquals("ACC1", tx.getSourceAccountNumber());
        assertNull(tx.getTargetAccountNumber());
    }

    @Test
    void transferTransactionShouldHaveTargetAccount() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 30);
        Transaction tx = new Transaction(TransactionType.TRANSFER, 200.0, now, "ACC1", "ACC2");

        assertEquals(TransactionType.TRANSFER, tx.getTransactionType());
        assertEquals("ACC1", tx.getSourceAccountNumber());
        assertEquals("ACC2", tx.getTargetAccountNumber());
    }

    @Test
    void toStringShouldContainTypeAmountTimestampAndAccount() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 30);
        Transaction tx = new Transaction(TransactionType.WITHDRAWAL, 100.0, now, "ACC5", null);

        String str = tx.toString();
        assertTrue(str.contains("WITHDRAWAL"));
        assertTrue(str.contains("100.00"));
        assertTrue(str.contains("2025-01-15"));
        assertTrue(str.contains("ACC5"));
    }

    @Test
    void toStringForTransferShouldContainBothAccounts() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 14, 0);
        Transaction tx = new Transaction(TransactionType.TRANSFER, 300.0, now, "ACC1", "ACC2");

        String str = tx.toString();
        assertTrue(str.contains("TRANSFER"));
        assertTrue(str.contains("ACC1"));
        assertTrue(str.contains("ACC2"));
    }
}

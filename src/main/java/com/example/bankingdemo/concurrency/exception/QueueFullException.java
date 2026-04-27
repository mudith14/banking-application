package com.example.bankingdemo.concurrency.exception;

import com.example.bankingdemo.exception.BankingException;

/**
 * Thrown when the TransactionQueue is at capacity and cannot accept more transactions.
 */
public class QueueFullException extends BankingException {

    public QueueFullException(String message) {
        super(message);
    }

    public QueueFullException(String message, Throwable cause) {
        super(message, cause);
    }
}

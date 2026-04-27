package com.example.bankingdemo.concurrency.exception;

import com.example.bankingdemo.exception.BankingException;

/**
 * Thrown when ThreadMXBean reports deadlocked threads during a banking operation.
 */
public class DeadlockDetectedException extends BankingException {

    public DeadlockDetectedException(String message) {
        super(message);
    }

    public DeadlockDetectedException(String message, Throwable cause) {
        super(message, cause);
    }
}

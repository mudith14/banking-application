package com.example.bankingdemo.concurrency.exception;

import com.example.bankingdemo.exception.BankingException;

/**
 * Thrown when a banking thread is interrupted during an operation.
 */
public class ThreadInterruptedException extends BankingException {

    public ThreadInterruptedException(String message) {
        super(message);
    }

    public ThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}

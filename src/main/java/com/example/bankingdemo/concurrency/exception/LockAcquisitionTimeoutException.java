package com.example.bankingdemo.concurrency.exception;

import com.example.bankingdemo.exception.BankingException;

/**
 * Thrown when a lock cannot be acquired within the configured timeout period.
 */
public class LockAcquisitionTimeoutException extends BankingException {

    public LockAcquisitionTimeoutException(String message) {
        super(message);
    }

    public LockAcquisitionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

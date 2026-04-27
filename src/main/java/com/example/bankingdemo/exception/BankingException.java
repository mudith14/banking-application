package com.example.bankingdemo.exception;

/**
 * Base exception for all banking-related errors.
 * All custom banking exceptions extend this class.
 */
public class BankingException extends RuntimeException {

    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}

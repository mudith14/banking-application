package com.example.bankingdemo.exception;

/**
 * Thrown when a withdrawal amount exceeds the available balance.
 */
public class InsufficientFundsException extends BankingException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}

package com.example.bankingdemo.exception;

/**
 * Thrown when a deposit or withdrawal amount is zero or negative.
 */
public class InvalidAmountException extends BankingException {

    public InvalidAmountException(String message) {
        super(message);
    }
}

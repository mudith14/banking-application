package com.example.bankingdemo.exception;

/**
 * Thrown when a withdrawal exceeds the overdraft limit of a CurrentAccount.
 */
public class OverdraftLimitExceededException extends BankingException {

    public OverdraftLimitExceededException(String message) {
        super(message);
    }
}

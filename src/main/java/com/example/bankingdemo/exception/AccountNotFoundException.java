package com.example.bankingdemo.exception;

/**
 * Thrown when an account cannot be found by its account number.
 */
public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }
}

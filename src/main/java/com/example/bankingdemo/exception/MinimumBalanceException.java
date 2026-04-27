package com.example.bankingdemo.exception;

/**
 * Thrown when a withdrawal would breach the minimum balance constraint
 * of a SavingsAccount.
 */
public class MinimumBalanceException extends BankingException {

    public MinimumBalanceException(String message) {
        super(message);
    }
}

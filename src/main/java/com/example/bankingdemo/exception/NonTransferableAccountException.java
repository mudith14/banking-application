package com.example.bankingdemo.exception;

/**
 * Thrown when a transfer is attempted from an account that does not support transfers.
 */
public class NonTransferableAccountException extends BankingException {

    public NonTransferableAccountException(String accountNumber) {
        super("Account " + accountNumber + " does not support transfers");
    }
}

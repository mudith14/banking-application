package com.example.bankingdemo.model;

/**
 * Contract for accounts that can generate a transaction statement.
 */
public interface Reportable {

    /**
     * Generate a formatted statement of transactions for this account.
     *
     * @return a human-readable transaction summary string
     */
    String generateStatement();
}

package com.example.bankingdemo.concurrency.model;

/**
 * Represents the status of a bank account.
 * Used with AtomicReference to demonstrate atomic enum transitions.
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}

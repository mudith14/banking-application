package com.example.bankingdemo.model;

import com.example.bankingdemo.exception.InsufficientFundsException;
import com.example.bankingdemo.exception.InvalidAmountException;
import com.example.bankingdemo.service.TransactionLog;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base class for all bank accounts.
 * Demonstrates abstraction, encapsulation, and method overriding.
 */
public abstract class Account {

    private static final AtomicLong ACCOUNT_NUMBER_GENERATOR = new AtomicLong(0);

    protected String accountNumber;
    private double balance;
    protected String ownerName;
    protected LocalDateTime createdAt;
    private TransactionLog transactionLog;

    /**
     * Construct a new Account with the given owner name and initial balance.
     *
     * @param ownerName      the name of the account owner
     * @param initialBalance the starting balance (must be non-negative)
     */
    protected Account(String ownerName, double initialBalance) {
        this.accountNumber = "ACC" + ACCOUNT_NUMBER_GENERATOR.incrementAndGet();
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the transaction log for this account.
     * Since Account is a POJO (not a Spring bean), the log must be set after construction.
     *
     * @param transactionLog the transaction log to use, or null to disable logging
     */
    public void setTransactionLog(TransactionLog transactionLog) {
        this.transactionLog = transactionLog;
    }

    /**
     * Get the transaction log associated with this account.
     *
     * @return the transaction log, or null if not set
     */
    public TransactionLog getTransactionLog() {
        return transactionLog;
    }

    /**
     * Deposit funds into this account.
     *
     * @param amount the amount to deposit (must be positive)
     * @throws InvalidAmountException if amount is zero or negative
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        this.balance += amount;
        if (transactionLog != null) {
            transactionLog.addTransaction(new Transaction(
                    TransactionType.DEPOSIT, amount, LocalDateTime.now(),
                    accountNumber, null));
        }
    }

    /**
     * Withdraw funds from this account.
     *
     * @param amount the amount to withdraw (must be positive and not exceed balance)
     * @throws InvalidAmountException      if amount is zero or negative
     * @throws InsufficientFundsException  if amount exceeds available balance
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        if (amount > balance) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds: available %.2f", balance));
        }
        this.balance -= amount;
        if (transactionLog != null) {
            transactionLog.addTransaction(new Transaction(
                    TransactionType.WITHDRAWAL, amount, LocalDateTime.now(),
                    accountNumber, null));
        }
    }

    /**
     * Withdraw funds from this account with a reason (compile-time polymorphism).
     * Produces the same balance effect as {@link #withdraw(double)}.
     *
     * @param amount the amount to withdraw
     * @param reason a description of why the withdrawal is being made
     * @throws InvalidAmountException      if amount is zero or negative
     * @throws InsufficientFundsException  if amount exceeds available balance
     */
    public void withdraw(double amount, String reason) {
        withdraw(amount);
    }

    /**
     * Set the balance directly. Intended for subclasses that need custom
     * withdrawal logic (e.g., overdraft support in CurrentAccount).
     *
     * @param balance the new balance value
     */
    protected void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Calculate interest for this account. Each subclass provides its own implementation.
     *
     * @return the calculated interest amount
     */
    public abstract double calculateInterest();

    @Override
    public String toString() {
        return String.format("%s[accountNumber=%s, ownerName=%s, balance=%.2f]",
                getClass().getSimpleName(), accountNumber, ownerName, balance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }
}

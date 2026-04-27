package com.example.bankingdemo.model;

import com.example.bankingdemo.exception.InvalidAmountException;
import com.example.bankingdemo.exception.OverdraftLimitExceededException;

import java.time.LocalDateTime;

/**
 * Current account with overdraft facility.
 * Allows withdrawals that exceed the balance up to a configured overdraft limit.
 */
public class CurrentAccount extends Account implements Transferable, Reportable {

    private final double overdraftLimit;

    /**
     * Create a new CurrentAccount.
     *
     * @param ownerName      the account owner's name
     * @param initialBalance the starting balance
     * @param overdraftLimit the maximum amount the balance can go negative
     */
    public CurrentAccount(String ownerName, double initialBalance, double overdraftLimit) {
        super(ownerName, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    /**
     * Withdraw funds, allowing overdraft up to the configured limit.
     * Does NOT call super.withdraw() because the base class rejects
     * amounts exceeding the balance.
     */
    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }
        if (amount > getBalance() + overdraftLimit) {
            throw new OverdraftLimitExceededException(
                    String.format("Withdrawal exceeds overdraft limit of %.2f", overdraftLimit));
        }
        setBalance(getBalance() - amount);
        if (getTransactionLog() != null) {
            getTransactionLog().addTransaction(new Transaction(
                    TransactionType.WITHDRAWAL, amount, LocalDateTime.now(),
                    getAccountNumber(), null));
        }
    }

    /**
     * Current accounts earn zero interest.
     */
    @Override
    public double calculateInterest() {
        return 0;
    }

    @Override
    public void transferTo(Account target, double amount) {
        this.withdraw(amount);
        target.deposit(amount);
    }

    @Override
    public String generateStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Statement for CurrentAccount %s | Owner: %s | Balance: %.2f",
                accountNumber, ownerName, getBalance()));
        if (getTransactionLog() != null) {
            var transactions = getTransactionLog().getTransactionsByAccountNumber(accountNumber);
            for (Transaction t : transactions) {
                sb.append("\n").append(t.toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("CurrentAccount[accountNumber=%s, ownerName=%s, balance=%.2f, overdraftLimit=%.2f]",
                accountNumber, ownerName, getBalance(), overdraftLimit);
    }
}

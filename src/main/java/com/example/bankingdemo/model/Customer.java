package com.example.bankingdemo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank customer who owns one or more accounts.
 * Demonstrates composition — the "has-a" relationship between Customer and Account.
 */
public class Customer {

    private final String name;
    private final List<Account> accounts;

    /**
     * Create a new Customer with the given name and no accounts.
     *
     * @param name the customer's name
     */
    public Customer(String name) {
        this.name = name;
        this.accounts = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    /**
     * Add an account to this customer.
     *
     * @param account the account to add
     */
    public void addAccount(Account account) {
        accounts.add(account);
    }

    /**
     * Remove an account from this customer.
     *
     * @param account the account to remove
     */
    public void removeAccount(Account account) {
        accounts.remove(account);
    }

    /**
     * Get all accounts owned by this customer.
     * Returns a defensive copy to preserve encapsulation.
     *
     * @return a copy of the accounts list
     */
    public List<Account> getAccounts() {
        return List.copyOf(accounts);
    }

    /**
     * Calculate the total balance across all owned accounts.
     *
     * @return the sum of all account balances
     */
    public double getTotalBalance() {
        return accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
}

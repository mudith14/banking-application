package com.example.bankingdemo.model;

/**
 * Contract for accounts that support fund transfers.
 * Not all account types implement this — for example,
 * FixedDepositAccount does not support direct transfers.
 */
public interface Transferable {

    /**
     * Transfer funds from this account to the target account.
     *
     * @param target the destination account
     * @param amount the amount to transfer (must be positive)
     */
    void transferTo(Account target, double amount);
}

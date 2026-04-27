package com.example.bankingdemo.concurrency.atomic;

import com.example.bankingdemo.concurrency.model.AccountStatus;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock-free account balance operations using AtomicLong (cents) and AtomicReference (status).
 * Demonstrates compare-and-swap based concurrency for banking operations.
 */
public class AccountBalanceAtomic {

    private final AtomicLong balanceCents;
    private final AtomicReference<AccountStatus> status;
    private final String accountNumber;

    public AccountBalanceAtomic(String accountNumber, long initialBalanceCents) {
        this.accountNumber = accountNumber;
        this.balanceCents = new AtomicLong(initialBalanceCents);
        this.status = new AtomicReference<>(AccountStatus.ACTIVE);
    }

    /**
     * Atomically deposits the given amount using a compareAndSet loop.
     *
     * @param amountCents the amount in cents to deposit
     */
    public void atomicDeposit(long amountCents) {
        long current;
        long updated;
        do {
            current = balanceCents.get();
            updated = current + amountCents;
        } while (!balanceCents.compareAndSet(current, updated));
    }

    /**
     * Atomically withdraws the given amount using a compareAndSet loop.
     * Returns false if insufficient funds.
     *
     * @param amountCents the amount in cents to withdraw
     * @return true if the withdrawal succeeded, false if insufficient funds
     */
    public boolean atomicWithdraw(long amountCents) {
        long current;
        long updated;
        do {
            current = balanceCents.get();
            if (current < amountCents) {
                return false;
            }
            updated = current - amountCents;
        } while (!balanceCents.compareAndSet(current, updated));
        return true;
    }

    /**
     * @return the current balance in cents
     */
    public long getBalanceCents() {
        return balanceCents.get();
    }

    /**
     * @return the current balance as a double (dollars)
     */
    public double getBalanceAsDouble() {
        return balanceCents.get() / 100.0;
    }

    /**
     * Atomically transitions the account status from expected to newStatus.
     *
     * @param expected  the expected current status
     * @param newStatus the desired new status
     * @return true if the transition succeeded
     */
    public boolean transitionStatus(AccountStatus expected, AccountStatus newStatus) {
        return status.compareAndSet(expected, newStatus);
    }

    /**
     * @return the current account status
     */
    public AccountStatus getStatus() {
        return status.get();
    }

    /**
     * @return the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }
}

package com.example.bankingdemo.model;

import com.example.bankingdemo.exception.MinimumBalanceException;

/**
 * Savings account with minimum balance constraint and pluggable interest calculation.
 * Demonstrates inheritance, interface implementation, and the strategy pattern.
 */
public class SavingsAccount extends Account implements Transferable, Reportable {

    private final double minimumBalance;
    private final double interestRate;
    private final InterestCalculator interestCalculator;

    /**
     * Create a new SavingsAccount.
     *
     * @param ownerName          the account owner's name
     * @param initialBalance     the starting balance
     * @param minimumBalance     the minimum balance that must be maintained
     * @param interestRate       the annual interest rate (e.g., 0.04 for 4%)
     * @param interestCalculator the strategy used to calculate interest
     */
    public SavingsAccount(String ownerName, double initialBalance,
                          double minimumBalance, double interestRate,
                          InterestCalculator interestCalculator) {
        super(ownerName, initialBalance);
        this.minimumBalance = minimumBalance;
        this.interestRate = interestRate;
        this.interestCalculator = interestCalculator;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Withdraw funds, enforcing the minimum balance constraint.
     * After withdrawal, balance must remain >= minimumBalance.
     */
    @Override
    public void withdraw(double amount) {
        if (getBalance() - amount < minimumBalance) {
            throw new MinimumBalanceException(
                    String.format("Withdrawal would breach minimum balance of %.2f", minimumBalance));
        }
        super.withdraw(amount);
    }

    @Override
    public double calculateInterest() {
        return interestCalculator.calculateInterest(getBalance(), interestRate);
    }

    @Override
    public void transferTo(Account target, double amount) {
        this.withdraw(amount);
        target.deposit(amount);
    }

    @Override
    public String generateStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Statement for SavingsAccount %s | Owner: %s | Balance: %.2f",
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
        return String.format("SavingsAccount[accountNumber=%s, ownerName=%s, balance=%.2f, minimumBalance=%.2f, interestRate=%.4f]",
                accountNumber, ownerName, getBalance(), minimumBalance, interestRate);
    }
}

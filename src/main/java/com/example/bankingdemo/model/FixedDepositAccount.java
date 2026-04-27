package com.example.bankingdemo.model;

/**
 * Fixed deposit account with a locked tenure and early withdrawal penalty.
 * Implements Reportable but NOT Transferable — fixed deposits do not allow direct transfers.
 */
public class FixedDepositAccount extends Account implements Reportable {

    private final int tenureMonths;
    private final double fixedRate;
    private final double earlyWithdrawalPenalty;

    /**
     * Create a new FixedDepositAccount.
     *
     * @param ownerName              the account owner's name
     * @param initialBalance         the deposited amount
     * @param tenureMonths           the lock-in period in months
     * @param fixedRate              the annual interest rate
     * @param earlyWithdrawalPenalty the penalty percentage for early withdrawal
     */
    public FixedDepositAccount(String ownerName, double initialBalance,
                               int tenureMonths, double fixedRate,
                               double earlyWithdrawalPenalty) {
        super(ownerName, initialBalance);
        this.tenureMonths = tenureMonths;
        this.fixedRate = fixedRate;
        this.earlyWithdrawalPenalty = earlyWithdrawalPenalty;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public double getFixedRate() {
        return fixedRate;
    }

    public double getEarlyWithdrawalPenalty() {
        return earlyWithdrawalPenalty;
    }

    @Override
    public double calculateInterest() {
        return getBalance() * fixedRate * (tenureMonths / 12.0);
    }

    @Override
    public String generateStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Statement for FixedDepositAccount %s | Owner: %s | Balance: %.2f | Tenure: %d months",
                accountNumber, ownerName, getBalance(), tenureMonths));
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
        return String.format("FixedDepositAccount[accountNumber=%s, ownerName=%s, balance=%.2f, tenureMonths=%d, fixedRate=%.4f]",
                accountNumber, ownerName, getBalance(), tenureMonths, fixedRate);
    }
}

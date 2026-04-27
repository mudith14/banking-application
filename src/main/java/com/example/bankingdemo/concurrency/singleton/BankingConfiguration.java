package com.example.bankingdemo.concurrency.singleton;

/**
 * Enum singleton holding global banking configuration.
 * Thread-safe by virtue of the JVM's enum initialization guarantees.
 */
public enum BankingConfiguration {

    INSTANCE;

    private double maxTransferLimit = 1_000_000.0;
    private double defaultInterestRate = 0.05;

    public double getMaxTransferLimit() {
        return maxTransferLimit;
    }

    public void setMaxTransferLimit(double maxTransferLimit) {
        this.maxTransferLimit = maxTransferLimit;
    }

    public double getDefaultInterestRate() {
        return defaultInterestRate;
    }

    public void setDefaultInterestRate(double defaultInterestRate) {
        this.defaultInterestRate = defaultInterestRate;
    }
}

package com.example.bankingdemo.model;

/**
 * Strategy interface for interest calculation.
 * Implementations provide different interest computation algorithms
 * (e.g., simple interest, compound interest).
 */
public interface InterestCalculator {

    /**
     * Calculate interest for a given balance and annual rate.
     *
     * @param balance the account balance
     * @param rate    the annual interest rate (e.g., 0.04 for 4%)
     * @return the calculated interest amount
     */
    double calculateInterest(double balance, double rate);
}

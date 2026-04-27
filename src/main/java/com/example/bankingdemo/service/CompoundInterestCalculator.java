package com.example.bankingdemo.service;

import com.example.bankingdemo.model.InterestCalculator;

/**
 * Compound interest calculator: interest = balance * ((1 + rate)^time - 1).
 * With the default time of 1, this returns balance * rate.
 */
public class CompoundInterestCalculator implements InterestCalculator {

    private final double time;

    public CompoundInterestCalculator() {
        this.time = 1.0;
    }

    public CompoundInterestCalculator(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    @Override
    public double calculateInterest(double balance, double rate) {
        return balance * (Math.pow(1 + rate, time) - 1);
    }
}

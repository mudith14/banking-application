package com.example.bankingdemo.service;

import com.example.bankingdemo.model.InterestCalculator;

/**
 * Simple interest calculator: interest = balance * rate * time.
 * With the default time of 1 (annual), this returns balance * rate.
 */
public class SimpleInterestCalculator implements InterestCalculator {

    private final double time;

    public SimpleInterestCalculator() {
        this.time = 1.0;
    }

    public SimpleInterestCalculator(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    @Override
    public double calculateInterest(double balance, double rate) {
        return balance * rate * time;
    }
}

package com.example.bankingdemo.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Property-based tests for SimpleInterestCalculator and CompoundInterestCalculator.
 * Feature: banking-oops-demo, Properties 9 and 10
 */
class InterestCalculatorProperties {

    /**
     * Property 9: For any non-negative balance and rate,
     * SimpleInterestCalculator.calculateInterest(balance, rate) with time=1
     * shall return balance * rate * 1 = balance * rate.
     *
     * Validates: Requirements 8.1
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 9: SimpleInterestCalculator formula")
    void simpleInterestShouldMatchFormula(
            @ForAll @DoubleRange(min = 0.0, max = 100000.0) double balance,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double rate) {

        SimpleInterestCalculator calculator = new SimpleInterestCalculator();
        double expected = balance * rate;

        assertThat(calculator.calculateInterest(balance, rate))
                .isCloseTo(expected, within(0.001));
    }

    /**
     * Property 10: For any non-negative balance, rate, and time,
     * CompoundInterestCalculator.calculateInterest(balance, rate) with a given time
     * shall return balance * ((1 + rate)^time - 1).
     *
     * Validates: Requirements 8.2
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 10: CompoundInterestCalculator formula")
    void compoundInterestShouldMatchFormula(
            @ForAll @DoubleRange(min = 0.0, max = 100000.0) double balance,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double rate,
            @ForAll @DoubleRange(min = 0.5, max = 10.0) double time) {

        CompoundInterestCalculator calculator = new CompoundInterestCalculator(time);
        double expected = balance * (Math.pow(1 + rate, time) - 1);

        assertThat(calculator.calculateInterest(balance, rate))
                .isCloseTo(expected, within(0.001));
    }
}

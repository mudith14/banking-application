package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Property-based test: SavingsAccount interest delegation.
 * Feature: banking-oops-demo, Property 8: SavingsAccount interest delegation
 *
 * Validates: Requirements 1.3, 8.4
 */
class SavingsInterestDelegationProperties {

    /**
     * Property 8: For any SavingsAccount with an injected InterestCalculator impl,
     * calling calculateInterest() shall return the same value as calling
     * interestCalculator.calculateInterest(balance, rate) directly.
     *
     * Validates: Requirements 1.3, 8.4
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 8: SavingsAccount interest delegation")
    void calculateInterestShouldDelegateToInjectedCalculator(
            @ForAll @DoubleRange(min = 100.0, max = 100000.0) double initialBalance,
            @ForAll @DoubleRange(min = 0.01, max = 0.20) double interestRate) {

        InterestCalculator calculator = (balance, rate) -> balance * rate;

        SavingsAccount account = new SavingsAccount(
                "Owner", initialBalance, 0.0, interestRate, calculator);

        double expected = calculator.calculateInterest(initialBalance, interestRate);

        assertThat(account.calculateInterest()).isCloseTo(expected, within(0.001));
    }
}

package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Property-based test: FixedDepositAccount interest calculation.
 * Feature: banking-oops-demo, Property 7: FixedDepositAccount interest calculation
 *
 * Validates: Requirements 1.5
 */
class FixedDepositInterestProperties {

    /**
     * Property 7: For any FixedDepositAccount with balance B, fixed rate R,
     * and tenure T, calculateInterest() shall return B * R * (T / 12.0).
     *
     * Validates: Requirements 1.5
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 7: FixedDepositAccount interest calculation")
    void fixedDepositInterestShouldMatchFormula(
            @ForAll @DoubleRange(min = 100.0, max = 100000.0) double balance,
            @ForAll @IntRange(min = 1, max = 60) int tenureMonths,
            @ForAll @DoubleRange(min = 0.01, max = 0.20) double fixedRate) {

        FixedDepositAccount account = new FixedDepositAccount(
                "Owner", balance, tenureMonths, fixedRate, 0.10);

        double expected = balance * fixedRate * (tenureMonths / 12.0);

        assertThat(account.calculateInterest()).isCloseTo(expected, within(0.001));
    }
}

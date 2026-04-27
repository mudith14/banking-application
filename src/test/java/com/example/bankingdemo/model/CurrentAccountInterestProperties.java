package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: CurrentAccount zero interest.
 * Feature: banking-oops-demo, Property 6: CurrentAccount zero interest
 *
 * Validates: Requirements 1.4
 */
class CurrentAccountInterestProperties {

    /**
     * Property 6: For any CurrentAccount with any balance,
     * calculateInterest() shall return 0.
     *
     * Validates: Requirements 1.4
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 6: CurrentAccount zero interest")
    void currentAccountInterestShouldAlwaysBeZero(
            @ForAll @DoubleRange(min = 0.0, max = 100000.0) double balance,
            @ForAll @DoubleRange(min = 0.0, max = 5000.0) double overdraftLimit) {

        CurrentAccount account = new CurrentAccount("Owner", balance, overdraftLimit);

        assertThat(account.calculateInterest()).isEqualTo(0.0);
    }
}

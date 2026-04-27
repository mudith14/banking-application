package com.example.bankingdemo.model;

import com.example.bankingdemo.exception.OverdraftLimitExceededException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based test: CurrentAccount overdraft limit.
 * Feature: banking-oops-demo, Property 4: CurrentAccount overdraft limit
 *
 * Validates: Requirements 2.6
 */
class CurrentAccountOverdraftProperties {

    /**
     * Property 4: For any CurrentAccount with balance B and overdraft limit L,
     * withdraw(amount) shall succeed when amount <= B + L and shall be rejected
     * when amount > B + L, leaving balance unchanged on rejection.
     *
     * Validates: Requirements 2.6
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 4: CurrentAccount overdraft limit")
    void overdraftLimitShouldBeEnforced(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double initialBalance,
            @ForAll @DoubleRange(min = 0.0, max = 5000.0) double overdraftLimit,
            @ForAll @DoubleRange(min = 0.01, max = 20000.0) double withdrawAmount) {

        CurrentAccount account = new CurrentAccount("Owner", initialBalance, overdraftLimit);
        double maxAllowed = initialBalance + overdraftLimit;

        if (withdrawAmount <= maxAllowed) {
            account.withdraw(withdrawAmount);
            assertThat(account.getBalance()).isCloseTo(
                    initialBalance - withdrawAmount, org.assertj.core.data.Offset.offset(0.001));
        } else {
            double balanceBefore = account.getBalance();
            assertThatThrownBy(() -> account.withdraw(withdrawAmount))
                    .isInstanceOf(OverdraftLimitExceededException.class);
            assertThat(account.getBalance()).isEqualTo(balanceBefore);
        }
    }
}

package com.example.bankingdemo.model;

import com.example.bankingdemo.exception.MinimumBalanceException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based test: SavingsAccount minimum balance invariant.
 * Feature: banking-oops-demo, Property 3: SavingsAccount minimum balance invariant
 *
 * Validates: Requirements 2.4, 2.5
 */
class SavingsWithdrawalProperties {

    private static final InterestCalculator SIMPLE_CALCULATOR = (balance, rate) -> balance * rate;

    /**
     * Property 3: For any SavingsAccount with balance B and minimum balance M,
     * after any successful withdraw(amount), the resulting balance shall be >= M.
     *
     * Validates: Requirements 2.4, 2.5
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 3: SavingsAccount minimum balance invariant")
    void successfulWithdrawalShouldMaintainMinimumBalance(
            @ForAll @DoubleRange(min = 500.0, max = 10000.0) double initialBalance,
            @ForAll @DoubleRange(min = 100.0, max = 499.0) double minimumBalance,
            @ForAll @DoubleRange(min = 0.01, max = 10000.0) double withdrawAmount) {

        SavingsAccount account = new SavingsAccount("Owner", initialBalance,
                minimumBalance, 0.04, SIMPLE_CALCULATOR);

        double maxAllowed = initialBalance - minimumBalance;

        if (withdrawAmount <= maxAllowed) {
            account.withdraw(withdrawAmount);
            assertThat(account.getBalance()).isGreaterThanOrEqualTo(minimumBalance);
        } else {
            double balanceBefore = account.getBalance();
            assertThatThrownBy(() -> account.withdraw(withdrawAmount))
                    .isInstanceOf(Exception.class);
            assertThat(account.getBalance()).isEqualTo(balanceBefore);
        }
    }
}

package com.example.bankingdemo.model;

import com.example.bankingdemo.exception.InvalidAmountException;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based test: non-positive deposit rejection.
 * Feature: banking-oops-demo, Property 2: Non-positive deposit rejection
 *
 * Validates: Requirements 2.3
 */
class DepositValidationProperties {

    /**
     * Minimal concrete Account subclass for testing purposes.
     */
    private static class TestAccount extends Account {
        TestAccount(String ownerName, double initialBalance) {
            super(ownerName, initialBalance);
        }

        @Override
        public double calculateInterest() {
            return 0;
        }
    }

    /**
     * Property 2: For any Account and any amount ≤ 0, calling deposit(amount)
     * shall leave the balance unchanged and signal an error (InvalidAmountException).
     *
     * Validates: Requirements 2.3
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 2: Non-positive deposit rejection")
    void depositOfNonPositiveAmountShouldBeRejected(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double initialBalance,
            @ForAll @DoubleRange(min = -10000.0, max = 0.0) double nonPositiveAmount) {

        TestAccount account = new TestAccount("TestOwner", initialBalance);
        double balanceBefore = account.getBalance();

        assertThatThrownBy(() -> account.deposit(nonPositiveAmount))
                .isInstanceOf(InvalidAmountException.class);

        assertThat(account.getBalance()).isEqualTo(balanceBefore);
    }
}

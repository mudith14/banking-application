package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: overloaded withdraw equivalence.
 * Feature: banking-oops-demo, Property 5: Overloaded withdraw equivalence
 *
 * Validates: Requirements 3.3
 */
class WithdrawOverloadProperties {

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
     * Property 5: For any Account and any valid withdrawal amount, calling
     * withdraw(amount) and withdraw(amount, reason) shall produce the same
     * resulting balance.
     *
     * Validates: Requirements 3.3
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 5: Overloaded withdraw equivalence")
    void withdrawWithAndWithoutReasonShouldProduceSameBalance(
            @ForAll @DoubleRange(min = 100.0, max = 10000.0) double initialBalance,
            @ForAll @DoubleRange(min = 0.01, max = 100.0) double withdrawAmount,
            @ForAll("reasons") String reason) {

        // Ensure withdraw amount does not exceed initial balance
        double safeAmount = Math.min(withdrawAmount, initialBalance);

        TestAccount account1 = new TestAccount("Owner1", initialBalance);
        TestAccount account2 = new TestAccount("Owner2", initialBalance);

        account1.withdraw(safeAmount);
        account2.withdraw(safeAmount, reason);

        assertThat(account1.getBalance()).isEqualTo(account2.getBalance());
    }

    @Provide
    Arbitrary<String> reasons() {
        return Arbitraries.of("ATM withdrawal", "Bill payment", "Transfer", "Rent", "Groceries");
    }
}

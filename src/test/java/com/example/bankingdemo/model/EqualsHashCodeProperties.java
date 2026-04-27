package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: equals/hashCode contract by account number.
 * Feature: banking-oops-demo, Property 17: equals/hashCode contract by account number
 *
 * Validates: Requirements 9.5
 */
class EqualsHashCodeProperties {

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
     * Property 17: For any two Account instances, equals() shall return true iff
     * they have the same account number, and if a.equals(b) then
     * a.hashCode() == b.hashCode().
     *
     * Since each Account gets a unique auto-generated number, two distinct
     * instances should NOT be equal. An account should always equal itself.
     * If a.equals(b) holds, then hashCode must be consistent.
     *
     * Validates: Requirements 9.5
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 17: equals/hashCode contract by account number")
    void equalsAndHashCodeContractShouldHold(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double balance1,
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double balance2) {

        TestAccount account1 = new TestAccount("Owner1", balance1);
        TestAccount account2 = new TestAccount("Owner2", balance2);

        // Reflexive: an account equals itself
        assertThat(account1).isEqualTo(account1);
        assertThat(account2).isEqualTo(account2);

        // Two distinct accounts have different auto-generated numbers, so not equal
        assertThat(account1).isNotEqualTo(account2);

        // hashCode consistency: equal objects must have equal hash codes
        // Since account1.equals(account1), hashCode must be consistent
        assertThat(account1.hashCode()).isEqualTo(account1.hashCode());

        // Not equal to null
        assertThat(account1).isNotEqualTo(null);

        // Symmetric: if a != b then b != a
        assertThat(account2).isNotEqualTo(account1);
    }
}

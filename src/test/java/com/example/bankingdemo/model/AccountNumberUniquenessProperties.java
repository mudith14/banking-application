package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: unique account numbers.
 * Feature: banking-oops-demo, Property 1: Unique account numbers
 *
 * Validates: Requirements 1.6
 */
class AccountNumberUniquenessProperties {

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
     * Property 1: For any sequence of N account creations (of any subclass type),
     * all N generated account numbers shall be distinct.
     *
     * Validates: Requirements 1.6
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 1: Unique account numbers")
    void allAccountNumbersShouldBeDistinct(@ForAll @IntRange(min = 2, max = 50) int count) {
        Set<String> accountNumbers = new HashSet<>();
        for (int i = 0; i < count; i++) {
            Account account = new TestAccount("Owner" + i, 100.0);
            accountNumbers.add(account.getAccountNumber());
        }
        assertThat(accountNumbers).hasSize(count);
    }
}

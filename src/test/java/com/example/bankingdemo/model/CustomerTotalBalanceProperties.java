package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: Customer total balance is sum of account balances.
 * Feature: banking-oops-demo, Property 11: Customer total balance is sum of account balances
 *
 * Validates: Requirements 5.4
 */
class CustomerTotalBalanceProperties {

    /**
     * Property 11: For any Customer with any set of accounts, getTotalBalance()
     * shall equal the sum of getBalance() across all owned accounts.
     *
     * Validates: Requirements 5.4
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 11: Customer total balance is sum of account balances")
    void totalBalanceShouldEqualSumOfAccountBalances(
            @ForAll @IntRange(min = 0, max = 10) int accountCount,
            @ForAll @DoubleRange(min = 0.0, max = 50000.0) double balanceSeed) {

        Customer customer = new Customer("TestCustomer");

        double expectedTotal = 0.0;
        for (int i = 0; i < accountCount; i++) {
            double balance = balanceSeed / (i + 1);
            CurrentAccount account = new CurrentAccount("Owner" + i, balance, 0);
            customer.addAccount(account);
            expectedTotal += account.getBalance();
        }

        assertThat(customer.getTotalBalance()).isCloseTo(expectedTotal,
                org.assertj.core.data.Offset.offset(0.001));
    }
}

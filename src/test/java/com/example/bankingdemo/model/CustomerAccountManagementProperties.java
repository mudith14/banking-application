package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: Customer account add/remove consistency.
 * Feature: banking-oops-demo, Property 12: Customer account add/remove consistency
 *
 * Validates: Requirements 5.2
 */
class CustomerAccountManagementProperties {

    /**
     * Property 12: For any Customer, adding an account then retrieving all accounts
     * shall include that account; removing an account then retrieving all accounts
     * shall not include that account.
     *
     * Validates: Requirements 5.2
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 12: Customer account add/remove consistency")
    void addedAccountShouldBeRetrievableAndRemovedAccountShouldNot(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double balance) {

        Customer customer = new Customer("TestCustomer");
        CurrentAccount account = new CurrentAccount("Owner", balance, 0);

        // After adding, the account should be present
        customer.addAccount(account);
        assertThat(customer.getAccounts()).contains(account);

        // After removing, the account should no longer be present
        customer.removeAccount(account);
        assertThat(customer.getAccounts()).doesNotContain(account);
    }
}

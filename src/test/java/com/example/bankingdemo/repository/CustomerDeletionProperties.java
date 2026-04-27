package com.example.bankingdemo.repository;

import com.example.bankingdemo.model.CurrentAccount;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: Delete customer closes all accounts.
 * Feature: banking-oops-demo, Property 19: Delete customer closes all accounts
 *
 * Validates: Requirements 5.3
 */
class CustomerDeletionProperties {

    /**
     * Property 19: For any Customer with N accounts, deleting the customer shall
     * result in all N accounts being removed from the AccountRepository.
     *
     * Test approach: Create an AccountRepository, add N CurrentAccount objects with
     * the same ownerName, then delete each account (simulating what the controller does),
     * and verify findByOwnerName returns an empty list.
     *
     * Validates: Requirements 5.3
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 19: Delete customer closes all accounts")
    void deletingCustomerRemovesAllAccountsFromRepository(
            @ForAll @IntRange(min = 1, max = 10) int numberOfAccounts) {

        AccountRepository repository = new AccountRepository();
        String ownerName = "CustomerToDelete";

        // Add N accounts with the same owner name
        for (int i = 0; i < numberOfAccounts; i++) {
            CurrentAccount account = new CurrentAccount(ownerName, 100.0 * (i + 1), 50.0);
            repository.save(account);
        }

        // Verify all accounts are present before deletion
        assertThat(repository.findByOwnerName(ownerName)).hasSize(numberOfAccounts);

        // Simulate customer deletion: find all accounts by owner, then delete each
        var accounts = repository.findByOwnerName(ownerName);
        for (var account : accounts) {
            repository.delete(account.getAccountNumber());
        }

        // After deletion, no accounts should remain for this owner
        assertThat(repository.findByOwnerName(ownerName)).isEmpty();
    }
}

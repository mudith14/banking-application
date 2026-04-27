package com.example.bankingdemo.repository;

import com.example.bankingdemo.model.Account;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Account objects backed by a ConcurrentHashMap.
 */
@Repository
public class AccountRepository {

    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    /**
     * Save (or update) an account.
     *
     * @param account the account to store
     */
    public void save(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    /**
     * Find an account by its account number.
     *
     * @param accountNumber the account number to look up
     * @return an Optional containing the account, or empty if not found
     */
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    /**
     * Find all accounts belonging to a given owner name.
     *
     * @param ownerName the owner name to search for
     * @return list of matching accounts (may be empty)
     */
    public List<Account> findByOwnerName(String ownerName) {
        return accounts.values().stream()
                .filter(a -> a.getOwnerName().equals(ownerName))
                .toList();
    }

    /**
     * Return all accounts in the repository.
     *
     * @return list of all accounts (may be empty)
     */
    public List<Account> findAll() {
        return List.copyOf(accounts.values());
    }

    /**
     * Delete an account by its account number.
     *
     * @param accountNumber the account number to remove
     */
    public void delete(String accountNumber) {
        accounts.remove(accountNumber);
    }
}

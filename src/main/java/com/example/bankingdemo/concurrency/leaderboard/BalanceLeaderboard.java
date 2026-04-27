package com.example.bankingdemo.concurrency.leaderboard;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Ranks accounts by balance using a ConcurrentSkipListMap.
 * Key = balance (Double), Value = list of account numbers with that balance.
 * Supports concurrent updates and retrieval of top accounts.
 */
@Component
public class BalanceLeaderboard {

    private final ConcurrentSkipListMap<Double, List<String>> leaderboard = new ConcurrentSkipListMap<>(Collections.reverseOrder());
    private final ConcurrentHashMap<String, Double> accountBalances = new ConcurrentHashMap<>();

    /**
     * Update or insert an account's balance in the leaderboard.
     *
     * @param accountNumber the account number
     * @param balance       the new balance
     */
    public void updateBalance(String accountNumber, double balance) {
        Double oldBalance = accountBalances.put(accountNumber, balance);

        // Remove from old balance bucket if present
        if (oldBalance != null) {
            leaderboard.computeIfPresent(oldBalance, (key, accounts) -> {
                accounts.remove(accountNumber);
                return accounts.isEmpty() ? null : accounts;
            });
        }

        // Add to new balance bucket
        leaderboard.compute(balance, (key, accounts) -> {
            if (accounts == null) {
                accounts = new ArrayList<>();
            }
            accounts.add(accountNumber);
            return accounts;
        });
    }

    /**
     * Get the top accounts ranked by balance (highest first).
     *
     * @param limit maximum number of entries to return
     * @return list of balance-to-account-list entries, sorted by descending balance
     */
    public List<Map.Entry<Double, List<String>>> getTopAccounts(int limit) {
        List<Map.Entry<Double, List<String>>> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Double, List<String>> entry : leaderboard.entrySet()) {
            if (count >= limit) break;
            result.add(Map.entry(entry.getKey(), new ArrayList<>(entry.getValue())));
            count++;
        }
        return result;
    }

    /**
     * Remove an account from the leaderboard.
     *
     * @param accountNumber the account number to remove
     */
    public void removeAccount(String accountNumber) {
        Double balance = accountBalances.remove(accountNumber);
        if (balance != null) {
            leaderboard.computeIfPresent(balance, (key, accounts) -> {
                accounts.remove(accountNumber);
                return accounts.isEmpty() ? null : accounts;
            });
        }
    }
}

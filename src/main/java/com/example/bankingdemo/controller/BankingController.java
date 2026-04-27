package com.example.bankingdemo.controller;

import com.example.bankingdemo.exception.AccountNotFoundException;
import com.example.bankingdemo.model.*;
import com.example.bankingdemo.repository.AccountRepository;
import com.example.bankingdemo.service.SimpleInterestCalculator;
import com.example.bankingdemo.service.TransactionLog;
import com.example.bankingdemo.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing banking operations via HTTP endpoints.
 */
@RestController
@RequestMapping("/api")
public class BankingController {

    private final AccountRepository accountRepository;
    private final TransferService transferService;
    private final TransactionLog transactionLog;
    private final InterestCalculator defaultInterestCalculator;

    public BankingController(AccountRepository accountRepository,
                             TransferService transferService,
                             TransactionLog transactionLog) {
        this.accountRepository = accountRepository;
        this.transferService = transferService;
        this.transactionLog = transactionLog;
        this.defaultInterestCalculator = new SimpleInterestCalculator();
    }

    /**
     * Create a new account of the specified type.
     * Request body keys: type, ownerName, initialBalance, and type-specific params.
     */
    @PostMapping("/accounts")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String ownerName = (String) request.get("ownerName");
        double initialBalance = ((Number) request.get("initialBalance")).doubleValue();

        Account account;
        switch (type.toLowerCase()) {
            case "savings" -> {
                double minimumBalance = ((Number) request.get("minimumBalance")).doubleValue();
                double interestRate = ((Number) request.get("interestRate")).doubleValue();
                account = new SavingsAccount(ownerName, initialBalance,
                        minimumBalance, interestRate, defaultInterestCalculator);
            }
            case "current" -> {
                double overdraftLimit = ((Number) request.get("overdraftLimit")).doubleValue();
                account = new CurrentAccount(ownerName, initialBalance, overdraftLimit);
            }
            case "fixed-deposit" -> {
                int tenureMonths = ((Number) request.get("tenureMonths")).intValue();
                double fixedRate = ((Number) request.get("fixedRate")).doubleValue();
                double earlyWithdrawalPenalty = ((Number) request.get("earlyWithdrawalPenalty")).doubleValue();
                account = new FixedDepositAccount(ownerName, initialBalance,
                        tenureMonths, fixedRate, earlyWithdrawalPenalty);
            }
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        }

        account.setTransactionLog(transactionLog);
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "accountNumber", account.getAccountNumber(),
                "type", type,
                "ownerName", ownerName,
                "balance", account.getBalance()
        ));
    }

    /**
     * Deposit funds into an account.
     */
    @PostMapping("/accounts/{accountNumber}/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @PathVariable String accountNumber,
            @RequestBody Map<String, Object> request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        double amount = ((Number) request.get("amount")).doubleValue();
        account.deposit(amount);

        return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "deposited", amount,
                "balance", account.getBalance()
        ));
    }

    /**
     * Withdraw funds from an account.
     */
    @PostMapping("/accounts/{accountNumber}/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @PathVariable String accountNumber,
            @RequestBody Map<String, Object> request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        double amount = ((Number) request.get("amount")).doubleValue();
        account.withdraw(amount);

        return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "withdrawn", amount,
                "balance", account.getBalance()
        ));
    }

    /**
     * Transfer funds between two accounts.
     */
    @PostMapping("/transfers")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody Map<String, Object> request) {
        String sourceAccountNumber = (String) request.get("sourceAccountNumber");
        String targetAccountNumber = (String) request.get("targetAccountNumber");
        double amount = ((Number) request.get("amount")).doubleValue();

        transferService.transfer(sourceAccountNumber, targetAccountNumber, amount);

        return ResponseEntity.ok(Map.of(
                "sourceAccountNumber", sourceAccountNumber,
                "targetAccountNumber", targetAccountNumber,
                "amount", amount,
                "status", "completed"
        ));
    }

    /**
     * Get account details by account number.
     */
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return ResponseEntity.ok(buildAccountResponse(account));
    }

    /**
     * Get transaction history for an account.
     */
    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String accountNumber) {
        accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        List<Transaction> transactions = transactionLog.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get all accounts for a customer by name.
     */
    @GetMapping("/customers/{customerName}/accounts")
    public ResponseEntity<List<Map<String, Object>>> getCustomerAccounts(
            @PathVariable String customerName) {
        List<Account> accounts = accountRepository.findByOwnerName(customerName);
        List<Map<String, Object>> response = accounts.stream()
                .map(this::buildAccountResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a customer and all their accounts.
     */
    @DeleteMapping("/customers/{customerName}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable String customerName) {
        List<Account> accounts = accountRepository.findByOwnerName(customerName);
        for (Account account : accounts) {
            accountRepository.delete(account.getAccountNumber());
        }
        return ResponseEntity.ok(Map.of(
                "customerName", customerName,
                "accountsClosed", accounts.size()
        ));
    }

    private Map<String, Object> buildAccountResponse(Account account) {
        String type;
        if (account instanceof SavingsAccount) {
            type = "savings";
        } else if (account instanceof CurrentAccount) {
            type = "current";
        } else if (account instanceof FixedDepositAccount) {
            type = "fixed-deposit";
        } else {
            type = "unknown";
        }

        return Map.of(
                "accountNumber", account.getAccountNumber(),
                "type", type,
                "ownerName", account.getOwnerName(),
                "balance", account.getBalance(),
                "interest", account.calculateInterest()
        );
    }
}

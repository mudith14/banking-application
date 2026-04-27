package com.example.bankingdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for REST endpoints exposed by BankingController.
 * Uses @SpringBootTest with MockMvc to test the full request/response cycle
 * including exception handling via BankingExceptionHandler.
 *
 * Validates: Requirements 3.1, 7.8, 7.9
 */
@SpringBootTest
@AutoConfigureMockMvc
class BankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Helper to create a savings account and return its account number ---
    private String createSavingsAccount(String ownerName, double initialBalance,
                                        double minimumBalance, double interestRate) throws Exception {
        Map<String, Object> body = Map.of(
                "type", "savings",
                "ownerName", ownerName,
                "initialBalance", initialBalance,
                "minimumBalance", minimumBalance,
                "interestRate", interestRate
        );
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        Map<?, ?> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (String) response.get("accountNumber");
    }

    // --- Helper to create a current account and return its account number ---
    private String createCurrentAccount(String ownerName, double initialBalance,
                                        double overdraftLimit) throws Exception {
        Map<String, Object> body = Map.of(
                "type", "current",
                "ownerName", ownerName,
                "initialBalance", initialBalance,
                "overdraftLimit", overdraftLimit
        );
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        Map<?, ?> response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (String) response.get("accountNumber");
    }

    /**
     * Test 1: GET /api/accounts/NONEXISTENT returns HTTP 404.
     * Validates: Requirements 7.8
     */
    @Test
    void getAccount_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/accounts/NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ACCOUNT_NOT_FOUND"));
    }

    /**
     * Test 2: Withdraw more than balance from savings account returns HTTP 400.
     * Validates: Requirements 7.9
     */
    @Test
    void withdraw_insufficientFunds_returns400() throws Exception {
        String accountNumber = createSavingsAccount("Alice", 500.0, 100.0, 0.04);

        Map<String, Object> withdrawBody = Map.of("amount", 1000.0);
        mockMvc.perform(post("/api/accounts/" + accountNumber + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MINIMUM_BALANCE_BREACH"));
    }

    /**
     * Test 3: Withdraw below minimum balance from savings account returns HTTP 400.
     * Validates: Requirements 7.9
     */
    @Test
    void withdraw_minimumBalanceBreach_returns400() throws Exception {
        // Balance 500, minimum 100 — withdrawing 450 would leave 50 < 100
        String accountNumber = createSavingsAccount("Bob", 500.0, 100.0, 0.04);

        Map<String, Object> withdrawBody = Map.of("amount", 450.0);
        mockMvc.perform(post("/api/accounts/" + accountNumber + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MINIMUM_BALANCE_BREACH"));
    }

    /**
     * Test 4: Withdraw beyond overdraft limit from current account returns HTTP 400.
     * Validates: Requirements 7.9
     */
    @Test
    void withdraw_overdraftLimitExceeded_returns400() throws Exception {
        // Balance 200, overdraft 100 — withdrawing 400 exceeds balance + overdraft (300)
        String accountNumber = createCurrentAccount("Charlie", 200.0, 100.0);

        Map<String, Object> withdrawBody = Map.of("amount", 400.0);
        mockMvc.perform(post("/api/accounts/" + accountNumber + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("OVERDRAFT_LIMIT_EXCEEDED"));
    }

    /**
     * Test 5: Successful deposit returns correct balance.
     * Validates: Requirements 7.9
     */
    @Test
    void deposit_success_returnsUpdatedBalance() throws Exception {
        String accountNumber = createCurrentAccount("Dana", 500.0, 0.0);

        Map<String, Object> depositBody = Map.of("amount", 250.0);
        mockMvc.perform(post("/api/accounts/" + accountNumber + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.deposited").value(250.0))
                .andExpect(jsonPath("$.balance").value(750.0));
    }

    /**
     * Test 6: Successful withdrawal returns correct balance.
     * Validates: Requirements 7.9
     */
    @Test
    void withdraw_success_returnsUpdatedBalance() throws Exception {
        String accountNumber = createCurrentAccount("Eve", 500.0, 0.0);

        Map<String, Object> withdrawBody = Map.of("amount", 200.0);
        mockMvc.perform(post("/api/accounts/" + accountNumber + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.withdrawn").value(200.0))
                .andExpect(jsonPath("$.balance").value(300.0));
    }

    /**
     * Test 7: Successful transfer between two accounts.
     * Validates: Requirements 7.9
     */
    @Test
    void transfer_success_returnsCompletedStatus() throws Exception {
        String sourceAccount = createCurrentAccount("Frank", 1000.0, 0.0);
        String targetAccount = createCurrentAccount("Grace", 500.0, 0.0);

        Map<String, Object> transferBody = Map.of(
                "sourceAccountNumber", sourceAccount,
                "targetAccountNumber", targetAccount,
                "amount", 300.0
        );
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.amount").value(300.0));

        // Verify balances via GET
        mockMvc.perform(get("/api/accounts/" + sourceAccount))
                .andExpect(jsonPath("$.balance").value(700.0));
        mockMvc.perform(get("/api/accounts/" + targetAccount))
                .andExpect(jsonPath("$.balance").value(800.0));
    }

    /**
     * Test 8: Polymorphic calculateInterest() — savings > 0, current = 0.
     * Validates: Requirements 3.1
     */
    @Test
    void calculateInterest_polymorphicBehavior_differsByAccountType() throws Exception {
        String savingsAccNum = createSavingsAccount("Heidi", 1000.0, 100.0, 0.05);
        String currentAccNum = createCurrentAccount("Ivan", 1000.0, 500.0);

        // Savings account should have interest > 0
        mockMvc.perform(get("/api/accounts/" + savingsAccNum))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interest", greaterThan(0.0)));

        // Current account should have interest = 0
        mockMvc.perform(get("/api/accounts/" + currentAccNum))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interest").value(0.0));
    }
}

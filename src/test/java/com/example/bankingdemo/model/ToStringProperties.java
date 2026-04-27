package com.example.bankingdemo.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test: toString contains all required subclass fields.
 * Feature: banking-oops-demo, Property 16: toString contains all required subclass fields
 *
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4
 */
class ToStringProperties {

    private static final InterestCalculator SIMPLE_CALCULATOR = (balance, rate) -> balance * rate;

    /**
     * Property 16 (SavingsAccount): toString() shall contain account number,
     * owner name, balance, minimum balance, and interest rate.
     *
     * Validates: Requirements 9.1, 9.2
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 16: toString contains all required subclass fields")
    void savingsAccountToStringShouldContainAllFields(
            @ForAll @DoubleRange(min = 500.0, max = 10000.0) double balance,
            @ForAll @DoubleRange(min = 100.0, max = 499.0) double minimumBalance,
            @ForAll @DoubleRange(min = 0.01, max = 0.20) double interestRate) {

        SavingsAccount account = new SavingsAccount(
                "TestOwner", balance, minimumBalance, interestRate, SIMPLE_CALCULATOR);

        String result = account.toString();

        assertThat(result).contains(account.getAccountNumber());
        assertThat(result).contains("TestOwner");
        assertThat(result).contains(String.format("%.2f", balance));
        assertThat(result).contains("SavingsAccount");
        assertThat(result).contains(String.format("%.2f", minimumBalance));
        assertThat(result).contains(String.format("%.4f", interestRate));
    }

    /**
     * Property 16 (CurrentAccount): toString() shall contain account number,
     * owner name, balance, and overdraft limit.
     *
     * Validates: Requirements 9.1, 9.3
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 16: toString contains all required subclass fields")
    void currentAccountToStringShouldContainAllFields(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double balance,
            @ForAll @DoubleRange(min = 0.0, max = 5000.0) double overdraftLimit) {

        CurrentAccount account = new CurrentAccount("TestOwner", balance, overdraftLimit);

        String result = account.toString();

        assertThat(result).contains(account.getAccountNumber());
        assertThat(result).contains("TestOwner");
        assertThat(result).contains(String.format("%.2f", balance));
        assertThat(result).contains("CurrentAccount");
        assertThat(result).contains(String.format("%.2f", overdraftLimit));
    }

    /**
     * Property 16 (FixedDepositAccount): toString() shall contain account number,
     * owner name, balance, tenure months, and fixed rate.
     *
     * Validates: Requirements 9.1
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 16: toString contains all required subclass fields")
    void fixedDepositAccountToStringShouldContainAllFields(
            @ForAll @DoubleRange(min = 100.0, max = 100000.0) double balance,
            @ForAll @IntRange(min = 1, max = 60) int tenureMonths,
            @ForAll @DoubleRange(min = 0.01, max = 0.20) double fixedRate) {

        FixedDepositAccount account = new FixedDepositAccount(
                "TestOwner", balance, tenureMonths, fixedRate, 0.10);

        String result = account.toString();

        assertThat(result).contains(account.getAccountNumber());
        assertThat(result).contains("TestOwner");
        assertThat(result).contains(String.format("%.2f", balance));
        assertThat(result).contains("FixedDepositAccount");
        assertThat(result).contains(String.valueOf(tenureMonths));
        assertThat(result).contains(String.format("%.4f", fixedRate));
    }
}

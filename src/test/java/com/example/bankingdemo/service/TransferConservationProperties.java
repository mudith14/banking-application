package com.example.bankingdemo.service;

import com.example.bankingdemo.model.CurrentAccount;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Property-based tests for transfer money conservation.
 * Feature: banking-oops-demo, Property 18
 */
class TransferConservationProperties {

    /**
     * Property 18: For any two Transferable accounts with combined balance T,
     * after a successful transfer of amount A from source to target,
     * the combined balance shall still equal T.
     *
     * Validates: Requirements 3.2, 4.2
     */
    @Property(tries = 100)
    @Tag("Feature: banking-oops-demo, Property 18: Transfer conserves total money")
    void transferShouldConserveTotalMoney(
            @ForAll @DoubleRange(min = 1.0, max = 100000.0) double sourceBalance,
            @ForAll @DoubleRange(min = 0.0, max = 100000.0) double targetBalance,
            @ForAll @DoubleRange(min = 0.01, max = 1.0) double transferFraction) {

        // Use CurrentAccount as the simplest Transferable implementation.
        // Overdraft limit of 0 keeps it straightforward.
        CurrentAccount source = new CurrentAccount("Source", sourceBalance, 0);
        CurrentAccount target = new CurrentAccount("Target", targetBalance, 0);

        double totalBefore = source.getBalance() + target.getBalance();

        // Transfer a fraction of the source balance so it always succeeds
        double amount = sourceBalance * transferFraction;
        source.transferTo(target, amount);

        double totalAfter = source.getBalance() + target.getBalance();

        assertThat(totalAfter).isCloseTo(totalBefore, within(0.001));
    }
}

package com.example.bankingdemo.concurrency.model;

import java.util.Map;

/**
 * Result of a multi-phase audit process coordinated by Phaser.
 *
 * @param accountSnapshots map of account number to balance snapshot
 * @param balancesValid    whether all balances passed validation
 * @param reportSummary    human-readable summary of the audit
 * @param auditTimeMs      time taken to complete the audit in milliseconds
 */
public record AuditReport(
        Map<String, Double> accountSnapshots,
        boolean balancesValid,
        String reportSummary,
        long auditTimeMs
) {}

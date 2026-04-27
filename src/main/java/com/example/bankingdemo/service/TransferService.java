package com.example.bankingdemo.service;

import com.example.bankingdemo.exception.AccountNotFoundException;
import com.example.bankingdemo.exception.NonTransferableAccountException;
import com.example.bankingdemo.model.Account;
import com.example.bankingdemo.model.Transaction;
import com.example.bankingdemo.model.TransactionType;
import com.example.bankingdemo.model.Transferable;
import com.example.bankingdemo.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service that orchestrates fund transfers between accounts.
 */
@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLog transactionLog;

    public TransferService(AccountRepository accountRepository, TransactionLog transactionLog) {
        this.accountRepository = accountRepository;
        this.transactionLog = transactionLog;
    }

    /**
     * Transfer funds from one account to another.
     *
     * @param sourceAccountNumber the account to debit
     * @param targetAccountNumber the account to credit
     * @param amount              the amount to transfer
     * @throws AccountNotFoundException       if either account does not exist
     * @throws NonTransferableAccountException if the source account is not Transferable
     */
    public void transfer(String sourceAccountNumber, String targetAccountNumber, double amount) {
        Account source = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(sourceAccountNumber));
        Account target = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(targetAccountNumber));

        if (!(source instanceof Transferable transferable)) {
            throw new NonTransferableAccountException(sourceAccountNumber);
        }

        transferable.transferTo(target, amount);

        transactionLog.addTransaction(new Transaction(
                TransactionType.TRANSFER, amount, LocalDateTime.now(),
                sourceAccountNumber, targetAccountNumber));
    }
}

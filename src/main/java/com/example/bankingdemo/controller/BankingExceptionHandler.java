package com.example.bankingdemo.controller;

import com.example.bankingdemo.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler that maps banking exceptions to consistent HTTP responses.
 * Returns JSON body: {"error": "ERROR_CODE", "message": "..."} for all banking errors.
 */
@ControllerAdvice
public class BankingExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "ACCOUNT_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAmount(InvalidAmountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "INVALID_AMOUNT", "message", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "INSUFFICIENT_FUNDS", "message", ex.getMessage()));
    }

    @ExceptionHandler(MinimumBalanceException.class)
    public ResponseEntity<Map<String, String>> handleMinimumBalance(MinimumBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "MINIMUM_BALANCE_BREACH", "message", ex.getMessage()));
    }

    @ExceptionHandler(OverdraftLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleOverdraftLimitExceeded(OverdraftLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "OVERDRAFT_LIMIT_EXCEEDED", "message", ex.getMessage()));
    }

    @ExceptionHandler(NonTransferableAccountException.class)
    public ResponseEntity<Map<String, String>> handleNonTransferable(NonTransferableAccountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "NON_TRANSFERABLE_ACCOUNT", "message", ex.getMessage()));
    }

    @ExceptionHandler(BankingException.class)
    public ResponseEntity<Map<String, String>> handleBankingException(BankingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "BANKING_ERROR", "message", ex.getMessage()));
    }
}

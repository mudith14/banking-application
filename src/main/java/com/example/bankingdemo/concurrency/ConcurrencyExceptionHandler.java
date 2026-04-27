package com.example.bankingdemo.concurrency;

import com.example.bankingdemo.concurrency.exception.DeadlockDetectedException;
import com.example.bankingdemo.concurrency.exception.LockAcquisitionTimeoutException;
import com.example.bankingdemo.concurrency.exception.QueueFullException;
import com.example.bankingdemo.concurrency.exception.ThreadInterruptedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Global exception handler for concurrency-related exceptions.
 * Maps each exception to the appropriate HTTP status and JSON error body.
 */
@ControllerAdvice
public class ConcurrencyExceptionHandler {

    @ExceptionHandler(LockAcquisitionTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleLockTimeout(LockAcquisitionTimeoutException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "LOCK_TIMEOUT", "message", ex.getMessage()));
    }

    @ExceptionHandler(DeadlockDetectedException.class)
    public ResponseEntity<Map<String, Object>> handleDeadlock(DeadlockDetectedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "DEADLOCK_DETECTED", "message", ex.getMessage()));
    }

    @ExceptionHandler(ThreadInterruptedException.class)
    public ResponseEntity<Map<String, Object>> handleThreadInterrupted(ThreadInterruptedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "THREAD_INTERRUPTED",
                        "message", ex.getMessage(),
                        "retryAfter", 5
                ));
    }

    @ExceptionHandler(QueueFullException.class)
    public ResponseEntity<Map<String, Object>> handleQueueFull(QueueFullException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "QUEUE_FULL", "message", ex.getMessage()));
    }
}

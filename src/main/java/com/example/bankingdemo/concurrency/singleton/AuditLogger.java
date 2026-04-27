package com.example.bankingdemo.concurrency.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton using the Bill Pugh initialization-on-demand holder pattern.
 * Stores audit log messages in a thread-safe list.
 */
public class AuditLogger {

    private final List<String> logs = new CopyOnWriteArrayList<>();

    private AuditLogger() {
        // private constructor to prevent external instantiation
    }

    private static class Holder {
        static final AuditLogger INSTANCE = new AuditLogger();
    }

    /**
     * Get the singleton instance via the holder pattern.
     *
     * @return the singleton AuditLogger instance
     */
    public static AuditLogger getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Log an audit message.
     *
     * @param message the message to log
     */
    public void log(String message) {
        logs.add(message);
    }

    /**
     * Get all logged messages.
     *
     * @return an unmodifiable view of the log entries
     */
    public List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }
}

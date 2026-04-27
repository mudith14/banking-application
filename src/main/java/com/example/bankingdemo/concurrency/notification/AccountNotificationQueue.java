package com.example.bankingdemo.concurrency.notification;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Async event notification queue using ConcurrentLinkedQueue.
 * Provides non-blocking, thread-safe notification storage and retrieval.
 */
@Component
public class AccountNotificationQueue {

    private final ConcurrentLinkedQueue<String> notifications = new ConcurrentLinkedQueue<>();

    /**
     * Add a notification message to the queue.
     *
     * @param message the notification message
     */
    public void addNotification(String message) {
        notifications.add(message);
    }

    /**
     * Poll the next notification from the queue.
     *
     * @return the next notification message, or null if the queue is empty
     */
    public String pollNotification() {
        return notifications.poll();
    }

    /**
     * Get the current number of pending notifications.
     *
     * @return the queue size
     */
    public int size() {
        return notifications.size();
    }
}

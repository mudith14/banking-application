package com.example.bankingdemo.concurrency.detection;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Detects deadlocked threads using ThreadMXBean.
 * Runs as a scheduled task at a configurable interval.
 */
@Component
public class DeadlockDetector {

    private static final Logger log = LoggerFactory.getLogger(DeadlockDetector.class);
    private static final long DEFAULT_INTERVAL_SECONDS = 30;

    private final ThreadMXBean threadMXBean;
    private final ScheduledExecutorService scheduler;

    public DeadlockDetector() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "deadlock-detector");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(
                this::runDetection,
                DEFAULT_INTERVAL_SECONDS,
                DEFAULT_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private void runDetection() {
        Map<String, Object> result = detectDeadlocks();
        if ((boolean) result.get("deadlocked")) {
            log.warn("Deadlock detected! Threads: {}", result.get("threadNames"));
        }
    }

    /**
     * Detect deadlocked threads.
     *
     * @return map with keys: "deadlocked" (Boolean), "threadNames" (List&lt;String&gt;),
     *         "lockInfo" (List&lt;String&gt;)
     */
    public Map<String, Object> detectDeadlocks() {
        Map<String, Object> result = new HashMap<>();
        long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();

        if (deadlockedThreadIds == null || deadlockedThreadIds.length == 0) {
            result.put("deadlocked", false);
            result.put("threadNames", List.of());
            result.put("lockInfo", List.of());
            return result;
        }

        List<String> threadNames = new ArrayList<>();
        List<String> lockInfo = new ArrayList<>();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreadIds, true, true);

        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                threadNames.add(info.getThreadName());
                if (info.getLockInfo() != null) {
                    lockInfo.add(String.format("Thread '%s' waiting on %s held by '%s'",
                            info.getThreadName(),
                            info.getLockInfo(),
                            info.getLockOwnerName()));
                }
            }
        }

        result.put("deadlocked", true);
        result.put("threadNames", threadNames);
        result.put("lockInfo", lockInfo);
        return result;
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("DeadlockDetector scheduler shut down");
    }
}

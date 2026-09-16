package com.vityarthi.edupulse.util;

import com.vityarthi.edupulse.model.AuditLog;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Singleton Asynchronous Audit Logger.
 * Demonstrates Concurrency and Background Thread processing.
 */
public class AsyncAuditLogger {
    private static final AsyncAuditLogger INSTANCE = new AsyncAuditLogger();
    private static final String LOG_FILE = "data/audit.log";

    private final BlockingQueue<AuditLog> logQueue = new LinkedBlockingQueue<>();
    private final List<AuditLog> inMemoryLogs = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "EduPulse-AuditLogger-Thread");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean running = true;

    private AsyncAuditLogger() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException ignored) {}

        worker.submit(this::processQueue);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public static AsyncAuditLogger getInstance() {
        return INSTANCE;
    }

    public void log(String userId, String action, String status, String details) {
        String logId = "LOG-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        AuditLog entry = new AuditLog(logId, userId, action, status, details);
        inMemoryLogs.add(entry);
        logQueue.offer(entry);
    }

    private void processQueue() {
        while (running || !logQueue.isEmpty()) {
            try {
                AuditLog entry = logQueue.poll(200, TimeUnit.MILLISECONDS);
                if (entry != null) {
                    writeToFile(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Failed to write audit log: " + e.getMessage());
            }
        }
    }

    private synchronized void writeToFile(AuditLog entry) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(entry.toString());
        } catch (IOException e) {
            // fallback
        }
    }

    public List<AuditLog> getRecentLogs(int limit) {
        synchronized (inMemoryLogs) {
            int size = inMemoryLogs.size();
            int start = Math.max(0, size - limit);
            return new ArrayList<>(inMemoryLogs.subList(start, size));
        }
    }

    public void shutdown() {
        running = false;
        worker.shutdown();
        try {
            if (!worker.awaitTermination(2, TimeUnit.SECONDS)) {
                worker.shutdownNow();
            }
        } catch (InterruptedException e) {
            worker.shutdownNow();
        }
    }
}

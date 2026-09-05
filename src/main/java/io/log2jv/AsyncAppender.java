package io.log2jv;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Wraps any other {@link Appender} and moves the actual writing
 * (e.g. to a file) onto a dedicated background thread so that the
 * calling code (e.g. an HTTP request handler) is not blocked on I/O.
 * <p>
 * If the queue is full, the new record is discarded instead of
 * blocking the caller — a deliberate trade-off of "better to lose one
 * log line than to slow down the whole application".
 */
public class AsyncAppender implements Appender {

    private final Appender target;
    private final BlockingQueue<LogRecord> queue;
    private final Thread worker;
    private volatile boolean running = true;

    public AsyncAppender(Appender target, int capacity) {
        this.target = target;
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.worker = new Thread(this::processQueue, "log2jv-async-appender");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    @Override
    public void append(LogRecord record) {
        if (!running) {
            return;
        }
        boolean accepted = queue.offer(record);
        if (!accepted) {
            System.err.println("log2jv: AsyncAppender queue is full, record discarded");
        }
    }

    private void processQueue() {
        while (running || !queue.isEmpty()) {
            try {
                LogRecord record = queue.poll(200, TimeUnit.MILLISECONDS);
                if (record != null) {
                    target.append(record);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void close() {
        running = false;
        try {
            worker.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        target.close();
    }
}

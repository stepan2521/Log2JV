package io.log2jv;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Writes records to a file. When the file exceeds {@code maxSizeBytes},
 * rotation occurs: app.log -> app.log.1 -> app.log.2 -> ... up to
 * {@code maxBackups}; the oldest copy is deleted.
 * <p>
 * Size-based rotation was chosen for the first version as the simplest
 * and most predictable approach. Time-based rotation (e.g. daily) can
 * be added later as a separate class implementing the same Appender interface.
 */
public class FileAppender implements Appender {

    private final Path filePath;
    private final Formatter formatter;
    private final long maxSizeBytes;
    private final int maxBackups;
    private final Object lock = new Object();

    private Writer writer;
    private long currentSize;

    public FileAppender(String path, Formatter formatter, long maxSizeBytes, int maxBackups) {
        this.filePath = Paths.get(path);
        this.formatter = formatter;
        this.maxSizeBytes = maxSizeBytes;
        this.maxBackups = maxBackups;
        openWriter();
    }

    private void openWriter() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            this.writer = Files.newBufferedWriter(
                    filePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
            this.currentSize = Files.exists(filePath) ? Files.size(filePath) : 0L;
        } catch (IOException e) {
            throw new UncheckedLoggerException("Failed to open log file: " + filePath, e);
        }
    }

    @Override
    public void append(LogRecord record) {
        String line = formatter.format(record);
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);

        synchronized (lock) {
            try {
                if (maxSizeBytes > 0 && currentSize + bytes.length > maxSizeBytes) {
                    rotate();
                }
                writer.write(line);
                writer.flush();
                currentSize += bytes.length;
            } catch (IOException e) {
                // An appender must not crash the application because of a logging error —
                // report to stderr and continue.
                System.err.println("log2jv: error writing to log file: " + e.getMessage());
            }
        }
    }

    private void rotate() throws IOException {
        writer.close();

        for (int i = maxBackups - 1; i >= 1; i--) {
            Path src = backupPath(i);
            Path dst = backupPath(i + 1);
            if (Files.exists(src)) {
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        if (maxBackups > 0) {
            Files.move(filePath, backupPath(1), StandardCopyOption.REPLACE_EXISTING);
        }

        openWriter();
    }

    private Path backupPath(int index) {
        return filePath.resolveSibling(filePath.getFileName() + "." + index);
    }

    @Override
    public void close() {
        synchronized (lock) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // nothing useful can be done during shutdown
            }
        }
    }
}

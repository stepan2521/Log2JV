package io.log2jv;

/**
 * An appender is responsible for "where" a log record goes: console, file,
 * network, etc. Implementations must be thread-safe because a single
 * appender is typically shared by all loggers in the application.
 */
public interface Appender {

    void append(LogRecord record);

    /**
     * Releases resources (closes files, stops threads).
     * Called once when the application shuts down.
     */
    default void close() {
        // nothing to close by default
    }
}

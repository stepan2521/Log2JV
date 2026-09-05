package io.log2jv;

/**
 * The main interface used by application code.
 * Obtain an instance via {@link LogManager#getLogger(Class)}.
 */
public interface Logger {

    void trace(String message);

    void trace(String format, Object... args);

    void debug(String message);

    void debug(String format, Object... args);

    void info(String message);

    void info(String format, Object... args);

    void warn(String message);

    void warn(String format, Object... args);

    void error(String message);

    void error(String format, Object... args);

    void error(String message, Throwable throwable);

    /**
     * Logs a FATAL message and then throws {@link UncheckedLoggerException},
     * terminating the application.
     */
    void fatal(String message);

    /**
     * Formats and logs a FATAL message, then throws
     * {@link UncheckedLoggerException}, terminating the application.
     */
    void fatal(String format, Object... args);

    /**
     * Logs a FATAL message with the given throwable and then rethrows it
     * (or wraps it in {@link UncheckedLoggerException} if it is checked),
     * terminating the application.
     */
    void fatal(String message, Throwable throwable);

    boolean isEnabledFor(Level level);
}

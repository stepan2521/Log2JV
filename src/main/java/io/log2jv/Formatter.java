package io.log2jv;

/**
 * Turns a {@link LogRecord} into a ready-to-output string.
 */
public interface Formatter {
    String format(LogRecord record);
}

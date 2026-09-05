package io.log2jv;

/**
 * Wraps low-level errors (for example, an IOException when opening the
 * log file) into an unchecked exception so that users of the library
 * are not forced to handle checked exceptions at every step.
 */
public class UncheckedLoggerException extends RuntimeException {

    public UncheckedLoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}

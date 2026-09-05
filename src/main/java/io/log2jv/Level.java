package io.log2jv;

/**
 * Logging levels, from the most verbose to the most severe.
 */
public enum Level {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5),
    OFF(6);

    private final int severity;

    Level(int severity) {
        this.severity = severity;
    }

    /**
     * @param threshold the minimum level configured for the logger
     * @return true if a message of this level should be written
     */
    public boolean isEnabledFor(Level threshold) {
        return this.severity >= threshold.severity;
    }
}

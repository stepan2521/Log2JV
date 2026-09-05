package io.log2jv;

import java.time.Instant;

/**
 * A single log event.
 * <p>
 * The object is immutable, therefore it can be safely
 * passed between threads.
 */
public final class LogRecord {

    private final Instant timestamp;
    private final Level level;
    private final String loggerName;
    private final String message;
    private final Throwable throwable;
    private final String threadName;

    private final String sourceClassName;
    private final String sourceMethodName;
    private final String sourceFileName;
    private final int sourceLineNumber;

    public LogRecord(Level level, String loggerName, String message, Throwable throwable) {
        this(level, loggerName, message, throwable, null);
    }

    public LogRecord(Level level, String loggerName, String message, Throwable throwable, StackTraceElement source) {
        this.timestamp = Instant.now();
        this.level = level;
        this.loggerName = loggerName;
        this.message = message;
        this.throwable = throwable;
        this.threadName = Thread.currentThread().getName();

        this.sourceClassName = source != null ? source.getClassName() : null;
        this.sourceMethodName = source != null ? source.getMethodName() : null;
        this.sourceFileName = source != null ? source.getFileName() : null;
        this.sourceLineNumber = source != null ? source.getLineNumber() : -1;
    }

    public Instant getTimestamp() {return timestamp;}
    public Level getLevel() {return level;}
    public String getLoggerName() {return loggerName;}
    public String getMessage() {return message;}
    public Throwable getThrowable() {return throwable;}
    public String getThreadName() {return threadName;}
    public String getSourceClassName() {return sourceClassName;}
    public String getSourceMethodName() {return sourceMethodName;}
    public String getSourceFileName() {return sourceFileName;}
    public int getSourceLineNumber() {return sourceLineNumber;}
}

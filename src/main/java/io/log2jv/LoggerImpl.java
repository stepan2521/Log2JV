package io.log2jv;

import java.util.List;
import java.util.Optional;

final class LoggerImpl implements Logger {

    private final String name;
    private final Level threshold;
    private final List<Appender> appenders;

    LoggerImpl(String name, Level threshold, List<Appender> appenders) {
        this.name = name;
        this.threshold = threshold;
        this.appenders = appenders;
    }

    @Override public void trace(String message) {log(Level.TRACE, message, null);}
    @Override public void trace(String format, Object... args) {logFormatted(Level.TRACE, format, args);}

    @Override public void debug(String message) {log(Level.DEBUG, message, null);}
    @Override public void debug(String format, Object... args) {logFormatted(Level.DEBUG, format, args);}

    @Override public void info(String message) {log(Level.INFO, message, null);}
    @Override public void info(String format, Object... args) {logFormatted(Level.INFO, format, args);}

    @Override public void warn(String message) {log(Level.WARN, message, null);}
    @Override public void warn(String format, Object... args) {logFormatted(Level.WARN, format, args);}

    @Override public void error(String message) {log(Level.ERROR, message, null);}
    @Override public void error(String format, Object... args) {logFormatted(Level.ERROR, format, args);}
    @Override public void error(String message, Throwable throwable) {log(Level.ERROR, message, throwable);}

    @Override
    public void fatal(String message) {
        log(Level.FATAL, message, null);
        throw new UncheckedLoggerException("FATAL: " + message, null);
    }

    @Override
    public void fatal(String format, Object... args) {
        String message = MessageFormatter.format(format, args);
        log(Level.FATAL, message, null);
        throw new UncheckedLoggerException("FATAL: " + message, null);
    }

    @Override
    public void fatal(String message, Throwable throwable) {
        log(Level.FATAL, message, throwable);
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new UncheckedLoggerException("FATAL: " + message, throwable);
    }

    @Override public boolean isEnabledFor(Level level) {return level.isEnabledFor(threshold);}

    private void logFormatted(Level level, String format, Object[] args) {
        if (!isEnabledFor(level)) {return;}
        log(level, MessageFormatter.format(format, args), null);
    }

    private void log(Level level, String message, Throwable throwable) {
        if (!isEnabledFor(level)) {return;}
        LogRecord record = new LogRecord(level, name, message, throwable, findCaller());
        for (Appender appender : appenders) {appender.append(record);}
    }

    private StackTraceElement findCaller() {
        Optional<StackTraceElement> caller = StackWalker.getInstance()
                .walk(frames -> frames
                        .filter(frame -> !isLoggerInternal(frame.getClassName()))
                        .findFirst()
                        .map(StackWalker.StackFrame::toStackTraceElement));

        return caller.orElse(null);
    }

    private boolean isLoggerInternal(String className) {
        return className.equals(LoggerImpl.class.getName())
                || className.equals(Logger.class.getName())
                || className.equals(LogManager.class.getName())
                || className.equals(MessageFormatter.class.getName())
                || className.equals(LogRecord.class.getName());
    }
}

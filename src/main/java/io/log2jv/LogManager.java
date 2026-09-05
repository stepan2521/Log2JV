package io.log2jv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point of the library. Usage example:
 *
 * <pre>{@code
 * private static final Logger log = LogManager.getLogger(MyClass.class);
 * log.info("Application started");
 * }</pre>
 *
 * Configuration is loaded once on the first access to the class
 * (see {@link ConfigLoader}); after that all appenders are shared
 * by every logger in the application.
 */
public final class LogManager {

    private static final Config CONFIG = ConfigLoader.load();
    private static final List<Appender> APPENDERS = Collections.unmodifiableList(buildAppenders());
    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> APPENDERS.forEach(Appender::close), "log2jv-shutdown"));
    }

    private LogManager() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return LOGGERS.computeIfAbsent(name, n -> new LoggerImpl(n, CONFIG.level, APPENDERS));
    }

    private static List<Appender> buildAppenders() {
        List<Appender> list = new ArrayList<>();

        if (CONFIG.consoleEnabled) {
            list.add(wrapAsyncIfNeeded(new ConsoleAppender(new PatternFormatter(CONFIG.consolePattern), CONFIG.consoleColors)));
        }

        if (CONFIG.fileEnabled) {
            Appender fileAppender = new FileAppender(
                    CONFIG.filePath,
                    new PatternFormatter(CONFIG.filePattern),
                    CONFIG.fileMaxSizeBytes,
                    CONFIG.fileMaxBackups
            );
            list.add(wrapAsyncIfNeeded(fileAppender));
        }

        if (list.isEmpty()) {
            // safety net: a logger must always have somewhere to write
            list.add(new ConsoleAppender(new PatternFormatter(CONFIG.consolePattern), CONFIG.consoleColors));
        }

        return list;
    }

    private static Appender wrapAsyncIfNeeded(Appender appender) {
        return CONFIG.asyncEnabled ? new AsyncAppender(appender, CONFIG.asyncQueueCapacity) : appender;
    }
}

package io.log2jv;

/**
 * Writes formatted records to the console.
 * <p>
 * ERROR / FATAL → stderr<br>
 * all others → stdout
 */
public class ConsoleAppender implements Appender {

    private static final String RESET = "\u001B[0m";

    private static final String FATAL = "\u001B[1;35m"; // bold magenta
    private static final String ERROR = "\u001B[1;31m"; // bold red
    private static final String WARN  = "\u001B[1;33m"; // bold yellow
    private static final String INFO  = "\u001B[3;37m"; // italic white
    private static final String DEBUG = "\u001B[1;37m"; // bold white
    private static final String TRACE = "\u001B[3;90m"; // italic gray

    private final Formatter formatter;
    private final boolean colorsEnabled;

    public ConsoleAppender(Formatter formatter) {
        this(formatter, true);
    }

    public ConsoleAppender(
            Formatter formatter,
            boolean colorsEnabled
    ) {
        this.formatter = formatter;
        this.colorsEnabled = colorsEnabled;
    }

    @Override
    public void append(LogRecord record) {
        String line = formatter.format(record);

        if (colorsEnabled) {
            line = colorize(record.getLevel(), line);
        }

        if (record.getLevel() == Level.ERROR || record.getLevel() == Level.FATAL) {
            System.err.print(line);
        } else {
            System.out.print(line);
        }
    }

    private String colorize(
            Level level,
            String line
    ) {
        String style;

        switch (level) {
            case FATAL:
                style = FATAL;
                break;

            case ERROR:
                style = ERROR;
                break;

            case WARN:
                style = WARN;
                break;

            case INFO:
                style = INFO;
                break;

            case DEBUG:
                style = DEBUG;
                break;

            case TRACE:
                style = TRACE;
                break;

            default:
                return line;
        }

        return style + line + RESET;
    }
}

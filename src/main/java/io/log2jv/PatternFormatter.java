package io.log2jv;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats a {@link LogRecord} according to a pattern.
 * <p>
 * Supported tokens:
 * <ul>
 *   <li>{@code %d{pattern}} — date/time</li>
 *   <li>{@code %level}      — level</li>
 *   <li>{@code %logger}     — logger name</li>
 *   <li>{@code %thread}     — thread name</li>
 *   <li>{@code %class}      — source class</li>
 *   <li>{@code %method}     — source method</li>
 *   <li>{@code %file}       — source file</li>
 *   <li>{@code %line}       — source line</li>
 *   <li>{@code %source}     — Class.method(File.java:line)</li>
 *   <li>{@code %msg}        — message</li>
 *   <li>{@code %ex}         — stack trace of the Throwable</li>
 *   <li>{@code %n}          — line separator</li>
 * </ul>
 */
public class PatternFormatter implements Formatter {

    private static final Pattern TOKEN = Pattern.compile(
            "%d(\\{([^}]*)})?"
                    + "|%level"
                    + "|%logger"
                    + "|%thread"
                    + "|%class"
                    + "|%method"
                    + "|%file"
                    + "|%line"
                    + "|%source"
                    + "|%msg"
                    + "|%ex"
                    + "|%n"
    );

    private static final String DEFAULT_DATE_PATTERN =
            "yyyy-MM-dd HH:mm:ss.SSS";

    private final String pattern;

    public PatternFormatter(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        Matcher matcher = TOKEN.matcher(pattern);
        int last = 0;

        while (matcher.find()) {
            sb.append(pattern, last, matcher.start());
            appendToken(sb, matcher, record);
            last = matcher.end();
        }

        sb.append(pattern, last, pattern.length());

        return sb.toString();
    }

    private void appendToken(
            StringBuilder sb,
            Matcher matcher,
            LogRecord record
    ) {
        String token = matcher.group();

        if (token.startsWith("%d")) {
            String datePattern =
                    matcher.group(2) != null
                            ? matcher.group(2)
                            : DEFAULT_DATE_PATTERN;

            DateTimeFormatter formatter =
                    DateTimeFormatter
                            .ofPattern(datePattern)
                            .withZone(ZoneId.systemDefault());

            sb.append(formatter.format(record.getTimestamp()));
            return;
        }

        switch (token) {
            case "%level": sb.append(record.getLevel()); break;
            case "%logger": sb.append(record.getLoggerName()); break;
            case "%thread": sb.append(record.getThreadName()); break;
            case "%class": appendOrUnknown(sb, record.getSourceClassName()); break;
            case "%method": appendOrUnknown(sb, record.getSourceMethodName()); break;
            case "%file": appendOrUnknown(sb, record.getSourceFileName()); break;

            case "%line":
                if (record.getSourceLineNumber() >= 0) {sb.append(record.getSourceLineNumber());}
                else {sb.append("?");}
                break;

            case "%source": appendSource(sb, record); break;
            case "%msg": sb.append(record.getMessage()); break;
            case "%ex": appendStackTrace(sb, record.getThrowable()); break;
            case "%n": sb.append(System.lineSeparator()); break;
            default: sb.append(token);
        }
    }

    private void appendOrUnknown(StringBuilder sb, String value) {
        sb.append(value != null ? value : "?");
    }

    private void appendSource(StringBuilder sb, LogRecord record) {
        String className = record.getSourceClassName();
        String methodName = record.getSourceMethodName();
        String fileName = record.getSourceFileName();
        int lineNumber = record.getSourceLineNumber();

        if (className == null) {
            sb.append("?");
            return;
        }

        sb.append(className);

        if (methodName != null) {
            sb.append('.').append(methodName);
        }

        if (fileName != null) {
            sb.append('(')
                    .append(fileName);

            if (lineNumber >= 0) {
                sb.append(':').append(lineNumber);
            }

            sb.append(')');
        }
    }

    private void appendStackTrace(
            StringBuilder sb,
            Throwable throwable
    ) {
        if (throwable == null) {
            return;
        }

        StringWriter writer = new StringWriter();

        throwable.printStackTrace(
                new PrintWriter(writer)
        );

        sb.append(System.lineSeparator());
        sb.append(writer);
    }
}
package io.log2jv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternFormatterTest {

    @Test
    void formatsBasicTokens() {
        PatternFormatter formatter = new PatternFormatter("[%level] %logger - %msg");
        LogRecord record = new LogRecord(Level.INFO, "com.example.App", "Hello, world", null);

        String result = formatter.format(record);

        assertEquals("[INFO] com.example.App - Hello, world", result);
    }

    @Test
    void formatsDateToken() {
        PatternFormatter formatter = new PatternFormatter("%d{yyyy}");
        LogRecord record = new LogRecord(Level.DEBUG, "x", "y", null);

        String result = formatter.format(record);

        assertTrue(result.matches("\\d{4}"), "Expected 4-digit year, got: " + result);
    }

    @Test
    void appendsStackTraceWhenThrowablePresent() {
        PatternFormatter formatter = new PatternFormatter("%msg%ex");
        LogRecord record = new LogRecord(Level.ERROR, "x", "Error", new RuntimeException("boom"));

        String result = formatter.format(record);

        assertTrue(result.contains("RuntimeException"));
        assertTrue(result.contains("boom"));
    }

    @Test
    void levelThresholdWorksCorrectly() {
        assertTrue(Level.ERROR.isEnabledFor(Level.INFO));
        assertTrue(Level.INFO.isEnabledFor(Level.INFO));
        assertEquals(false, Level.DEBUG.isEnabledFor(Level.INFO));
    }
}

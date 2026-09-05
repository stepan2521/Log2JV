package io.log2jv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileAppenderTest {

    @Test
    void rotatesWhenExceedingMaxSize(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("app.log");
        // small limit so that rotation happens quickly
        FileAppender appender = new FileAppender(
                logFile.toString(),
                new PatternFormatter("%msg%n"),
                50, // bytes
                3
        );

        for (int i = 0; i < 20; i++) {
            appender.append(new LogRecord(Level.INFO, "test", "line number " + i, null));
        }
        appender.close();

        assertTrue(Files.exists(logFile), "Main log file should exist");
        assertTrue(Files.exists(tempDir.resolve("app.log.1")), "At least one backup copy should appear");
    }
}

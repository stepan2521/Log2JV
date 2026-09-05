package io.log2jv;

/**
 * Logger settings. Default values are chosen so that the logger works
 * out of the box (console output at INFO level) even when no
 * configuration file is found.
 */
public class Config {

    public Level level = Level.INFO;

    public boolean consoleEnabled = true;
    public boolean consoleColors = true;
    public String consolePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] %source - %msg%n";

    public boolean fileEnabled = false;
    public String filePath = "logs/app.log";
    public String filePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] %logger - %msg%ex%n";
    public long fileMaxSizeBytes = 10L * 1024 * 1024; // 10 MB
    public int fileMaxBackups = 5;

    public boolean asyncEnabled = false;
    public int asyncQueueCapacity = 1024;
}

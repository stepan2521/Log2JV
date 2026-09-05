package io.log2jv;

/**
 * Example file demonstrating usage of Log2JV.
 * Recommended to read the README.md
 */
public class Demo {
    public static void main(String[] args) {
        // Creating a Logger
        final Logger log = LogManager.getLogger(Demo.class);

        Launch(log);
    }

    private static void Launch(Logger log) {
        InfoTest(log);
        ErrorTest(log);
        DebugTest(log);
        TraceTest(log);
        WarnTest(log);
        FormattingTest(log);
        TryCatchTest(log);
        // FATAL is last: it logs and then crashes the JVM (by design)
        FatalTest(log);
    }

    /**
     * A basic log with .info().
     * Remember that it will write something like:<br>
     * <b>2026-09-05 13:28:07.403 [INFO] io.log2jv.Demo.InfoTest(Demo.java:30) - Hello!</b><br>
     * Demo.java:30 is clickable (in IntelliJ IDEA).
     */
    private static void InfoTest(Logger log) {
        log.info("Demo Info");
        log.info("Hello from ${}!", "Log2JV");
        log.info("Cost is $${} dollars", 5);
    }

    /**
     * Error will not crash your program.
     * But it is good to place logging of exceptions inside try-catch blocks.
     */
    private static void ErrorTest(Logger log) {
        log.error("Demo Error");
        log.error("Something went wrong with code ${}", 42);
    }

    /**
     * FATAL is the most severe level (above ERROR).
     * After writing the message it throws an unchecked exception
     * and terminates the application.
     */
    private static void FatalTest(Logger log) {
        log.fatal("Demo Fatal — this will crash the program");
        // The line below is never reached
        log.fatal("Critical failure in module ${}", "core");
    }

    private static void DebugTest(Logger log) {
        log.debug("Demo Debug");
        log.debug("User id = ${}, session = ${}", 1001, "abc-xyz");
    }

    private static void TraceTest(Logger log) {
        log.trace("Demo Trace");
    }

    private static void WarnTest(Logger log) {
        log.warn("Demo Warn");
        log.warn("Disk usage is at ${}%", 92);
    }

    /**
     * Demonstrates placeholders, arrays, Pair and dollar escaping.
     */
    private static void FormattingTest(Logger log) {
        log.info("Array: ${}", new int[]{10, 20, 30});
        log.info("Pair: ${}", Pair.of("timeout", 5000));
        log.info("Nested: ${}", (Object) new String[][]{{"a", "b"}, {"c", "d"}});
        log.info("Just a dollar sign: $$");
        log.info("Price tag: $$${}$$", 19);
    }

    /**
     * Shows that formatting errors (wrong number of arguments)
     * can be caught and logged via log.error(..., throwable).
     */
    private static void TryCatchTest(Logger log) {
        try {
            // Will throw "Too few arguments"
            log.info("A message from ${} ${}", "try/catch");
        } catch (Exception e) {
            // We can still handle the exception through log.error()
            log.error("Caught an exception while formatting", e);
        }
    }
}

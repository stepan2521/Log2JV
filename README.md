# Log2JV - Log To Java

Simple, zero-dependency Java logging library inspired by the log4j2 architecture
(levels, appenders, formatters, file-based configuration, asynchronous writing,
file rotation).

## JDK Compatibility 

The library is compiled with the `--release 11` flag (see `build.gradle.kts`).
This means:

- The resulting `.jar` can be used in projects running on **Java 11 and any newer
  JDK** (17, 21, 25, etc.) — the JVM is backward-compatible with older bytecode.
- You can build the library with any installed JDK (21, 25, …). The `--release`
  flag makes the compiler verify that you do not use any API introduced after
  Java 11, regardless of which JDK you compile with.
- The opposite is **not** true: a `.jar` built for Java 17+ will not run on a
  JVM 11. The lower the target, the wider the audience that can use the library.

If later we need newer language features (records, sealed classes, virtual
threads, etc.), we can raise the target — but then users on older JDKs will
lose the ability to use the library.

## Project Structure

```
log2jv/
├── build.gradle.kts              # build, target Java 11
├── settings.gradle.kts
├── src/main/java/io/log2jv/
│   ├── Level.java                # TRACE .. OFF
│   ├── LogRecord.java            # immutable log event
│   ├── Formatter.java            # formatter interface
│   ├── PatternFormatter.java     # patterns like %d{...} %level %logger %msg %n %ex
│   ├── Appender.java             # “where to write” interface
│   ├── ConsoleAppender.java
│   ├── FileAppender.java         # file writing + size-based rotation
│   ├── AsyncAppender.java        # decorator: asynchronous writing via a queue
│   ├── Config.java               # default configuration values
│   ├── ConfigLoader.java         # reading .properties (system property / classpath)
│   ├── Logger.java               # public interface for application code
│   ├── LoggerImpl.java           # implementation (package-private)
│   ├── LogManager.java           # entry point: LogManager.getLogger(...)
│   ├── MessageFormatter.java     # ${} message placeholders
│   ├── Pair.java                 # simple key-value pair helper
│   └── UncheckedLoggerException.java
├── src/test/java/...             # JUnit tests + Demo.java with main()
├── src/test/resources/log2jv.properties  # config for running the Demo
└── examples/log2jv.properties    # config template for library users
```

## How it works (briefly)

1. On the first access `LogManager` loads the configuration once via
   `ConfigLoader` and builds the list of appenders (`ConsoleAppender`,
   `FileAppender`, optionally wrapped in `AsyncAppender`).
2. `LogManager.getLogger(...)` returns a `LoggerImpl` that shares the same
   appender list and the level threshold from the configuration.
3. When you call `log.info(...)` a `LogRecord` is created and sent to every
   appender. Each appender formats the record with its own `Formatter` and
   decides where to write it (stdout/stderr, a file on disk).
4. `FileAppender` monitors the file size and, when the limit is exceeded,
   renames `app.log → app.log.1 → app.log.2 → …`.
5. `AsyncAppender` simply puts the `LogRecord` into a `BlockingQueue` and
   performs the real write on a background daemon thread — the caller never
   waits for I/O.

## Building

You need Gradle installed (or use the included wrapper):

```bash
./gradlew build          # compile + tests + jar
./gradlew test           # tests only
./gradlew printJarPath   # prints the path to the finished .jar
```

If you do not have the Gradle Wrapper yet, run once (with internet access):

```bash
gradle wrapper --gradle-version 8.7
```

This creates `gradlew`, `gradlew.bat` and `gradle/wrapper/*`, after which you
can build with `./gradlew build` without a system-wide Gradle installation.

The finished artifact appears in `build/libs/log2jv-0.1.0.jar`.

## Using in another project

### 1. Add the jar

Gradle (local file — the simplest way to start):

```kotlin
dependencies {
    implementation(files("libs/log2jv-0.1.0.jar"))
}
```

Maven:

```xml
<dependency>
    <groupId>io.log2jv</groupId>
    <artifactId>log2jv</artifactId>
    <version>0.1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/log2jv-0.1.0.jar</systemPath>
</dependency>
```

(For proper reuse across several projects it is better to publish the jar to a
local or corporate Maven repository, but a file dependency is fine for the
first version.)

### 2. Place the configuration

Copy `examples/log2jv.properties` into `src/main/resources/log2jv.properties`
of your project and adjust it to your needs (see comments in the file).

### 3. Use in code

```java
import io.log2jv.Logger;
import io.log2jv.LogManager;

public class MyService {
    private static final Logger log = LogManager.getLogger(MyService.class);

    public void doWork() {
        log.info("Processing started");
        try {
            // ...
        } catch (Exception e) {
            log.error("Processing failed", e);
        }
    }
}
```

## Configuration parameters

| Key                     | Default        | Description                                      |
|-------------------------|----------------|--------------------------------------------------|
| `logger.level`          | `INFO`         | `TRACE` / `DEBUG` / `INFO` / `WARN` / `ERROR` / `FATAL` / `OFF` |
| `console.enabled`       | `true`         | Enable console output                            |
| `console.pattern`       | see `Config`   | Formatting pattern for the console               |
| `console.colors`        | `true`         | Enable ANSI colors on the console                |
| `file.enabled`          | `false`        | Enable writing to a file                         |
| `file.path`             | `logs/app.log` | Path to the log file                             |
| `file.pattern`          | see `Config`   | Formatting pattern for the file                  |
| `file.maxSizeBytes`     | `10485760`     | Maximum file size before rotation (bytes)        |
| `file.maxBackups`       | `5`            | How many old copies to keep                      |
| `async.enabled`         | `false`        | Asynchronous writing via a background thread     |
| `async.queueCapacity`   | `1024`         | Queue size for asynchronous writing              |

Pattern tokens: `%d` / `%d{date-pattern}`, `%level`, `%logger`, `%thread`,
`%msg`, `%ex` (exception stack trace), `%n` (line separator),
`%source` (Class.method(File.java:line)), `%class`, `%method`, `%file`, `%line`.

## Message formatting and colors

Log2JV supports Java varargs (no Kotlin needed; Java 11 has varargs):

```java
Logger log = LogManager.getLogger(MyClass.class);

log.info("Application ${} started on JDK ${}", "Log2JV", 11);
log.info("Array: ${}; parameter: ${}",
        new int[]{0, 5, 2, 4, 6},
        new Pair<>("Name", 5));
log.info("Cost is $${} dollars", 5);   // -> "Cost is $5 dollars"
log.info("$$ is a dollar literal");    // -> "$ is a dollar literal"
log.fatal("Unrecoverable error in ${}", "payment-service");  // logs + crashes
```

- `${}` — placeholder for the next argument (any type). Arrays and `Pair`
  receive nice formatting automatically.
- `$$` — literal dollar sign `$`.
- `$${}` — literal `$` followed by a placeholder (e.g. price tags).

The number of placeholders is checked at runtime. Errors of the form
"expected 3, but got 2" cannot be prevented at compile time with ordinary
Java varargs APIs.

Colors are applied only by `ConsoleAppender`:

- FATAL — bold magenta
- ERROR — bold red
- WARN  — bold yellow
- INFO  — italic white
- DEBUG — bold white
- TRACE — italic gray

ERROR and FATAL messages go to stderr; everything else goes to stdout.
Calling any `fatal(...)` method logs the message and then throws, crashing the process.

To disable ANSI colors:

```properties
console.colors=false
```

## Changelog

### 0.1.0
The **Log2JV** library was just released! There are warns, errors, info and more.
Unfortunately we did not save the progress that was made.

### 0.2.0
Remade the logging, added colors.

### 0.3.0
Now you can click on the `Class:line` in a log message to jump to that line!
(This may not work in some IDEs.)

### 0.4.0
Output is now UTF-8, so more languages are supported.

### 0.4.1
Updated the Demo file and added the changelog to README.md.

### 0.5.0
Replaced the old `%s` / `%d` / `%f` / ... message placeholders with the clearer
`${}` syntax. Literal dollar signs are written as `$$`. All documentation and
Javadoc translated to English.

### 0.6.0
- Added **FATAL** logging level and corresponding `fatal(...)` methods
  (severity above ERROR). FATAL messages are written to stderr and coloured
  in bold magenta.
- Fixed parsing of `$${}` sequences (literal `$` + placeholder).
- Expanded Demo with more examples and a try/catch demonstration.

### 0.6.1
- **FATAL now terminates the application**: after the message is written,
an unchecked exception is thrown (the original throwable is rethrown when
provided).
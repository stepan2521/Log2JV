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
  flag makes the compiler check that you’re not using any API introduced after
  Java 11, no matter which JDK you’re compiling with.
- The opposite is not true: a `.jar` built for Java 17+ will not run on JVM 11.
  The lower the target, the wider the audience that can use the library.

If later we need newer language features (records, sealed classes, virtual
threads, etc.), we can raise the target — but then some users on older JDKs will
lose the ability to use the library.

## Project Structure

```text
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

The finished artifact appears in:

```text
build/libs/log2jv-0.6.2.jar
```

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.stepanmail1999-ctrl:log2jv:0.6.2")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.stepanmail1999-ctrl:log2jv:0.6.2'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.stepanmail1999-ctrl</groupId>
    <artifactId>log2jv</artifactId>
    <version>0.6.2</version>
</dependency>
```

> If the artifact is not yet available on Maven Central, use a jar from
> [GitHub Releases](https://github.com/stepanmail1999-ctrl/Log2JV/releases)
> or JitPack:
>
> ```kotlin
> repositories {
>     mavenCentral()
>     maven("https://jitpack.io")
> }
>
> dependencies {
>     implementation("com.github.stepanmail1999-ctrl:Log2JV:v0.6.2")
> }
> ```

### Local jar (optional)

```kotlin
dependencies {
    implementation(files("libs/log2jv-0.6.2.jar"))
}
```

## Configuration

Copy `examples/log2jv.properties` into `src/main/resources/log2jv.properties`
of your project and adjust it to your needs.

| Key                     | Default        | Description |
|-------------------------|----------------|-------------|
| `logger.level`          | `INFO`         | `TRACE` / `DEBUG` / `INFO` / `WARN` / `ERROR` / `FATAL` / `OFF` |
| `console.enabled`       | `true`         | Enable console output |
| `console.pattern`       | see `Config`   | Formatting pattern for the console |
| `console.colors`        | `true`         | Enable ANSI colors on the console |
| `file.enabled`          | `false`        | Enable writing to a file |
| `file.path`             | `logs/app.log` | Path to the log file |
| `file.pattern`          | see `Config`   | Formatting pattern for the file |
| `file.maxSizeBytes`     | `10485760`     | Maximum file size before rotation (bytes) |
| `file.maxBackups`       | `5`            | How many old copies to keep |
| `async.enabled`         | `false`        | Asynchronous writing via a background thread |
| `async.queueCapacity`   | `1024`         | Queue size for asynchronous writing |

Pattern tokens: `%d` / `%d{date-pattern}`, `%level`, `%logger`, `%thread`,
`%msg`, `%ex` (exception stack trace), `%n` (line separator),
`%source` (Class.method(File.java:line)), `%class`, `%method`, `%file`, `%line`.

## Usage

```java
import io.log2jv.Logger;
import io.log2jv.LogManager;
import io.log2jv.Pair;

public class MyService {
    private static final Logger log = LogManager.getLogger(MyService.class);

    public void doWork() {
        log.info("Processing started");
        log.info("User ${} connected from ${}", "Stepan", "127.0.0.1");
        log.info("Array: ${}; pair: ${}", new int[]{1, 2, 3}, Pair.of("timeout", 5000));
        log.info("Cost is $${} dollars", 5);   // -> "Cost is $5 dollars"
        log.info("$$ is a dollar literal");    // -> "$ is a dollar literal"

        try {
            // ...
        } catch (Exception e) {
            log.error("Processing failed", e);
        }
    }
}
```

### Message placeholders

- `${}` — next argument (any type). Arrays and `Pair` are formatted automatically.
- `$$` — literal dollar sign `$`.
- `$${}` — literal `$` followed by a placeholder.

The number of placeholders is checked at runtime.

### Colors

Applied only by `ConsoleAppender`:

- FATAL — bold magenta
- ERROR — bold red
- WARN  — bold yellow
- INFO  — italic white
- DEBUG — bold white
- TRACE — italic gray

ERROR and FATAL go to stderr; everything else goes to stdout.  
Calling any `fatal(...)` method logs the message and then throws, terminating the process.

Disable ANSI colors:

```properties
console.colors=false
```

## Changelog

See [Releases](https://github.com/stepanmail1999-ctrl/Log2JV/releases).

package io.log2jv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class ConfigLoader {

    private static final String DEFAULT_RESOURCE =
            "log2jv.properties";

    private ConfigLoader() {
    }

    public static Config load() {
        Properties props = new Properties();

        boolean loaded =
                tryLoadFromSystemProperty(props)
                        || tryLoadFromClasspath(props);

        if (!loaded) {
            System.err.println(
                    "log2jv: configuration not found, "
                            + "using defaults "
                            + "(INFO, console)"
            );
        }

        return fromProperties(props);
    }

    private static boolean tryLoadFromSystemProperty(
            Properties props
    ) {
        String path =
                System.getProperty("log2jv.config");

        if (path == null) {
            return false;
        }

        try (InputStream in =
                     Files.newInputStream(Path.of(path))) {

            loadUtf8(props, in);
            return true;

        } catch (IOException e) {
            System.err.println(
                    "log2jv: failed to read "
                            + path
                            + ": "
                            + e.getMessage()
            );

            return false;
        }
    }

    private static boolean tryLoadFromClasspath(
            Properties props
    ) {
        try (InputStream in =
                     ConfigLoader.class
                             .getClassLoader()
                             .getResourceAsStream(
                                     DEFAULT_RESOURCE
                             )) {

            if (in == null) {
                return false;
            }

            loadUtf8(props, in);
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private static void loadUtf8(
            Properties props,
            InputStream in
    ) throws IOException {
        try (InputStreamReader reader =
                     new InputStreamReader(
                             in,
                             StandardCharsets.UTF_8
                     )) {

            props.load(reader);
        }
    }

    private static Config fromProperties(
            Properties p
    ) {
        Config c = new Config();

        c.level = Level.valueOf(
                p.getProperty(
                        "logger.level",
                        c.level.name()
                ).toUpperCase(Locale.ROOT)
        );

        c.consoleEnabled =
                parseBoolean(
                        p,
                        "console.enabled",
                        c.consoleEnabled
                );

        c.consoleColors =
                parseBoolean(
                        p,
                        "console.colors",
                        c.consoleColors
                );

        c.consolePattern =
                p.getProperty(
                        "console.pattern",
                        c.consolePattern
                );

        c.fileEnabled =
                parseBoolean(
                        p,
                        "file.enabled",
                        c.fileEnabled
                );

        c.filePath =
                p.getProperty(
                        "file.path",
                        c.filePath
                );

        c.filePattern =
                p.getProperty(
                        "file.pattern",
                        c.filePattern
                );

        c.fileMaxSizeBytes =
                parseLong(
                        p,
                        "file.maxSizeBytes",
                        c.fileMaxSizeBytes
                );

        c.fileMaxBackups =
                parseInt(
                        p,
                        "file.maxBackups",
                        c.fileMaxBackups
                );

        c.asyncEnabled =
                parseBoolean(
                        p,
                        "async.enabled",
                        c.asyncEnabled
                );

        c.asyncQueueCapacity =
                parseInt(
                        p,
                        "async.queueCapacity",
                        c.asyncQueueCapacity
                );

        return c;
    }

    private static boolean parseBoolean(
            Properties p,
            String key,
            boolean def
    ) {
        return Boolean.parseBoolean(
                p.getProperty(
                        key,
                        String.valueOf(def)
                )
        );
    }

    private static long parseLong(
            Properties p,
            String key,
            long def
    ) {
        try {
            return Long.parseLong(
                    p.getProperty(
                            key,
                            String.valueOf(def)
                    )
            );
        } catch (NumberFormatException e) {
            System.err.println(
                    "log2jv: invalid number for "
                            + key
                            + ", using "
                            + def
            );

            return def;
        }
    }

    private static int parseInt(
            Properties p,
            String key,
            int def
    ) {
        try {
            return Integer.parseInt(
                    p.getProperty(
                            key,
                            String.valueOf(def)
                    )
            );
        } catch (NumberFormatException e) {
            System.err.println(
                    "log2jv: invalid number for "
                            + key
                            + ", using "
                            + def
            );

            return def;
        }
    }
}
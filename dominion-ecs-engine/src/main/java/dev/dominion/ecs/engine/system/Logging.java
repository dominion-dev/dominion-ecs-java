/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

public final class Logging {
    public static final String PROJECT_PROPERTIES = "dominion-ecs-java/project.properties";
    public static final String REVISION = "revision";
    public static final String DEFAULT_LOGGER = "util.logging";
    public static final System.Logger.Level DEFAULT_LOGGING_LEVEL = System.Logger.Level.INFO;

    private static final java.util.logging.Level[] spi2JulLevelMapping = {
            java.util.logging.Level.ALL,     // mapped from ALL
            java.util.logging.Level.FINER,   // mapped from TRACE
            java.util.logging.Level.FINE,    // mapped from DEBUG
            java.util.logging.Level.INFO,    // mapped from INFO
            java.util.logging.Level.WARNING, // mapped from WARNING
            java.util.logging.Level.SEVERE,  // mapped from ERROR
            java.util.logging.Level.OFF      // mapped from OFF
    };
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);
    private static final AtomicInteger levelIdx = new AtomicInteger(0);
    private static final System.Logger.Level[] levels = new System.Logger.Level[1 << 10];
    private static int rootLevelOrdinal;

    static {
        try {
            String version = fetchPomVersion();
            var level = setupDefaultLoggingLibrary();
            System.Logger.Level rootLevel = level.orElse(DEFAULT_LOGGING_LEVEL);
            rootLevelOrdinal = rootLevel.ordinal();
            registerLoggingLevel(rootLevel);
            if (Config.showBanner()) {
                showBanner(version, rootLevel, level.isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static System.Logger getLogger() {
        return System.getLogger(Config.DOMINION_ + STACK_WALKER.getCallerClass().getSimpleName());
    }

    public static int registerLoggingLevel(System.Logger.Level level) {
        int idx = levelIdx.getAndIncrement();
        levels[idx] = level;
        return idx;
    }

    public static boolean isLoggable(int idx, System.Logger.Level levelToCheck) {
        return !(levelToCheck.ordinal() < rootLevelOrdinal
                || idx < 0
                || levelToCheck.ordinal() < levels[idx].ordinal()
                || levels[idx] == System.Logger.Level.OFF);
    }

    public static String format(String subject, String message) {
        return "[" + subject + "] - " + message;
    }

    public static void printPanel(String... rows) {
        System.out.println("---");
        for (String row : rows) {
            if (row != null) {
                System.out.println(". " + row);
            }
        }
        System.out.println("---\n");
    }

    private static boolean isDefaultLogger() {
        return System.getLogger("").getClass().getName().contains(DEFAULT_LOGGER);
    }

    private static void showBanner(String version, System.Logger.Level level, boolean isDefaultLoggingLevel) {
        System.out.println("\n|) () |\\/| | |\\| | () |\\|\n");
        System.out.printf("%25s%n", "ECS v" + version);
        System.out.println();
        printPanel(
                "Root Logging System"
                , "  Logging-Level: '" + level + "'"
                , isDefaultLoggingLevel ? "  Change the root level with the sys-property '"
                        + Config.getPropertyName(Config.LOGGING_LEVEL) + "'." : null
                , isDefaultLogger() ?
                        "  Dominion is compatible with all logging systems that support the " +
                                "'System.Logger' Platform Logging API and Service (JEP 264)." : null
        );
    }

    private static String fetchPomVersion() throws IOException {
        InputStream is = Logging.class.getClassLoader()
                .getResourceAsStream(PROJECT_PROPERTIES);
        if (is == null) return "?";
        Properties properties = new Properties();
        properties.load(is);
        return properties.getProperty(REVISION);
    }

    private static Optional<System.Logger.Level> setupDefaultLoggingLibrary() {
        var level = Config.fetchLoggingLevel("");
        System.setProperty("java.util.logging.SimpleFormatter.format"
                , (Config.logCaller() ? "[%2$s] " : "") + "%4$4.4s %3$32.32s %5$s %6$s %n");
        Logger dominionRootLogger = Logger.getLogger(Config.DOMINION_);
        java.util.logging.Level julLevel =
                spi2JulLevelMapping[level.orElse(DEFAULT_LOGGING_LEVEL).ordinal()];
        dominionRootLogger.setLevel(julLevel);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(julLevel);
        dominionRootLogger.addHandler(consoleHandler);
        return level;
    }

    public record Context(String subject, int levelIndex) {
        public static Context TEST = new Context("test", 0);
        public static Context STRESS_TEST = new Context("stress-test", -1);
    }
}

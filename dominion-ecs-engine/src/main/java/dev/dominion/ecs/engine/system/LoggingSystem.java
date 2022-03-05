/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

public final class LoggingSystem {
    public static final String DOMINION_LOGGING_LEVEL = "dominion.logging.level";
    public static final String DOMINION_LOGGING_LOG_CALLER = "dominion.logging.log-caller";
    public static final String DOMINION_SHOW_BANNER = "dominion.show-banner";
    public static final String POM_PROPERTIES = "from-pom.properties";
    public static final String REVISION = "revision";
    public static final String DEFAULT_LOGGER = "util.logging";
    public static final String DOMINION = "dominion.";

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
    private static System.Logger.Level level = System.Logger.Level.INFO;

    static {
        try {
            String version = fetchPomVersion();
            level = setupDefaultLoggingSystem();
            showBanner(version, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static System.Logger getLogger() {
        return System.getLogger(DOMINION + STACK_WALKER.getCallerClass().getSimpleName());
    }

    private static boolean isDefaultLogger() {
        return System.getLogger("").getClass().getName().contains(DEFAULT_LOGGER);
    }

    private static void showBanner(String version, System.Logger.Level level) {
        String showBanner = System.getProperty(DOMINION_SHOW_BANNER);
        System.out.println("showBanner = " + showBanner);
        System.out.println("showBanner = " + System.getenv(DOMINION_SHOW_BANNER));
        if (showBanner != null && showBanner.equals("false")) return;
        System.out.println("\n|) () |\\/| | |\\| | () |\\|\n");
        System.out.printf("%25s%n", "ECS v" + version);
        System.out.println();
        System.out.println("|" + String.format("%-70s", " Default Logging Level: '" + level + "'") + "|");
        System.out.println("|" + String.format("%-70s", "  You can change it by setting the system-property '" + DOMINION_LOGGING_LEVEL + "' or") + "|");
        if (isDefaultLogger()) {
            System.out.println("|" + String.format("%-70s", "  You can configure another logging system that supports 'System.Logger'") + "|\n");
        }
    }

    private static String fetchPomVersion() throws IOException {
        InputStream is = LoggingSystem.class.getClassLoader()
                .getResourceAsStream(POM_PROPERTIES);
        Properties properties = new Properties();
        properties.load(is);
        return properties.getProperty(REVISION);
    }

    private static System.Logger.Level setupDefaultLoggingSystem() {
        String levelStr = System.getProperty(DOMINION_LOGGING_LEVEL);
        level = levelStr != null ? System.Logger.Level.valueOf(levelStr) : level;
        String callerStr = System.getProperty(DOMINION_LOGGING_LOG_CALLER);
        boolean logCaller = callerStr != null && callerStr.equals("true");
        System.setProperty("java.util.logging.SimpleFormatter.format"
                , (logCaller ? "[%2$s] " : "") + "%4$4.4s %3$s - %5$s %6$s %n");
        Logger root = Logger.getLogger("");
        java.util.logging.Level julLevel = spi2JulLevelMapping[level.ordinal()];
        root.setLevel(julLevel);
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(julLevel);
        }
        return level;
    }

    public static boolean isLoggable(System.Logger.Level levelToCheck) {
        return !(levelToCheck.ordinal() < level.ordinal() || level == System.Logger.Level.OFF);
    }
}

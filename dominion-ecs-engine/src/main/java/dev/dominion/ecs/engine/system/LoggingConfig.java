/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

public class LoggingConfig {
    public static final String DOMINION_DEFAULT_LOGGING_LEVEL = "dominion.default.logging.level";
    public static final String POM_PROPERTIES = "from-pom.properties";
    public static final String REVISION = "revision";
    public static final String DEFAULT_LOGGER = "util.logging";

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);

    static {
        try {
            String version = fetchPomVersion();
            Level level = setupDefaultLoggingSystem();
            showBanner(version, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static System.Logger getLogger() {
        return System.getLogger(STACK_WALKER.getCallerClass().getSimpleName());
    }

    private static boolean isDefaultLogger() {
        return System.getLogger("").getClass().getName().contains(DEFAULT_LOGGER);
    }

    private static void showBanner(String version, Level level) {
        System.out.println("\n|) () |\\/| | |\\| | () |\\|\n");
        System.out.printf("%25s%n", "ECS v" + version);
//        System.out.println("ECS Engine v" + version);
        System.out.println();
        if (isDefaultLogger()) {
            System.out.println("|" + String.format("%-75s", " Default Logging Level: '" + level + "'") + "|");
            System.out.println("|" + String.format("%-75s", "  To modify, set the system-property '" + DOMINION_DEFAULT_LOGGING_LEVEL + "' or") + "|");
            System.out.println("|" + String.format("%-75s", "  configure another logging system that supports 'System.Logger' (JEP 264)") + "|\n");
        }
    }

    private static String fetchPomVersion() throws IOException {
        InputStream is = LoggingConfig.class.getClassLoader()
                .getResourceAsStream(POM_PROPERTIES);
        Properties properties = new Properties();
        properties.load(is);
        return properties.getProperty(REVISION);
    }

    private static Level setupDefaultLoggingSystem() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%2$s] %4$4.4s %3$s - %5$s %6$s %n");
        Level level = Level.INFO;
        String levelStr = System.getProperty(DOMINION_DEFAULT_LOGGING_LEVEL);
        level = levelStr != null ? Level.parse(levelStr) : level;
        Logger root = Logger.getLogger("");
        root.setLevel(level);
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(level);
        }
        return level;
    }
}

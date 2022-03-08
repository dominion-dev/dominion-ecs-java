/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

public final class ConfigSystem {
    public static final String DOMINION_SHOW_BANNER = "dominion.show-banner";
    public static final String DOMINION_LOGGING_LEVEL = "dominion.logging.level";
    public static final String DOMINION_LOGGING_LOG_CALLER = "dominion.logging.log-caller";
    public static final String DOMINION_CONFIG_ = "dominion.config.";
    public static final String _CLASS_INDEX_BIT = ".class-index-bit";
    public static final int DEFAULT_CLASS_INDEX_BIT = 20;
    public static final String _CHUNK_BIT = ".chunk-bit";
    public static final int DEFAULT_CHUNK_BIT = 14;

    public static boolean showBanner() {
        String showBanner = System.getProperty(DOMINION_SHOW_BANNER);
        return showBanner == null || !showBanner.equals("false");
    }

    public static System.Logger.Level fetchLoggingLevel() {
        String levelStr = System.getProperty(DOMINION_LOGGING_LEVEL);
        return levelStr != null ? System.Logger.Level.valueOf(levelStr) : System.Logger.Level.INFO;
    }

    public static boolean logCaller() {
        String callerStr = System.getProperty(DOMINION_LOGGING_LOG_CALLER);
        return callerStr != null && callerStr.equals("true");
    }

    public static int fetchClassIndexBit(String dominionName) {
        String valueStr = System.getProperty(DOMINION_CONFIG_ + dominionName + _CLASS_INDEX_BIT);
        return valueStr != null ? Integer.parseInt(valueStr) : DEFAULT_CLASS_INDEX_BIT;
    }

    public static int fetchChunkBit(String dominionName) {
        String valueStr = System.getProperty(DOMINION_CONFIG_ + dominionName + _CHUNK_BIT);
        return valueStr != null ? Integer.parseInt(valueStr) : DEFAULT_CHUNK_BIT;
    }
}

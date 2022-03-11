/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.system;

import java.util.Optional;

public final class ConfigSystem {
    public static final String DOMINION_ = "dominion.";
    public static final String SHOW_BANNER = "show-banner";
    public static final String LOGGING_LEVEL = "logging-level";
    public static final String LOGGING_CALLER = "logging-caller";
    public static final String CLASS_INDEX_BIT = "class-index-bit";
    public static final String CHUNK_BIT = "chunk-bit";
    public static final int DEFAULT_CLASS_INDEX_BIT = 20;
    public static final int DEFAULT_CHUNK_BIT = 14;

    public static boolean showBanner() {
        String showBanner = System.getProperty(getPropertyName(SHOW_BANNER));
        return showBanner == null || !showBanner.equals("false");
    }

    public static Optional<System.Logger.Level> fetchLoggingLevel(String name) {
        String levelStr = System.getProperty(getPropertyName(name, LOGGING_LEVEL));
        return Optional.ofNullable(levelStr != null ? System.Logger.Level.valueOf(levelStr) : null);
    }

    public static boolean logCaller() {
        String callerStr = System.getProperty(getPropertyName(LOGGING_CALLER));
        return callerStr != null && callerStr.equals("true");
    }

    public static Optional<Integer> fetchClassIndexBit(String dominionName) {
        String valueStr = System.getProperty(getPropertyName(dominionName, CLASS_INDEX_BIT));
        return Optional.ofNullable(valueStr != null ? Integer.parseInt(valueStr) : null);
    }

    public static Optional<Integer> fetchChunkBit(String dominionName) {
        String valueStr = System.getProperty(getPropertyName(dominionName, CHUNK_BIT));
        return Optional.ofNullable(valueStr != null ? Integer.parseInt(valueStr) : null);
    }

    public static String getPropertyName(String key) {
        return getPropertyName("", key);
    }

    public static String getPropertyName(String dominionName, String key) {
        return DOMINION_ + dominionName + (dominionName.isEmpty() ? "" : ".") + key;
    }
}

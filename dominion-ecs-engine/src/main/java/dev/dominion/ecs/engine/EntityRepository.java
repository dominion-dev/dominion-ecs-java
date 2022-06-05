/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.*;
import dev.dominion.ecs.api.Results.*;
import dev.dominion.ecs.engine.CompositionRepository.Node;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class EntityRepository implements Dominion {
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final String name;
    private final LoggingSystem.Context loggingContext;
    private final CompositionRepository compositions;
    private final int systemTimeoutSeconds;

    public EntityRepository(String name, int classIndexBit, int chunkBit, int chunkCountBit, int systemTimeoutSeconds,
                            LoggingSystem.Context loggingContext) {
        this.name = name;
        this.systemTimeoutSeconds = systemTimeoutSeconds;
        this.loggingContext = loggingContext;
        compositions = new CompositionRepository(classIndexBit, chunkBit, chunkCountBit, loggingContext);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Entity createEntity(Object... components) {
        return createEntity(null, components);
    }

    @Override
    public Entity createEntityAs(Entity prefab, Object... components) {
        return createEntityAs(null, prefab, components);
    }

    @Override
    public Entity createEntity(String name, Object... components) {
        Object[] componentArray = components.length == 0 ? null : components;
        DataComposition composition = compositions.getOrCreate(componentArray);
        IntEntity entity = composition.createEntity(name, componentArray);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + entity + " with " + composition)
            );
        }
        return entity;
    }

    @Override
    public Entity createPreparedEntity(String name, Composition.Of with) {
        return null;
    }

    @Override
    public Entity createEntityAs(String name, Entity prefab, Object... components) {
        IntEntity origin = (IntEntity) prefab;
        Object[] originComponents = origin.getComponents();
        if (originComponents == null || originComponents.length == 0) {
            return createEntity(name, components);
        }
        Object[] targetComponents = Arrays.copyOf(originComponents, originComponents.length + components.length);
        System.arraycopy(components, 0, targetComponents, originComponents.length, components.length);
        return createEntity(name, targetComponents);
    }

    @Override
    public boolean deleteEntity(Entity entity) {
        return ((IntEntity) entity).delete();
    }

    @Override
    public Scheduler createScheduler() {
        return new SystemScheduler(systemTimeoutSeconds, loggingContext);
    }

    @Override
    public <T> Results<With1<T>> findEntitiesWith(Class<T> type) {
        Map<IndexKey, Node> nodes = compositions.findWith(type);
        return new ResultSet.With1<>(compositions, nodes, type);
    }

    @Override
    public <T1, T2> Results<With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2) {
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2);
        return new ResultSet.With2<>(compositions, nodes, type1, type2);
    }

    @Override
    public <T1, T2, T3> Results<With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3);
        return new ResultSet.With3<>(compositions, nodes, type1, type2, type3);
    }

    @Override
    public <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4);
        return new ResultSet.With4<>(compositions, nodes, type1, type2, type3, type4);
    }

    @Override
    public <T1, T2, T3, T4, T5> Results<With5<T1, T2, T3, T4, T5>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4, type5);
        return new ResultSet.With5<>(compositions, nodes, type1, type2, type3, type4, type5);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> Results<With6<T1, T2, T3, T4, T5, T6>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4, type5, type6);
        return new ResultSet.With6<>(compositions, nodes, type1, type2, type3, type4, type5, type6);
    }

    @Override
    public void close() {
        compositions.close();
    }

    public static class Factory implements Dominion.Factory {

        public static final int NAME_MAX_LENGTH = 48;
        private static final AtomicInteger counter = new AtomicInteger(1);

        private static String normalizeName(String name) {
            name = name == null || name.isEmpty() ? "dominion-" + counter.getAndIncrement() : name;
            name = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9-_.]", "");

            return name.substring(0, Math.min(NAME_MAX_LENGTH, name.length()));
        }

        @Override
        public Dominion create() {
            return create(null);
        }

        @Override
        public Dominion create(String name) {
            name = normalizeName(name);
            Optional<System.Logger.Level> fetchLoggingLevel = ConfigSystem.fetchLoggingLevel(name);
            System.Logger.Level level = fetchLoggingLevel.orElse(LoggingSystem.DEFAULT_LOGGING_LEVEL);
            Optional<Integer> fetchClassIndexBit = ConfigSystem.fetchIntValue(name, ConfigSystem.CLASS_INDEX_BIT);
            int classIndexBit = fetchClassIndexBit.orElse(ConfigSystem.DEFAULT_CLASS_INDEX_BIT);
            Optional<Integer> fetchChunkBit = ConfigSystem.fetchIntValue(name, ConfigSystem.CHUNK_BIT);
            int chunkBit = fetchChunkBit.orElse(ConfigSystem.DEFAULT_CHUNK_BIT);
            Optional<Integer> fetchChunkCountBit = ConfigSystem.fetchIntValue(name, ConfigSystem.CHUNK_COUNT_BIT);
            int chunkCountBit = fetchChunkCountBit.orElse(ConfigSystem.DEFAULT_CHUNK_COUNT_BIT);
            Optional<Integer> fetchSystemTimeoutSeconds = ConfigSystem.fetchIntValue(name, ConfigSystem.SYSTEM_TIMEOUT_SECONDS);
            int systemTimeoutSeconds = fetchSystemTimeoutSeconds.orElse(ConfigSystem.DEFAULT_SYSTEM_TIMEOUT_SECONDS);

            if (ConfigSystem.showBanner()) {
                LoggingSystem.printPanel(
                        "Dominion '" + name + "'"
                        , "  Logging-Level: '" + level
                                + (fetchLoggingLevel.isEmpty() ? "' (set sys-property '"
                                + ConfigSystem.getPropertyName(name, ConfigSystem.LOGGING_LEVEL) + "')" : "'")
                        , "  ClassIndex-Bit: " + classIndexBit
                                + (fetchClassIndexBit.isEmpty() ? " (set sys-property '"
                                + ConfigSystem.getPropertyName(name, ConfigSystem.CLASS_INDEX_BIT) + "')" : "")
                        , "  Chunk-Bit: " + chunkBit
                                + (fetchChunkBit.isEmpty() ? " (set sys-property '"
                                + ConfigSystem.getPropertyName(name, ConfigSystem.CHUNK_BIT) + "')" : "")
                        , "  ChunkCount-Bit: " + chunkCountBit
                                + (fetchChunkCountBit.isEmpty() ? " (set sys-property '"
                                + ConfigSystem.getPropertyName(name, ConfigSystem.CHUNK_COUNT_BIT) + "')" : "")
                        , "  SystemTimeout-Seconds: " + systemTimeoutSeconds
                                + (fetchSystemTimeoutSeconds.isEmpty() ? " (set sys-property '"
                                + ConfigSystem.getPropertyName(name, ConfigSystem.SYSTEM_TIMEOUT_SECONDS) + "')" : "")
                );
            }

            int loggingLevelIndex = LoggingSystem.registerLoggingLevel(level);

            return new EntityRepository(name
                    , classIndexBit
                    , chunkBit
                    , chunkCountBit
                    , systemTimeoutSeconds
                    , new LoggingSystem.Context(name, loggingLevelIndex)
            );
        }
    }
}

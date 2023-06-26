/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.*;
import dev.dominion.ecs.api.Results.*;
import dev.dominion.ecs.engine.CompositionRepository.Node;
import dev.dominion.ecs.engine.system.Config;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.Logging;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class EntityRepository implements Dominion {
    private static final System.Logger LOGGER = Logging.getLogger();
    private final String name;
    private final Logging.Context loggingContext;
    private final CompositionRepository compositions;
    private final int systemTimeoutSeconds;

    private final AtomicBoolean closed = new AtomicBoolean();

    public EntityRepository(String name, int classIndexBit, int chunkBit, int systemTimeoutSeconds,
                            Logging.Context loggingContext) {
        this.name = name;
        this.systemTimeoutSeconds = systemTimeoutSeconds;
        this.loggingContext = loggingContext;
        compositions = new CompositionRepository(classIndexBit, chunkBit, loggingContext);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Entity createEntity(Object... components) {
        checkState();
        Object[] componentArray = components.length == 0 ? null : components;
        DataComposition composition = compositions.getOrCreate(componentArray);
        IntEntity entity = composition.createEntity(false, componentArray);
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Creating " + entity + " with " + composition)
            );
        }
        return entity;
    }

    @Override
    public Entity createPreparedEntity(Composition.OfTypes withValues) {
        checkState();
        DataComposition composition = (DataComposition) withValues.getContext();
        return composition.createEntity(true, withValues.getComponents());
    }

    @Override
    public Entity createEntityAs(Entity prefab, Object... components) {
        checkState();
        IntEntity origin = (IntEntity) prefab;
        int originArrayLength;
        if (origin.getComponentArray() == null || (originArrayLength = origin.getArrayLength()) == 0) {
            return createEntity(components);
        }
        Object[] targetComponents = Arrays.copyOf(components, originArrayLength + components.length);
        System.arraycopy(origin.getComponentArray(), 0, targetComponents, components.length, originArrayLength);
        return createEntity(targetComponents);
    }

    @Override
    public boolean deleteEntity(Entity entity) {
        checkState();
        return ((IntEntity) entity).delete();
    }

    @Override
    public boolean modifyEntity(Composition.Modifier modifier) {
        checkState();
        var mod = (PreparedComposition.NewEntityComposition) modifier;
        if (mod == null) {
            return false;
        }
        return mod.entity().modify(compositions, mod.targetComposition(), mod.addedComponent(), mod.addedComponents());
    }

    @Override
    public Composition composition() {
        return compositions.getPreparedComposition();
    }

    @Override
    public Scheduler createScheduler() {
        checkState();
        return new SystemScheduler(systemTimeoutSeconds, loggingContext);
    }

    @Override
    public <T> Results<T> findCompositionsWith(Class<T> type) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type);
        return new ResultSet.With<>(compositions, nodes, type);
    }

    @Override
    public <T1, T2> Results<With2<T1, T2>> findCompositionsWith(Class<T1> type1, Class<T2> type2) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2);
        return new ResultSet.With2<>(compositions, nodes, false, type1, type2);
    }

    @Override
    public <T1, T2, T3> Results<With3<T1, T2, T3>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3);
        return new ResultSet.With3<>(compositions, nodes, false, type1, type2, type3);
    }

    @Override
    public <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4);
        return new ResultSet.With4<>(compositions, nodes, false, type1, type2, type3, type4);
    }

    @Override
    public <T1, T2, T3, T4, T5> Results<With5<T1, T2, T3, T4, T5>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4, type5);
        return new ResultSet.With5<>(compositions, nodes, false, type1, type2, type3, type4, type5);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> Results<With6<T1, T2, T3, T4, T5, T6>> findCompositionsWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4, type5, type6);
        return new ResultSet.With6<>(compositions, nodes, false, type1, type2, type3, type4, type5, type6);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity> Results<T> findAllEntities() {
        checkState();
        return (Results<T>) new ResultSet.All(compositions);
    }

    @Override
    public <T> Results<With1<T>> findEntitiesWith(Class<T> type) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type);
        return new ResultSet.With1<>(compositions, nodes, type);
    }

    @Override
    public <T1, T2> Results<With2<T1, T2>> findEntitiesWith(Class<T1> type1, Class<T2> type2) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2);
        return new ResultSet.With2<>(compositions, nodes, true, type1, type2);
    }

    @Override
    public <T1, T2, T3> Results<With3<T1, T2, T3>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3);
        return new ResultSet.With3<>(compositions, nodes, true, type1, type2, type3);
    }

    @Override
    public <T1, T2, T3, T4> Results<With4<T1, T2, T3, T4>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4);
        return new ResultSet.With4<>(compositions, nodes, true, type1, type2, type3, type4);
    }

    @Override
    public <T1, T2, T3, T4, T5> Results<With5<T1, T2, T3, T4, T5>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4, type5);
        return new ResultSet.With5<>(compositions, nodes, true, type1, type2, type3, type4, type5);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> Results<With6<T1, T2, T3, T4, T5, T6>> findEntitiesWith(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        checkState();
        Map<IndexKey, Node> nodes = compositions.findWith(type1, type2, type3, type4, type5, type6);
        return new ResultSet.With6<>(compositions, nodes, true, type1, type2, type3, type4, type5, type6);
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (closed.getAndSet(true)) return;
        compositions.close();
    }

    private void checkState() {
        if (isClosed()) throw new IllegalStateException(this + " has already been closed.");
    }

    @Override
    public String toString() {
        return "Dominion '" + name + "'";
    }

    public static class Factory implements Dominion.Factory {

        public static final int NAME_MAX_LENGTH = 48;
        private static final AtomicInteger counter = new AtomicInteger(1);

        private static String normalizeName(String name) {
            name = name == null || name.isEmpty() ? "dominion-" + counter.getAndIncrement() : name;
            name = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z\\d-_.]", "");

            return name.substring(0, Math.min(NAME_MAX_LENGTH, name.length()));
        }

        @Override
        public Dominion create() {
            return create(null);
        }

        @Override
        public Dominion create(String name) {
            name = normalizeName(name);
            Optional<System.Logger.Level> fetchLoggingLevel = Config.fetchLoggingLevel(name);
            System.Logger.Level level = fetchLoggingLevel.orElse(Logging.DEFAULT_LOGGING_LEVEL);
            Optional<Config.DominionSize> fetchSize = Config.fetchSize(name);
            Config.DominionSize size = fetchSize.orElse(Config.DominionSize.MEDIUM);
            Optional<Integer> fetchClassIndexBit = Config.fetchIntValue(name, Config.CLASS_INDEX_BIT);
            int classIndexBit = fetchClassIndexBit.orElse(fetchSize.orElse(Config.DominionSize.MEDIUM).classIndexBit());
            Optional<Integer> fetchChunkBit = Config.fetchIntValue(name, Config.CHUNK_BIT);
            int chunkBit = fetchChunkBit.orElse(fetchSize.orElse(Config.DominionSize.MEDIUM).chunkBit());
            Optional<Integer> fetchSystemTimeoutSeconds = Config.fetchIntValue(name, Config.SYSTEM_TIMEOUT_SECONDS);
            int systemTimeoutSeconds = fetchSystemTimeoutSeconds.orElse(Config.DEFAULT_SYSTEM_TIMEOUT_SECONDS);
            if (Config.showBanner()) {
                Logging.printPanel(
                        "Dominion '" + name + "'"
                        , "  Logging-Level: '" + level
                                + (fetchLoggingLevel.isEmpty() ? "' (set sys-property '"
                                + Config.getPropertyName(name, Config.LOGGING_LEVEL) + "')" : "'")
                        , "  Dominion-Size: '" + size
                                + (fetchSize.isEmpty() ? "' (set sys-property '"
                                + Config.getPropertyName(name, Config.SIZE) + "')" : "'")
                        , "  ClassIndex-Bit: " + classIndexBit
                                + (fetchClassIndexBit.isEmpty() && fetchSize.isEmpty() ? " (set sys-property '"
                                + Config.getPropertyName(name, Config.CLASS_INDEX_BIT) + "')" : "")
                        , "  Chunk-Bit: " + chunkBit
                                + (fetchChunkBit.isEmpty() && fetchSize.isEmpty() ? " (set sys-property '"
                                + Config.getPropertyName(name, Config.CHUNK_BIT) + "')" : "")
                        , "  SystemTimeout-Seconds: " + systemTimeoutSeconds
                                + (fetchSystemTimeoutSeconds.isEmpty() ? " (set sys-property '"
                                + Config.getPropertyName(name, Config.SYSTEM_TIMEOUT_SECONDS) + "')" : "")
                );
                if (fetchSize.isEmpty()) {
                    Logging.printPanel(
                            Stream.concat(Stream.of("Dominion 'Size' options:"),
                                            Arrays.stream(Config.DominionSize.values()).map(Config.DominionSize::toString))
                                    .toArray(String[]::new));
                }
            }
            int loggingLevelIndex = Logging.registerLoggingLevel(level);
            return new EntityRepository(name
                    , classIndexBit
                    , chunkBit
                    , systemTimeoutSeconds
                    , new Logging.Context(name, loggingLevelIndex)
            );
        }
    }
}

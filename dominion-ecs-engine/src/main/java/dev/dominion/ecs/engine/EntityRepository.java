/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.ConfigSystem;
import dev.dominion.ecs.engine.system.HashKey;
import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class EntityRepository implements Dominion {
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final String name;
    private final LoggingSystem.Context loggingContext;
    private final CompositionRepository compositions;

    public EntityRepository(String name, int classIndexBit, int chunkBit, int chunkCountBit, LoggingSystem.Context loggingContext) {
        this.name = name;
        this.loggingContext = loggingContext;
        compositions = new CompositionRepository(classIndexBit, chunkBit, chunkCountBit, loggingContext);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Entity createEntity(Object... components) {
        Object[] componentArray = components.length == 0 ? null : components;
        Composition composition = compositions.getOrCreate(componentArray);
        IntEntity entity = composition.createEntity(componentArray);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + entity + " with " + composition)
            );
        }
        return entity;
    }

    @Override
    public Entity createEntityAs(Entity prefab, Object... components) {
        return null;
    }

    @Override
    public boolean deleteEntity(Entity entity) {
        return ((IntEntity) entity).delete();
    }

    @Override
    public <T> Results<Results.Comp1<T>> findComponents(Class<T> type) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type);
        return new Comp1Results<>(compositions.getClassIndex(), nodes, type);
    }

    @Override
    public <T1, T2> Results<Results.Comp2<T1, T2>> findComponents(Class<T1> type1, Class<T2> type2) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2);
        return new Comp2Results<>(compositions.getClassIndex(), nodes, type1, type2);
    }

    @Override
    public <T1, T2, T3> Results<Results.Comp3<T1, T2, T3>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3);
        return new Comp3Results<>(compositions.getClassIndex(), nodes, type1, type2, type3);
    }

    @Override
    public <T1, T2, T3, T4> Results<Results.Comp4<T1, T2, T3, T4>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3, type4);
        return new Comp4Results<>(compositions.getClassIndex(), nodes, type1, type2, type3, type4);
    }

    @Override
    public <T1, T2, T3, T4, T5> Results<Results.Comp5<T1, T2, T3, T4, T5>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3, type4, type5);
        return new Comp5Results<>(compositions.getClassIndex(), nodes, type1, type2, type3, type4, type5);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> Results<Results.Comp6<T1, T2, T3, T4, T5, T6>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3, type4, type5, type6);
        return new Comp6Results<>(compositions.getClassIndex(), nodes, type1, type2, type3, type4, type5, type6);
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
                );
            }

            int loggingLevelIndex = LoggingSystem.registerLoggingLevel(level);

            return new EntityRepository(name
                    , classIndexBit
                    , chunkBit
                    , chunkCountBit
                    , new LoggingSystem.Context(name, loggingLevelIndex)
            );
        }
    }

    public static abstract class AbstractResults<T> implements Results<T> {
        private final ClassIndex classIndex;
        private final Collection<CompositionRepository.Node> nodes;
        protected HashKey stateKey;

        public AbstractResults(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes) {
            this.classIndex = classIndex;
            this.nodes = nodes;
        }

        abstract Iterator<T> compositionIterator(Composition composition);

        @Override
        public Iterator<T> iterator() {
            return nodes != null ?
                    (nodes.size() > 1 ?
                            new IteratorWrapper<>(this, nodes.iterator()) :
                            compositionIterator(nodes.iterator().next().getComposition()))
                    :
                    new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public T next() {
                            return null;
                        }
                    };
        }

        @Override
        public <S extends Enum<S>> Results<T> withState(S state) {
            stateKey = Composition.calcHashKey(state, classIndex);
            return this;
        }

        @Override
        public Stream<T> stream() {
            return null;
        }

        @Override
        public Results<T> filter(Class<?>... componentTypes) {
            return null; //ToDo
        }
    }

    private static final class IteratorWrapper<T> implements Iterator<T> {
        private final AbstractResults<T> owner;
        private final Iterator<CompositionRepository.Node> nodesIterator;
        private Iterator<T> wrapped;

        public IteratorWrapper(AbstractResults<T> owner, Iterator<CompositionRepository.Node> nodesIterator) {
            this.owner = owner;
            this.nodesIterator = nodesIterator;
            this.wrapped = this.nodesIterator.hasNext() ?
                    owner.compositionIterator(this.nodesIterator.next().getComposition()) :
                    new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public T next() {
                            return null;
                        }
                    };
        }

        @Override
        public boolean hasNext() {
            return wrapped.hasNext()
                    || (nodesIterator.hasNext() && (wrapped = owner.compositionIterator(nodesIterator.next().getComposition())).hasNext());
        }

        @Override
        public T next() {
            return wrapped.next();
        }
    }

    public final static class Comp1Results<T> extends AbstractResults<Results.Comp1<T>> {
        private final Class<T> type;

        public Comp1Results(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes, Class<T> type) {
            super(classIndex, nodes);
            this.type = type;
        }

        @Override
        Iterator<Comp1<T>> compositionIterator(Composition composition) {
            Iterator<IntEntity> iterator = stateKey == null ?
                    composition.getTenant().iterator() :
                    new Composition.StateIterator(composition.getStateRootEntity(stateKey));
            return composition.select(type, iterator);
        }
    }

    public final static class Comp2Results<T1, T2> extends AbstractResults<Results.Comp2<T1, T2>> {
        private final Class<T1> type1;
        private final Class<T2> type2;

        public Comp2Results(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2) {
            super(classIndex, nodes);
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        Iterator<Comp2<T1, T2>> compositionIterator(Composition composition) {
            Iterator<IntEntity> iterator = stateKey == null ?
                    composition.getTenant().iterator() :
                    new Composition.StateIterator(composition.getStateRootEntity(stateKey));
            return composition.select(type1, type2, iterator);
        }
    }

    public final static class Comp3Results<T1, T2, T3> extends AbstractResults<Results.Comp3<T1, T2, T3>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;

        public Comp3Results(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            super(classIndex, nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
        }

        @Override
        Iterator<Comp3<T1, T2, T3>> compositionIterator(Composition composition) {
            Iterator<IntEntity> iterator = stateKey == null ?
                    composition.getTenant().iterator() :
                    new Composition.StateIterator(composition.getStateRootEntity(stateKey));
            return composition.select(type1, type2, type3, iterator);
        }
    }

    public final static class Comp4Results<T1, T2, T3, T4> extends AbstractResults<Results.Comp4<T1, T2, T3, T4>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;

        public Comp4Results(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
            super(classIndex, nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
        }

        @Override
        Iterator<Comp4<T1, T2, T3, T4>> compositionIterator(Composition composition) {
            Iterator<IntEntity> iterator = stateKey == null ?
                    composition.getTenant().iterator() :
                    new Composition.StateIterator(composition.getStateRootEntity(stateKey));
            return composition.select(type1, type2, type3, type4, iterator);
        }
    }

    public final static class Comp5Results<T1, T2, T3, T4, T5> extends AbstractResults<Results.Comp5<T1, T2, T3, T4, T5>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;

        public Comp5Results(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
            super(classIndex, nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
            this.type5 = type5;
        }

        @Override
        Iterator<Comp5<T1, T2, T3, T4, T5>> compositionIterator(Composition composition) {
            Iterator<IntEntity> iterator = stateKey == null ?
                    composition.getTenant().iterator() :
                    new Composition.StateIterator(composition.getStateRootEntity(stateKey));
            return composition.select(type1, type2, type3, type4, type5, iterator);
        }
    }

    public final static class Comp6Results<T1, T2, T3, T4, T5, T6> extends AbstractResults<Results.Comp6<T1, T2, T3, T4, T5, T6>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;
        private final Class<T6> type6;

        public Comp6Results(ClassIndex classIndex, Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
            super(classIndex, nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
            this.type5 = type5;
            this.type6 = type6;
        }

        @Override
        Iterator<Comp6<T1, T2, T3, T4, T5, T6>> compositionIterator(Composition composition) {
            Iterator<IntEntity> iterator = stateKey == null ?
                    composition.getTenant().iterator() :
                    new Composition.StateIterator(composition.getStateRootEntity(stateKey));
            return composition.select(type1, type2, type3, type4, type5, type6, iterator);
        }
    }
}

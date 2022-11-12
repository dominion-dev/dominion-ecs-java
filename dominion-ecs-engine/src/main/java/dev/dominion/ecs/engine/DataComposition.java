/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DataComposition {
    public static final int COMPONENT_INDEX_CAPACITY = 1 << 10;
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final Class<?>[] componentTypes;
    private final CompositionRepository repository;
    private final ChunkedPool<IntEntity> pool;
    private final ChunkedPool.Tenant<IntEntity> tenant;
    private final ClassIndex classIndex;
    private final IdSchema idSchema;
    private final int[] componentIndex;
    private final Map<IndexKey, ChunkedPool.Tenant<IntEntity>> stateTenants = new ConcurrentHashMap<>();
    private final LoggingSystem.Context loggingContext;

    public DataComposition(CompositionRepository repository, ChunkedPool<IntEntity> pool
            , ClassIndex classIndex, IdSchema idSchema, LoggingSystem.Context loggingContext
            , Class<?>... componentTypes) {
        this.repository = repository;
        this.pool = pool;
        this.tenant = pool == null ? null : pool.newTenant(componentTypes.length, this, "root");
        this.classIndex = classIndex;
        this.idSchema = idSchema;
        this.componentTypes = componentTypes;
        this.loggingContext = loggingContext;
        if (isMultiComponent()) {
            componentIndex = new int[COMPONENT_INDEX_CAPACITY];
            Arrays.fill(componentIndex, -1);
            for (int i = 0; i < length(); i++) {
                componentIndex[classIndex.getIndex(componentTypes[i])] = i;
            }
        } else {
            componentIndex = null;
        }
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + this)
            );
        }
    }

    public int length() {
        return componentTypes.length;
    }

    public boolean isMultiComponent() {
        return length() > 1;
    }

    public int fetchComponentIndex(Class<?> componentType) {
        return componentIndex[classIndex.getIndex(componentType)];
    }

    public Object[] sortComponentsInPlaceByIndex(Object[] components) {
        int newIdx;
        for (int i = 0; i < components.length; i++) {
            newIdx = fetchComponentIndex(components[i].getClass());
            if (newIdx != i) {
                swapComponents(components, i, newIdx);
            }
        }
        newIdx = fetchComponentIndex(components[0].getClass());
        if (newIdx > 0) {
            swapComponents(components, 0, newIdx);
        }
        return components;
    }

    private void swapComponents(Object[] components, int i, int newIdx) {
        Object temp = components[newIdx];
        components[newIdx] = components[i];
        components[i] = temp;
    }

    public <S extends Enum<S>> ChunkedPool.Tenant<IntEntity> fetchStateTenants(S state) {
        return fetchStateTenants(classIndex.getIndexKeyByEnum(state));
    }

    public ChunkedPool.Tenant<IntEntity> fetchStateTenants(IndexKey key) {
        return stateTenants.computeIfAbsent(key,
                s -> {
                    var newStateTenant = pool.newTenant(0, this, key);
                    if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                        LOGGER.log(
                                System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                                        , "Adding state " + newStateTenant + " to " + this)
                        );
                    }
                    return newStateTenant;
                });
    }

    public <S extends Enum<S>> ChunkedPool.Tenant<IntEntity> getStateTenant(S state) {
        return getStateTenant(classIndex.getIndexKeyByEnum(state));
    }

    public ChunkedPool.Tenant<IntEntity> getStateTenant(IndexKey state) {
        return stateTenants.get(state);
    }

    public IntEntity createEntity(boolean prepared, Object... components) {
        return tenant.register(new IntEntity(tenant.nextId()),
                !prepared && isMultiComponent() ? sortComponentsInPlaceByIndex(components) : components);
    }

    public void attachEntity(IntEntity entity, int[] indexMapping, int[] addedIndexMapping, Object[] addedComponents) {
        tenant.migrate(entity, tenant.nextId(), indexMapping, addedIndexMapping, addedComponents);
    }

    public Class<?>[] getComponentTypes() {
        return componentTypes;
    }

    public CompositionRepository getRepository() {
        return repository;
    }

    public ChunkedPool.Tenant<IntEntity> getTenant() {
        return tenant;
    }

    public IdSchema getIdSchema() {
        return idSchema;
    }

    @Override
    public String toString() {
        int iMax = componentTypes.length - 1;
        if (iMax == -1)
            return "Composition=[] with " + tenant;
        StringBuilder b = new StringBuilder("Composition=[");
        for (int i = 0; ; i++) {
            b.append(componentTypes[i].getSimpleName());
            if (i == iMax)
                return b.append("] with ").append(tenant).toString();
            b.append(", ");
        }
    }

    public <T> Iterator<T> selectT(Class<T> type, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        int idx = isMultiComponent() ? fetchComponentIndex(type) : 0;
        return new IteratorT<>(idx, iterator, this);
    }

    public <T> Iterator<Results.With1<T>> select(Class<T> type, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        int idx = isMultiComponent() ? fetchComponentIndex(type) : 0;
        return new IteratorWith1<>(idx, iterator, this);
    }

    public <T1, T2> Iterator<Results.With2<T1, T2>> select(Class<T1> type1, Class<T2> type2, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        return new IteratorWith2<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                iterator, this);
    }

    public <T1, T2, T3> Iterator<Results.With3<T1, T2, T3>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        return new IteratorWith3<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                iterator, this);
    }

    public <T1, T2, T3, T4> Iterator<Results.With4<T1, T2, T3, T4>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        return new IteratorWith4<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                fetchComponentIndex(type4),
                iterator, this);
    }

    public <T1, T2, T3, T4, T5> Iterator<Results.With5<T1, T2, T3, T4, T5>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        return new IteratorWith5<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                fetchComponentIndex(type4),
                fetchComponentIndex(type5),
                iterator, this);
    }

    public <T1, T2, T3, T4, T5, T6> Iterator<Results.With6<T1, T2, T3, T4, T5, T6>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6, ChunkedPool.PoolDataIterator<IntEntity> iterator) {
        return new IteratorWith6<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                fetchComponentIndex(type4),
                fetchComponentIndex(type5),
                fetchComponentIndex(type6),
                iterator, this);
    }

    record IteratorT<T>(int idx, ChunkedPool.PoolDataIterator<IntEntity> iterator,
                        DataComposition composition) implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T next() {
            T comp = (T) iterator.data(idx);
            iterator.next();
            return comp;
        }
    }

    record IteratorWith1<T>(int idx, ChunkedPool.PoolDataIterator<IntEntity> iterator,
                            DataComposition composition) implements Iterator<Results.With1<T>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Results.With1<T> next() {
            return new Results.With1<>((T) iterator.data(idx), iterator.next());
        }
    }

    record IteratorWith2<T1, T2>(int idx1, int idx2,
                                 ChunkedPool.PoolDataIterator<IntEntity> iterator,
                                 DataComposition composition) implements Iterator<Results.With2<T1, T2>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Results.With2<T1, T2> next() {

            return new Results.With2<>(
                    (T1) iterator.data(idx1),
                    (T2) iterator.data(idx2),
                    iterator.next());
        }
    }

    record IteratorWith3<T1, T2, T3>(int idx1, int idx2, int idx3,
                                     ChunkedPool.PoolDataIterator<IntEntity> iterator,
                                     DataComposition composition) implements Iterator<Results.With3<T1, T2, T3>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Results.With3<T1, T2, T3> next() {
            return new Results.With3<>(
                    (T1) iterator.data(idx1),
                    (T2) iterator.data(idx2),
                    (T3) iterator.data(idx3),
                    iterator.next());
        }
    }

    record IteratorWith4<T1, T2, T3, T4>(int idx1, int idx2, int idx3, int idx4,
                                         ChunkedPool.PoolDataIterator<IntEntity> iterator,
                                         DataComposition composition) implements Iterator<Results.With4<T1, T2, T3, T4>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Results.With4<T1, T2, T3, T4> next() {
            return new Results.With4<>(
                    (T1) iterator.data(idx1),
                    (T2) iterator.data(idx2),
                    (T3) iterator.data(idx3),
                    (T4) iterator.data(idx4),
                    iterator.next());
        }
    }

    record IteratorWith5<T1, T2, T3, T4, T5>(int idx1, int idx2, int idx3, int idx4, int idx5,
                                             ChunkedPool.PoolDataIterator<IntEntity> iterator,
                                             DataComposition composition) implements Iterator<Results.With5<T1, T2, T3, T4, T5>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Results.With5<T1, T2, T3, T4, T5> next() {
            return new Results.With5<>(
                    (T1) iterator.data(idx1),
                    (T2) iterator.data(idx2),
                    (T3) iterator.data(idx3),
                    (T4) iterator.data(idx4),
                    (T5) iterator.data(idx5),
                    iterator.next());
        }
    }

    record IteratorWith6<T1, T2, T3, T4, T5, T6>(int idx1, int idx2, int idx3, int idx4, int idx5, int idx6,
                                                 ChunkedPool.PoolDataIterator<IntEntity> iterator,
                                                 DataComposition composition) implements Iterator<Results.With6<T1, T2, T3, T4, T5, T6>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Results.With6<T1, T2, T3, T4, T5, T6> next() {
            return new Results.With6<>(
                    (T1) iterator.data(idx1),
                    (T2) iterator.data(idx2),
                    (T3) iterator.data(idx3),
                    (T4) iterator.data(idx4),
                    (T5) iterator.data(idx5),
                    (T6) iterator.data(idx6),
                    iterator.next());
        }
    }
}

/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.IdSchema;
import dev.dominion.ecs.engine.system.ClassIndex;
import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.Arrays;
import java.util.Iterator;

public final class DataComposition {
    public static final int COMPONENT_INDEX_CAPACITY = 1 << 10;
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final Class<?>[] componentTypes;
    private final CompositionRepository repository;
    private final ChunkedPool.Tenant<IntEntity> tenant;
    private final ClassIndex classIndex;
    private final IdSchema idSchema;
    private final int[] componentIndex;
    //    private final Map<IndexKey, IntEntity> states = new ConcurrentHashMap<>();
//    private final StampedLock stateLock = new StampedLock();
    private final LoggingSystem.Context loggingContext;

    public DataComposition(CompositionRepository repository, ChunkedPool<IntEntity> pool
            , ClassIndex classIndex, IdSchema idSchema, LoggingSystem.Context loggingContext
            , Class<?>... componentTypes) {
        this.repository = repository;
        this.tenant = pool == null ? null : pool.newTenant(componentTypes.length, this);
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

    public IntEntity createEntity(String name, boolean prepared, Object... components) {
        int id = tenant.nextId();
        return tenant.register(id, new IntEntity(id, name),
                !prepared && isMultiComponent() ? sortComponentsInPlaceByIndex(components) : components);
    }

//    public void detachEntityAndState(IntEntity entity) {
//        detachEntity(entity);
//        if (entity.getPrev() != null || entity.getNext() != null) {
//            detachEntityState(entity);
//        }
//    }

    public void attachEntity(IntEntity entity, int[] indexMapping, int[] addedIndexMapping, Object[] addedComponents) {
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Start Attaching " + entity + " to " + this + " and  " + tenant)
            );
        }

        entity = tenant.migrate(entity, tenant.nextId(), indexMapping, addedIndexMapping, addedComponents);

        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Attached " + entity)
            );
        }
    }
//
//    public void attachEntity(IntEntity entity, boolean prepared, Object... components) {
//        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
//            LOGGER.log(
//                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
//                            , "Start Attaching " + entity + " to " + this + " and  " + tenant)
//            );
//        }
//        entity = tenant.register(entity.setId(tenant.nextId()), entity.setComposition(this), switch (length()) {
//            case 0 -> null;
//            case 1 -> components;
//            default -> !prepared && isMultiComponent() ? sortComponentsInPlaceByIndex(components) : components;
//        });
//        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
//            LOGGER.log(
//                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
//                            , "Attached " + entity)
//            );
//        }
//    }

//    public void reEnableEntity(IntEntity entity) {
//        tenant.register(entity.getId(), entity, null);
//    }

//    public void detachEntity(IntEntity entity) {
//        tenant.freeId(entity.getId());
//        entity.flagDetachedId();
//        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
//            LOGGER.log(
//                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
//                            , "Detached: " + entity)
//            );
//        }
//    }

//    public <S extends Enum<S>> IntEntity setEntityState(IntEntity entity, S state) {
//        boolean detached = detachEntityState(entity);
//        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
//            LOGGER.log(
//                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
//                            , "Detaching state from " + entity + " : " + detached)
//            );
//        }
//        if (state != null) {
//            attachEntityState(entity, state);
//        }
//        return entity;
//    }

//    private boolean detachEntityState(IntEntity entity) {
//        IndexKey key = entity.getStateRoot();
//        // if entity is root
//        if (key != null) {
//            // if alone: root
//            if (entity.getPrev() == null) {
//                if (states.remove(key) != null) {
//                    entity.setStateRoot(null);
//                    return true;
//                }
//            } else
//            // root -> prev
//            {
//                IntEntity prev = (IntEntity) entity.getPrev();
//                if (states.replace(key, entity, prev)) {
//                    entity.setStateRoot(null);
//                    entity.setPrev(null);
//                    prev.setNext(null);
//                    prev.setStateRoot(key);
//                    return true;
//                }
//            }
//        } else
//        // next -> entity -> ?prev
//        {
//            long stamp = stateLock.writeLock();
//            try {
//                IntEntity prev, next;
//                // recheck after lock
//                if ((next = (IntEntity) entity.getNext()) != null) {
//                    if ((prev = (IntEntity) entity.getPrev()) != null) {
//                        prev.setNext(next);
//                        next.setPrev(prev);
//                    } else {
//                        next.setPrev(null);
//                    }
//                }
//                entity.setPrev(null);
//                entity.setNext(null);
//                return true;
//            } finally {
//                stateLock.unlockWrite(stamp);
//            }
//        }
//        return false;
//    }
//
//    private <S extends Enum<S>> void attachEntityState(IntEntity entity, S state) {
//        IndexKey indexKey = calcIndexKey(state, classIndex);
//        IntEntity prev = states.computeIfAbsent(indexKey
//                , entity::setStateRoot);
//        if (prev != entity) {
//            states.computeIfPresent(indexKey, (k, oldEntity) -> {
//                entity.setPrev(oldEntity);
//                entity.setStateRoot(k);
//                oldEntity.setNext(entity);
//                oldEntity.setStateRoot(null);
//                return entity;
//            });
//        }
//        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
//            LOGGER.log(
//                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
//                            , "Attaching state "
//                                    + state.getClass().getSimpleName() + "." + state
//                                    + " to " + entity)
//            );
//        }
//    }

    public Class<?>[] getComponentTypes() {
        return componentTypes;
    }

    public CompositionRepository getRepository() {
        return repository;
    }

    public ChunkedPool.Tenant<IntEntity> getTenant() {
        return tenant;
    }

//    public Map<IndexKey, IntEntity> getStates() {
//        return Collections.unmodifiableMap(states);
//    }

//    public IntEntity getStateRootEntity(IndexKey key) {
//        return states.get(key);
//    }

    public IdSchema getIdSchema() {
        return idSchema;
    }

    @Override
    public String toString() {
        int iMax = componentTypes.length - 1;
        if (iMax == -1)
            return "Composition=[]";
        StringBuilder b = new StringBuilder("Composition=[");
        for (int i = 0; ; i++) {
            b.append(componentTypes[i].getSimpleName());
            if (i == iMax)
                return b.append(']').toString();
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

    public static class StateIterator implements Iterator<IntEntity> {
        private IntEntity next;

        public StateIterator(IntEntity rootEntity) {
            next = rootEntity;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public IntEntity next() {
            var current = next;
            next = (IntEntity) next.getPrev();
            return current;
        }
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

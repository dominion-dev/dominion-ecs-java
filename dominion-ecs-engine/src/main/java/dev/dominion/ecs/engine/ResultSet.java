/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.Logging;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ResultSet<T> implements Results<T> {
    private static final System.Logger LOGGER = Logging.getLogger();
    protected final boolean withEntity;
    protected final CompositionRepository compositionRepository;
    private final Map<IndexKey, CompositionRepository.Node> nodeMap;
    protected IndexKey stateKey;

    public ResultSet(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap, boolean withEntity) {
        this.compositionRepository = compositionRepository;
        this.nodeMap = nodeMap;
        this.withEntity = withEntity;
        if (Logging.isLoggable(compositionRepository.getLoggingContext().levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(compositionRepository.getLoggingContext().subject()
                            , "Creating " + this)
            );
        }
    }

    @Override
    public String toString() {
        return "ResultSet{" +
                "nodes=" + (nodeMap == null ? null : nodeMap.values()) +
                ", withEntity=" + withEntity +
                ", stateKey=" + stateKey +
                '}';
    }

    abstract Iterator<T> compositionIterator(DataComposition composition);

    @Override
    public Iterator<T> iterator() {
        return nodeMap != null && nodeMap.size() > 0 ?
                (nodeMap.size() > 1 ?
                        new IteratorWrapper<>(this, nodeMap.values().iterator()) :
                        compositionIterator(nodeMap.values().iterator().next().getComposition()))
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

    @Override public int size() {
        if (nodeMap == null || nodeMap.isEmpty()) return 0;

        var size = 0;
        for (CompositionRepository.Node node : nodeMap.values()) {
            final var composition = node.getComposition();
            final var sizeIterator = getPoolSizeIterator(composition);
            while (sizeIterator.hasNext()) {
                size += sizeIterator.next();
            }
        }
        return size;
    }

    @Override
    public <S extends Enum<S>> Results<T> withState(S state) {
        stateKey = compositionRepository.getClassIndex().getIndexKeyByEnum(state);
        if (Logging.isLoggable(compositionRepository.getLoggingContext().levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(compositionRepository.getLoggingContext().subject()
                            , "Setting state " + state + " to " + this)
            );
        }
        return this;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @Override
    public Stream<T> parallelStream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), true);
    }

    @Override
    public Results<T> without(Class<?>... componentTypes) {
        compositionRepository.mapWithout(nodeMap, componentTypes);
        return this;
    }

    @Override
    public Results<T> withAlso(Class<?>... componentTypes) {
        compositionRepository.mapWithAlso(nodeMap, componentTypes);
        return this;
    }

    @SuppressWarnings("unchecked")
    protected ChunkedPool.PoolSizeIterator<IntEntity> getPoolSizeIterator(DataComposition composition) {
        boolean withState = stateKey != null;
        ChunkedPool.PoolSizeIterator<IntEntity> iterator;
        if (withState) {
            var tenant = composition.getStateTenant(stateKey);
            iterator = tenant == null ?
                    ChunkedPool.PoolSizeEmptyIterator.INSTANCE :
                    tenant.sizeIterator();
        } else {
            iterator = composition.getTenant().sizeIterator();
        }
        return iterator;
    }

    protected ChunkedPool.PoolDataIterator<IntEntity> getPoolDataIterator(DataComposition composition, boolean multiData) {
        boolean withState = stateKey != null;
        ChunkedPool.PoolDataIterator<IntEntity> iterator;
        if (withState) {
            var tenant = composition.getStateTenant(stateKey);
            iterator = tenant == null ?
                    new ChunkedPool.PoolDataEmptyIterator<>() :
                    withEntity ?
                            tenant.iteratorWithState(multiData) :
                            tenant.noItemIteratorWithState(multiData);
        } else {
            var tenant = composition.getTenant();
            iterator = withEntity ?
                    tenant.iterator() :
                    tenant.noItemIterator();
        }
        return iterator;
    }

    private static final class IteratorWrapper<T> implements Iterator<T> {
        private final ResultSet<T> owner;
        private final Iterator<CompositionRepository.Node> nodesIterator;
        private Iterator<T> wrapped;

        public IteratorWrapper(ResultSet<T> owner, Iterator<CompositionRepository.Node> nodesIterator) {
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
            if (wrapped.hasNext()) {
                return true;
            }

            while (nodesIterator.hasNext()) {
                if ((wrapped = owner.compositionIterator(nodesIterator.next().getComposition())).hasNext()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public T next() {
            return wrapped.next();
        }
    }

    public final static class With<T> extends ResultSet<T> {
        private final Class<T> type;

        public With(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                    Class<T> type) {
            super(compositionRepository, nodeMap, false);
            this.type = type;
        }

        @Override
        Iterator<T> compositionIterator(DataComposition composition) {
            return composition.selectT(type, composition.getTenant().noItemIterator());
        }

        @Override
        public <S extends Enum<S>> Results<T> withState(S state) {
            throw new UnsupportedOperationException("Unsupported .findCompositionWith(Class<T> type).withState(S state) call : use .findEntitiesWith(Class<T> type).withState(S state) instead");
        }
    }

    public final static class All extends ResultSet<IntEntity> {

        public All(CompositionRepository compositionRepository) {
            super(compositionRepository, null, false);
        }

        @Override
        Iterator<IntEntity> compositionIterator(DataComposition composition) {
            return null;
        }

        @Override
        public Iterator<IntEntity> iterator() {
            return compositionRepository.getPool().allEntities();
        }

        @Override public int size() {
            return compositionRepository.getPool().size();
        }

        @Override
        public <S extends Enum<S>> Results<IntEntity> withState(S state) {
            throw new UnsupportedOperationException("Unsupported operation.");
        }

        @Override
        public Results<IntEntity> without(Class<?>... componentTypes) {
            throw new UnsupportedOperationException("Unsupported operation.");
        }

        @Override
        public Results<IntEntity> withAlso(Class<?>... componentTypes) {
            throw new UnsupportedOperationException("Unsupported operation.");
        }
    }

    public final static class With1<T> extends ResultSet<Results.With1<T>> {
        private final Class<T> type;
        private final NextWith1<T> nextWith1 = new NextWith1<>();

        public With1(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     Class<T> type) {
            super(compositionRepository, nodeMap, true);
            this.type = type;
        }

        @Override
        Iterator<Results.With1<T>> compositionIterator(DataComposition composition) {
            var iterator = getPoolDataIterator(composition, composition.length() > 1);
            var fetcher = iterator instanceof ChunkedPool.PoolDataIteratorWithState<IntEntity> ? nextWith1 : null;
            return composition.select(type, iterator, fetcher);
        }
    }

    public final static class With2<T1, T2> extends ResultSet<Results.With2<T1, T2>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final NextWith2<T1, T2> nextWith2 = new NextWith2<>();

        public With2(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        Iterator<Results.With2<T1, T2>> compositionIterator(DataComposition composition) {
            var iterator = getPoolDataIterator(composition, true);
            var fetcher = iterator instanceof ChunkedPool.PoolMultiDataIteratorWithState<IntEntity> ? nextWith2 : null;
            return composition.select(type1, type2, iterator, fetcher);
        }
    }

    public final static class With3<T1, T2, T3> extends ResultSet<Results.With3<T1, T2, T3>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final NextWith3<T1, T2, T3> nextWith3 = new NextWith3<>();

        public With3(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
        }

        @Override
        Iterator<Results.With3<T1, T2, T3>> compositionIterator(DataComposition composition) {
            var iterator = getPoolDataIterator(composition, true);
            var fetcher = iterator instanceof ChunkedPool.PoolMultiDataIteratorWithState<IntEntity> ? nextWith3 : null;
            return composition.select(type1, type2, type3, iterator, fetcher);
        }
    }

    public final static class With4<T1, T2, T3, T4> extends ResultSet<Results.With4<T1, T2, T3, T4>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final NextWith4<T1, T2, T3, T4> nextWith4 = new NextWith4<>();

        public With4(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
        }

        @Override
        Iterator<Results.With4<T1, T2, T3, T4>> compositionIterator(DataComposition composition) {
            var iterator = getPoolDataIterator(composition, true);
            var fetcher = iterator instanceof ChunkedPool.PoolMultiDataIteratorWithState<IntEntity> ? nextWith4 : null;
            return composition.select(type1, type2, type3, type4, iterator, fetcher);
        }
    }

    public final static class With5<T1, T2, T3, T4, T5> extends ResultSet<Results.With5<T1, T2, T3, T4, T5>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;
        private final NextWith5<T1, T2, T3, T4, T5> nextWith5 = new NextWith5<>();

        public With5(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
            this.type5 = type5;
        }

        @Override
        Iterator<Results.With5<T1, T2, T3, T4, T5>> compositionIterator(DataComposition composition) {
            var iterator = getPoolDataIterator(composition, true);
            var fetcher = iterator instanceof ChunkedPool.PoolMultiDataIteratorWithState<IntEntity> ? nextWith5 : null;
            return composition.select(type1, type2, type3, type4, type5, iterator, fetcher);
        }
    }

    public final static class With6<T1, T2, T3, T4, T5, T6> extends ResultSet<Results.With6<T1, T2, T3, T4, T5, T6>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;
        private final Class<T6> type6;
        private final NextWith6<T1, T2, T3, T4, T5, T6> nextWith6 = new NextWith6<>();

        public With6(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
            this.type5 = type5;
            this.type6 = type6;
        }

        @Override
        Iterator<Results.With6<T1, T2, T3, T4, T5, T6>> compositionIterator(DataComposition composition) {
            var iterator = getPoolDataIterator(composition, true);
            var fetcher = iterator instanceof ChunkedPool.PoolMultiDataIteratorWithState<IntEntity> ? nextWith6 : null;
            return composition.select(type1, type2, type3, type4, type5, type6, iterator, fetcher);
        }
    }

    public final static class NextWith1<T1> implements ChunkedPool.PoolIteratorNextWith1 {
        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[] dataArray, int next, ChunkedPool.Item item) {
            return new Results.With1<>(
                    (T1) dataArray[next],
                    (Entity) item);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[][] multiDataArray, int i1, int next, ChunkedPool.Item item) {
            return new Results.With1<>(
                    (T1) multiDataArray[i1][next],
                    (Entity) item);
        }
    }

    public final static class NextWith2<T1, T2> implements ChunkedPool.PoolIteratorNextWith2 {
        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[][] multiDataArray, int i1, int i2, int next, ChunkedPool.Item item) {
            return new Results.With2<>(
                    (T1) multiDataArray[i1][next],
                    (T2) multiDataArray[i2][next],
                    (Entity) item);
        }
    }

    public final static class NextWith3<T1, T2, T3> implements ChunkedPool.PoolIteratorNextWith3 {
        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int next, ChunkedPool.Item item) {
            return new Results.With3<>(
                    (T1) multiDataArray[i1][next],
                    (T2) multiDataArray[i2][next],
                    (T3) multiDataArray[i3][next],
                    (Entity) item);
        }
    }

    public final static class NextWith4<T1, T2, T3, T4> implements ChunkedPool.PoolIteratorNextWith4 {
        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int i4, int next, ChunkedPool.Item item) {
            return new Results.With4<>(
                    (T1) multiDataArray[i1][next],
                    (T2) multiDataArray[i2][next],
                    (T3) multiDataArray[i3][next],
                    (T4) multiDataArray[i4][next],
                    (Entity) item);
        }
    }

    public final static class NextWith5<T1, T2, T3, T4, T5> implements ChunkedPool.PoolIteratorNextWith5 {
        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int i4, int i5, int next, ChunkedPool.Item item) {
            return new Results.With5<>(
                    (T1) multiDataArray[i1][next],
                    (T2) multiDataArray[i2][next],
                    (T3) multiDataArray[i3][next],
                    (T4) multiDataArray[i4][next],
                    (T5) multiDataArray[i5][next],
                    (Entity) item);
        }
    }

    public final static class NextWith6<T1, T2, T3, T4, T5, T6> implements ChunkedPool.PoolIteratorNextWith6 {
        @SuppressWarnings("unchecked")
        @Override
        public Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int i4, int i5, int i6, int next, ChunkedPool.Item item) {
            return new Results.With6<>(
                    (T1) multiDataArray[i1][next],
                    (T2) multiDataArray[i2][next],
                    (T3) multiDataArray[i3][next],
                    (T4) multiDataArray[i4][next],
                    (T5) multiDataArray[i5][next],
                    (T6) multiDataArray[i6][next],
                    (Entity) item);
        }
    }
}

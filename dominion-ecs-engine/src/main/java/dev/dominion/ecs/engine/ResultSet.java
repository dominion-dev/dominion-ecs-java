/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.system.IndexKey;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ResultSet<T> implements Results<T> {
    protected final boolean withEntity;
    private final CompositionRepository compositionRepository;
    private final Map<IndexKey, CompositionRepository.Node> nodeMap;
    protected IndexKey stateKey;

    public ResultSet(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap, boolean withEntity) {
        this.compositionRepository = compositionRepository;
        this.nodeMap = nodeMap;
        this.withEntity = withEntity;
    }

    abstract Iterator<T> compositionIterator(DataComposition composition);

    @Override
    public Iterator<T> iterator() {
        return nodeMap != null ?
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

    @Override
    public <S extends Enum<S>> Results<T> withState(S state) {
        stateKey = DataComposition.calcIndexKey(state, compositionRepository.getClassIndex());
        return this;
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
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
            return wrapped.hasNext()
                    || (nodesIterator.hasNext() && (wrapped = owner.compositionIterator(nodesIterator.next().getComposition())).hasNext());
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
    }

    public final static class With1<T> extends ResultSet<Results.With1<T>> {
        private final Class<T> type;

        public With1(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     Class<T> type) {
            super(compositionRepository, nodeMap, true);
            this.type = type;
        }

        @Override
        Iterator<Results.With1<T>> compositionIterator(DataComposition composition) {
            return composition.select(type, composition.getTenant().iterator());
        }
    }

    public final static class With2<T1, T2> extends ResultSet<Results.With2<T1, T2>> {
        private final Class<T1> type1;
        private final Class<T2> type2;

        public With2(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        Iterator<Results.With2<T1, T2>> compositionIterator(DataComposition composition) {
            ChunkedPool.PoolDataIterator<IntEntity> iterator = withEntity ?
                    composition.getTenant().iterator() : composition.getTenant().noItemIterator();
            return composition.select(type1, type2, iterator);
        }
    }

    public final static class With3<T1, T2, T3> extends ResultSet<Results.With3<T1, T2, T3>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;

        public With3(CompositionRepository compositionRepository, Map<IndexKey, CompositionRepository.Node> nodeMap,
                     boolean withEntity, Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            super(compositionRepository, nodeMap, withEntity);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
        }

        @Override
        Iterator<Results.With3<T1, T2, T3>> compositionIterator(DataComposition composition) {
            ChunkedPool.PoolDataIterator<IntEntity> iterator = withEntity ?
                    composition.getTenant().iterator() : composition.getTenant().noItemIterator();
            return composition.select(type1, type2, type3, iterator);
        }
    }

    public final static class With4<T1, T2, T3, T4> extends ResultSet<Results.With4<T1, T2, T3, T4>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;

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
            ChunkedPool.PoolDataIterator<IntEntity> iterator = withEntity ?
                    composition.getTenant().iterator() : composition.getTenant().noItemIterator();
            return composition.select(type1, type2, type3, type4, iterator);
        }
    }

    public final static class With5<T1, T2, T3, T4, T5> extends ResultSet<Results.With5<T1, T2, T3, T4, T5>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;

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
            ChunkedPool.PoolDataIterator<IntEntity> iterator = withEntity ?
                    composition.getTenant().iterator() : composition.getTenant().noItemIterator();
            return composition.select(type1, type2, type3, type4, type5, iterator);
        }
    }

    public final static class With6<T1, T2, T3, T4, T5, T6> extends ResultSet<Results.With6<T1, T2, T3, T4, T5, T6>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;
        private final Class<T6> type6;

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
            ChunkedPool.PoolDataIterator<IntEntity> iterator = withEntity ?
                    composition.getTenant().iterator() : composition.getTenant().noItemIterator();
            return composition.select(type1, type2, type3, type4, type5, type6, iterator);
        }
    }
}

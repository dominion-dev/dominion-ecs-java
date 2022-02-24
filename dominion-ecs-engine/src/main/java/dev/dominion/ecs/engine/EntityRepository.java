/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public final class EntityRepository implements Dominion {

    private final CompositionRepository compositions = new CompositionRepository();

    @Override
    public Entity createEntity(Object... components) {
        Object[] componentArray = components.length == 0 ? null : components;
        Composition composition = compositions.getOrCreate(componentArray);
        return composition.createEntity(componentArray);
    }

    @Override
    public Entity createEntityAs(Entity prefab, Object... components) {
        return null;
    }

    @Override
    public boolean destroyEntity(Entity entity) {
        LongEntity longEntity = (LongEntity) entity;
        return longEntity.getComposition().destroyEntity(longEntity);
    }

    @Override
    public <T> Results<Results.Comp1<T>> findComponents(Class<T> type) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type);
        return new Comp1Results<>(nodes, type);
    }

    @Override
    public <T1, T2> Results<Results.Comp2<T1, T2>> findComponents(Class<T1> type1, Class<T2> type2) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2);
        return new Comp2Results<>(nodes, type1, type2);
    }

    @Override
    public <T1, T2, T3> Results<Results.Comp3<T1, T2, T3>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3);
        return new Comp3Results<>(nodes, type1, type2, type3);
    }

    @Override
    public <T1, T2, T3, T4> Results<Results.Comp4<T1, T2, T3, T4>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3, type4);
        return new Comp4Results<>(nodes, type1, type2, type3, type4);
    }

    @Override
    public <T1, T2, T3, T4, T5> Results<Results.Comp5<T1, T2, T3, T4, T5>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3, type4, type5);
        return new Comp5Results<>(nodes, type1, type2, type3, type4, type5);
    }

    @Override
    public <T1, T2, T3, T4, T5, T6> Results<Results.Comp6<T1, T2, T3, T4, T5, T6>> findComponents(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        Collection<CompositionRepository.Node> nodes = compositions.find(type1, type2, type3, type4, type5, type6);
        return new Comp6Results<>(nodes, type1, type2, type3, type4, type5, type6);
    }

    @Override
    public void close() {
        compositions.close();
    }


    public static abstract class AbstractResults<T> implements Results<T> {
        private final Collection<CompositionRepository.Node> nodes;

        public AbstractResults(Collection<CompositionRepository.Node> nodes) {
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

        public Comp1Results(Collection<CompositionRepository.Node> nodes, Class<T> type) {
            super(nodes);
            this.type = type;
        }

        @Override
        Iterator<Comp1<T>> compositionIterator(Composition composition) {
            return composition.select(type);
        }
    }

    public final static class Comp2Results<T1, T2> extends AbstractResults<Results.Comp2<T1, T2>> {
        private final Class<T1> type1;
        private final Class<T2> type2;

        public Comp2Results(Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2) {
            super(nodes);
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        Iterator<Comp2<T1, T2>> compositionIterator(Composition composition) {
            return composition.select(type1, type2);
        }
    }

    public final static class Comp3Results<T1, T2, T3> extends AbstractResults<Results.Comp3<T1, T2, T3>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;

        public Comp3Results(Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3) {
            super(nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
        }

        @Override
        Iterator<Comp3<T1, T2, T3>> compositionIterator(Composition composition) {
            return composition.select(type1, type2, type3);
        }
    }

    public final static class Comp4Results<T1, T2, T3, T4> extends AbstractResults<Results.Comp4<T1, T2, T3, T4>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;

        public Comp4Results(Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
            super(nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
        }

        @Override
        Iterator<Comp4<T1, T2, T3, T4>> compositionIterator(Composition composition) {
            return composition.select(type1, type2, type3, type4);
        }
    }

    public final static class Comp5Results<T1, T2, T3, T4, T5> extends AbstractResults<Results.Comp5<T1, T2, T3, T4, T5>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;

        public Comp5Results(Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
            super(nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
            this.type5 = type5;
        }

        @Override
        Iterator<Comp5<T1, T2, T3, T4, T5>> compositionIterator(Composition composition) {
            return composition.select(type1, type2, type3, type4, type5);
        }
    }

    public final static class Comp6Results<T1, T2, T3, T4, T5, T6> extends AbstractResults<Results.Comp6<T1, T2, T3, T4, T5, T6>> {
        private final Class<T1> type1;
        private final Class<T2> type2;
        private final Class<T3> type3;
        private final Class<T4> type4;
        private final Class<T5> type5;
        private final Class<T6> type6;

        public Comp6Results(Collection<CompositionRepository.Node> nodes, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
            super(nodes);
            this.type1 = type1;
            this.type2 = type2;
            this.type3 = type3;
            this.type4 = type4;
            this.type5 = type5;
            this.type6 = type6;
        }

        @Override
        Iterator<Comp6<T1, T2, T3, T4, T5, T6>> compositionIterator(Composition composition) {
            return composition.select(type1, type2, type3, type4, type5, type6);
        }
    }
}

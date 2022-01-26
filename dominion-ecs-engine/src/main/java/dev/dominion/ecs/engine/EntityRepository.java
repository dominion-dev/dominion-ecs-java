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
        final Composition composition = compositions.getOrCreate(components);
        return switch (components.length) {
            case 0 -> composition.createEntity();
            case 1 -> composition.createEntity(components[0]);
            default -> composition.createEntity(components);
        };
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
}

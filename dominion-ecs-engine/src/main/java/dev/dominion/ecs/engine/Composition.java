package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.system.ClassIndex;

import java.util.Iterator;

public final class Composition {
    private final static int componentIndexCapacity = 1 << 10;
    private final Class<?>[] componentTypes;
    private final CompositionRepository repository;
    private final ConcurrentPool.Tenant<LongEntity> tenant;
    private final ClassIndex classIndex;
    private final int[] componentIndex;

    public Composition(CompositionRepository repository, ConcurrentPool.Tenant<LongEntity> tenant, ClassIndex classIndex, Class<?>... componentTypes) {
        this.repository = repository;
        this.tenant = tenant;
        this.classIndex = classIndex;
        this.componentTypes = componentTypes;
        if (componentTypes.length > 1) {
            componentIndex = new int[componentIndexCapacity];
            for (int i = 0; i < componentTypes.length; i++) {
                componentIndex[classIndex.getIndex(componentTypes[i])] = i;
            }
        } else {
            componentIndex = null;
        }
    }

    public Object[] sortComponentsInPlaceByIndex(Object[] components) {
        int newIdx;
        for (int i = 0; i < components.length; i++) {
            newIdx = componentIndex[classIndex.getIndex(components[i].getClass())];
            if (newIdx != i) {
                swapComponents(components, i, newIdx);
            }
        }
        newIdx = componentIndex[classIndex.getIndex(components[0].getClass())];
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

    public LongEntity createEntity(Object... components) {
        long id = tenant.nextId();
        LongEntity entity = tenant.register(id, new LongEntity(id, this));
        return switch (componentTypes.length) {
            case 0 -> entity;
            case 1 -> entity.setSingleComponent(components[0]);
            default -> entity.setComponents(sortComponentsInPlaceByIndex(components));
        };
    }

    public LongEntity attachEntity(LongEntity entity, Object... components) {
        long id = tenant.nextId();
        entity.setId(id);
        entity.setComposition(this);
        tenant.register(id, entity);
        return switch (componentTypes.length) {
            case 0 -> entity;
            case 1 -> entity.setSingleComponent(components[0]);
            default -> entity.setComponents(sortComponentsInPlaceByIndex(components));
        };
    }

    public boolean destroyEntity(LongEntity entity) {
        detachEntity(entity);
        entity.setComposition(null);
        entity.setSingleComponent(null);
        entity.setComponents(null);
        return true;
    }

    public void detachEntity(LongEntity entity) {
        tenant.freeId(entity.getId());
    }

    public Class<?>[] getComponentTypes() {
        return componentTypes;
    }

    public CompositionRepository getRepository() {
        return repository;
    }

    public ConcurrentPool.Tenant<LongEntity> getTenant() {
        return tenant;
    }

    public <T> Iterator<Results.Comp1<T>> select(Class<T> type) {
        int idx = componentIndex == null ? -1 : componentIndex[classIndex.getIndex(type)];
        return new Comp1Iterator<>(idx, tenant.iterator());
    }

    public <T1, T2> Iterator<Results.Comp2<T1, T2>> select(Class<T1> type1, Class<T2> type2) {
        return new Comp2Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                tenant.iterator());
    }

    public <T1, T2, T3> Iterator<Results.Comp3<T1, T2, T3>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        return new Comp3Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                componentIndex[classIndex.getIndex(type3)],
                tenant.iterator());
    }

    public <T1, T2, T3, T4> Iterator<Results.Comp4<T1, T2, T3, T4>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        return new Comp4Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                componentIndex[classIndex.getIndex(type3)],
                componentIndex[classIndex.getIndex(type4)],
                tenant.iterator());
    }

    public <T1, T2, T3, T4, T5> Iterator<Results.Comp5<T1, T2, T3, T4, T5>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        return new Comp5Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                componentIndex[classIndex.getIndex(type3)],
                componentIndex[classIndex.getIndex(type4)],
                componentIndex[classIndex.getIndex(type5)],
                tenant.iterator());
    }

    public <T1, T2, T3, T4, T5, T6> Iterator<Results.Comp6<T1, T2, T3, T4, T5, T6>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        return new Comp6Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                componentIndex[classIndex.getIndex(type3)],
                componentIndex[classIndex.getIndex(type4)],
                componentIndex[classIndex.getIndex(type5)],
                componentIndex[classIndex.getIndex(type6)],
                tenant.iterator());
    }

    public <T1, T2, T3, T4, T5, T6, T7> Iterator<Results.Comp7<T1, T2, T3, T4, T5, T6, T7>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6, Class<T7> type7) {
        return new Comp7Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                componentIndex[classIndex.getIndex(type3)],
                componentIndex[classIndex.getIndex(type4)],
                componentIndex[classIndex.getIndex(type5)],
                componentIndex[classIndex.getIndex(type6)],
                componentIndex[classIndex.getIndex(type7)],
                tenant.iterator());
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8> Iterator<Results.Comp8<T1, T2, T3, T4, T5, T6, T7, T8>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6, Class<T7> type7, Class<T8> type8) {
        return new Comp8Iterator<>(
                componentIndex[classIndex.getIndex(type1)],
                componentIndex[classIndex.getIndex(type2)],
                componentIndex[classIndex.getIndex(type3)],
                componentIndex[classIndex.getIndex(type4)],
                componentIndex[classIndex.getIndex(type5)],
                componentIndex[classIndex.getIndex(type6)],
                componentIndex[classIndex.getIndex(type7)],
                componentIndex[classIndex.getIndex(type8)],
                tenant.iterator());
    }

    record Comp1Iterator<T>(int idx, Iterator<LongEntity> iterator) implements Iterator<Results.Comp1<T>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp1<T> next() {
            LongEntity longEntity = iterator.next();
            T comp = (T) (idx < 0 ? longEntity.getSingleComponent() : longEntity.getComponents()[idx]);
            return new Results.Comp1<>(comp, longEntity);
        }
    }

    record Comp2Iterator<T1, T2>(int idx1, int idx2,
                                 Iterator<LongEntity> iterator) implements Iterator<Results.Comp2<T1, T2>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp2<T1, T2> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp2<>((T1) components[idx1], (T2) components[idx2], longEntity);
        }
    }

    record Comp3Iterator<T1, T2, T3>(int idx1, int idx2, int idx3,
                                     Iterator<LongEntity> iterator) implements Iterator<Results.Comp3<T1, T2, T3>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp3<T1, T2, T3> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp3<>(
                    (T1) components[idx1],
                    (T2) components[idx2],
                    (T3) components[idx3],
                    longEntity);
        }
    }

    record Comp4Iterator<T1, T2, T3, T4>(int idx1, int idx2, int idx3, int idx4,
                                         Iterator<LongEntity> iterator) implements Iterator<Results.Comp4<T1, T2, T3, T4>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp4<T1, T2, T3, T4> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp4<>(
                    (T1) components[idx1],
                    (T2) components[idx2],
                    (T3) components[idx3],
                    (T4) components[idx4],
                    longEntity);
        }
    }

    record Comp5Iterator<T1, T2, T3, T4, T5>(int idx1, int idx2, int idx3, int idx4, int idx5,
                                             Iterator<LongEntity> iterator) implements Iterator<Results.Comp5<T1, T2, T3, T4, T5>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp5<T1, T2, T3, T4, T5> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp5<>(
                    (T1) components[idx1],
                    (T2) components[idx2],
                    (T3) components[idx3],
                    (T4) components[idx4],
                    (T5) components[idx5],
                    longEntity);
        }
    }

    record Comp6Iterator<T1, T2, T3, T4, T5, T6>(int idx1, int idx2, int idx3, int idx4, int idx5, int idx6,
                                                 Iterator<LongEntity> iterator) implements Iterator<Results.Comp6<T1, T2, T3, T4, T5, T6>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp6<T1, T2, T3, T4, T5, T6> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp6<>(
                    (T1) components[idx1],
                    (T2) components[idx2],
                    (T3) components[idx3],
                    (T4) components[idx4],
                    (T5) components[idx5],
                    (T6) components[idx6],
                    longEntity);
        }
    }

    record Comp7Iterator<T1, T2, T3, T4, T5, T6, T7>(int idx1, int idx2, int idx3, int idx4, int idx5, int idx6,
                                                     int idx7,
                                                     Iterator<LongEntity> iterator) implements Iterator<Results.Comp7<T1, T2, T3, T4, T5, T6, T7>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp7<T1, T2, T3, T4, T5, T6, T7> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp7<>(
                    (T1) components[idx1],
                    (T2) components[idx2],
                    (T3) components[idx3],
                    (T4) components[idx4],
                    (T5) components[idx5],
                    (T6) components[idx6],
                    (T7) components[idx7],
                    longEntity);
        }
    }

    record Comp8Iterator<T1, T2, T3, T4, T5, T6, T7, T8>(int idx1, int idx2, int idx3, int idx4, int idx5, int idx6,
                                                         int idx7, int idx8,
                                                         Iterator<LongEntity> iterator) implements Iterator<Results.Comp8<T1, T2, T3, T4, T5, T6, T7, T8>> {
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Results.Comp8<T1, T2, T3, T4, T5, T6, T7, T8> next() {
            LongEntity longEntity = iterator.next();
            Object[] components = longEntity.getComponents();
            return new Results.Comp8<>(
                    (T1) components[idx1],
                    (T2) components[idx2],
                    (T3) components[idx3],
                    (T4) components[idx4],
                    (T5) components[idx5],
                    (T6) components[idx6],
                    (T7) components[idx7],
                    (T8) components[idx8],
                    longEntity);
        }
    }
}

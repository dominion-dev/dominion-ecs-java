package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.system.ClassIndex;

import java.util.Iterator;

public final class Composition {
    private final static int componentIndexCapacity = 1 << 10;
    private final Class<?>[] componentTypes;
    private final ConcurrentPool.Tenant<LongEntity> tenant;
    private final ClassIndex classIndex;
    private final int[] componentIndex;

    public Composition(ConcurrentPool.Tenant<LongEntity> tenant, ClassIndex classIndex, Class<?>... componentTypes) {
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
        LongEntity entity = (tenant.register(id, new LongEntity(id, this)));
        return switch (componentTypes.length) {
            case 0 -> entity;
            case 1 -> entity.setSingleComponent(components[0]);
            default -> entity.setComponents(sortComponentsInPlaceByIndex(components));
        };
    }

    public boolean destroyEntity(LongEntity entity) {
        tenant.freeId(entity.getId());
        entity.setComposition(null);
        entity.setSingleComponent(null);
        entity.setComponents(null);
        return true;
    }

    public Class<?>[] getComponentTypes() {
        return componentTypes;
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
            return new Results.Comp2<>((T1) longEntity.getComponents()[idx1], (T2) longEntity.getComponents()[idx2], longEntity);
        }
    }
}

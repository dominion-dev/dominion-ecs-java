/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Results;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.collections.ObjectArrayPool;
import dev.dominion.ecs.engine.system.ClassIndex;

import java.util.Iterator;

public final class Composition {
    public final static int COMPONENT_INDEX_CAPACITY = 1 << 10;
    private final Class<?>[] componentTypes;
    private final CompositionRepository repository;
    private final ConcurrentPool.Tenant<LongEntity> tenant;
    private final ObjectArrayPool arrayPool;
    private final ClassIndex classIndex;
    private final int[] componentIndex;

    public Composition(CompositionRepository repository, ConcurrentPool.Tenant<LongEntity> tenant, ObjectArrayPool arrayPool, ClassIndex classIndex, Class<?>... componentTypes) {
        this.repository = repository;
        this.tenant = tenant;
        this.arrayPool = arrayPool;
        this.classIndex = classIndex;
        this.componentTypes = componentTypes;
        if (isMultiComponent()) {
            componentIndex = new int[COMPONENT_INDEX_CAPACITY];
            for (int i = 0; i < length(); i++) {
                componentIndex[classIndex.getIndex(componentTypes[i])] = i + 1;
            }
        } else {
            componentIndex = null;
        }
    }

    public int length() {
        return componentTypes.length;
    }

    public boolean isMultiComponent() {
        return length() > 1;
    }

    public int fetchComponentIndex(Class<?> componentType) {
        return componentIndex[classIndex.getIndex(componentType)] - 1;
    }

    public boolean hasComponentType(Class<?> componentType) {
        return isMultiComponent() ?
                componentIndex != null && componentIndex[classIndex.getIndex(componentType)] > 0 :
                length() == 1 && componentTypes[0] == componentType;
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

    public LongEntity createEntity(Object... components) {
        long id = tenant.nextId();
        return tenant.register(id, new LongEntity(id, this,
                isMultiComponent() ? sortComponentsInPlaceByIndex(components) : components));
    }

    public LongEntity attachEntity(LongEntity entity, Object... components) {
        long id = tenant.nextId();
        entity.setId(id);
        tenant.register(id, entity);
        return switch (length()) {
            case 0 -> entity.setData(new LongEntity.Data(this, null));
            case 1 -> entity.setData(new LongEntity.Data(this, components));
            default -> entity.setData(new LongEntity.Data(this, sortComponentsInPlaceByIndex(components)));
        };
    }

    public boolean destroyEntity(LongEntity entity) {
        detachEntity(entity);
        Object[] components = entity.getComponents();
        if (components != null && entity.isComponentArrayFromCache()) {
            arrayPool.push(components);
        }
        entity.setData(null);
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
        int idx = componentIndex == null ? 0 : fetchComponentIndex(type);
        return new Comp1Iterator<>(idx, tenant.iterator());
    }

    public <T1, T2> Iterator<Results.Comp2<T1, T2>> select(Class<T1> type1, Class<T2> type2) {
        return new Comp2Iterator<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                tenant.iterator());
    }

    public <T1, T2, T3> Iterator<Results.Comp3<T1, T2, T3>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        return new Comp3Iterator<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                tenant.iterator());
    }

    public <T1, T2, T3, T4> Iterator<Results.Comp4<T1, T2, T3, T4>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        return new Comp4Iterator<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                fetchComponentIndex(type4),
                tenant.iterator());
    }

    public <T1, T2, T3, T4, T5> Iterator<Results.Comp5<T1, T2, T3, T4, T5>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5) {
        return new Comp5Iterator<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                fetchComponentIndex(type4),
                fetchComponentIndex(type5),
                tenant.iterator());
    }

    public <T1, T2, T3, T4, T5, T6> Iterator<Results.Comp6<T1, T2, T3, T4, T5, T6>> select(Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4, Class<T5> type5, Class<T6> type6) {
        return new Comp6Iterator<>(
                fetchComponentIndex(type1),
                fetchComponentIndex(type2),
                fetchComponentIndex(type3),
                fetchComponentIndex(type4),
                fetchComponentIndex(type5),
                fetchComponentIndex(type6),
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
            T comp = (T) longEntity.getComponents()[idx];
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
}

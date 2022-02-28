/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.system.UncheckedReferenceUpdater;

import java.util.concurrent.locks.StampedLock;

public final class IntEntity implements Entity, ConcurrentPool.Identifiable {
    private static final int componentArrayFromPoolBit = 1 << 31;
    private static final UncheckedReferenceUpdater<IntEntity, StampedLock> lockUpdater;

    static {
        UncheckedReferenceUpdater<IntEntity, StampedLock> updater = null;
        try {
            updater = new UncheckedReferenceUpdater<>(IntEntity.class, "lock");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        lockUpdater = updater;
    }

    private int id;
    private int prevId;
    private int nextId;
    private volatile Data data;
    @SuppressWarnings("unused")
    private volatile StampedLock lock;

    public IntEntity(int id, Composition composition, Object... components) {
        this.id = id;
        data = new Data(composition, components);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int setId(int id) {
        return this.id = id | (this.id & componentArrayFromPoolBit);
    }

    @Override
    public int getPrevId() {
        return prevId;
    }

    @Override
    public int setPrevId(int prevId) {
        return this.prevId = prevId;
    }

    @Override
    public int getNextId() {
        return nextId;
    }

    @Override
    public int setNextId(int nextId) {
        return this.nextId = nextId;
    }

    public Composition getComposition() {
        return data.composition;
    }

    public Object[] getComponents() {
        return data.components;
    }

    public Data getData() {
        return data;
    }

    IntEntity setData(Data data) {
        this.data = data;
        return this;
    }

    @Override
    public Entity add(Object... components) {
        if (lock == null) {
            lockUpdater.compareAndSet(this, null, new StampedLock());
        }
        long stamp = lock.writeLock();
        try {
            return data.composition.getRepository().addComponents(this, components);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public Object remove(Object component) {
        if (lock == null) {
            lockUpdater.compareAndSet(this, null, new StampedLock());
        }
        long stamp = lock.writeLock();
        try {
            if (!contains(component)) {
                return null;
            }
            return data.composition.getRepository().removeComponentType(this, component.getClass());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public Object removeType(Class<?> componentType) {
        if (lock == null) {
            lockUpdater.compareAndSet(this, null, new StampedLock());
        }
        long stamp = lock.writeLock();
        try {
            if (!has(componentType)) {
                return null;
            }
            return data.composition.getRepository().removeComponentType(this, componentType);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public boolean has(Class<?> componentType) {
        return data.components != null && (
                data.composition.isMultiComponent() ?
                        data.composition.fetchComponentIndex(componentType) > -1 :
                        data.components[0].getClass().equals(componentType));
    }

    @Override
    public boolean contains(Object component) {
        int idx;
        return data.components != null && (
                data.composition.isMultiComponent() ?
                        (idx = data.composition.fetchComponentIndex(component.getClass())) > -1
                                && data.components[idx].equals(component) :
                        data.components[0].equals(component)
        );
    }

    @Override
    public <S extends Enum<S>> void setState(S state) {
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    public boolean isComponentArrayFromCache() {
        return (id & componentArrayFromPoolBit) == componentArrayFromPoolBit;
    }

    void flagComponentArrayFromCache() {
        id |= componentArrayFromPoolBit;
    }

    public record Data(Composition composition, Object[] components) {
    }
}

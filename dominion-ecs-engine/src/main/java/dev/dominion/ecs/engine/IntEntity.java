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
    private static final int DETACHED_ID_BIT = 1 << 31;
    private static final int COMPONENT_ARRAY_FROM_POOL_BIT = 1 << 30;
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
    private int prevId = -1;
    private int nextId = -1;
    private volatile Data data;
    @SuppressWarnings("unused")
    private volatile StampedLock lock;

    public IntEntity(int id, Composition composition, Object... components) {
        this.id = id;
        data = new Data(composition, components);
    }

    private static String idToString(int id) {
        return "|" + id + "|";
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int setId(int id) {
        return this.id = id | (this.id & COMPONENT_ARRAY_FROM_POOL_BIT);
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
        createLock();
        long stamp = lock.writeLock();
        try {
            if (isDetachedId()) {
                return null;
            }
            return data.composition.getRepository().addComponents(this, components);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public Object remove(Object component) {
        createLock();
        long stamp = lock.writeLock();
        try {
            if (isDetachedId() || !contains(component)) {
                return null;
            }
            return data.composition.getRepository().removeComponentType(this, component.getClass());
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public Object removeType(Class<?> componentType) {
        createLock();
        long stamp = lock.writeLock();
        try {
            if (isDetachedId() || !has(componentType)) {
                return null;
            }
            return data.composition.getRepository().removeComponentType(this, componentType);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public boolean has(Class<?> componentType) {
        var data = this.data;
        return data.components != null && (
                data.composition.isMultiComponent() ?
                        data.composition.fetchComponentIndex(componentType) > -1 :
                        data.components[0].getClass().equals(componentType));
    }

    @Override
    public boolean contains(Object component) {
        int idx;
        var data = this.data;
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
    public boolean isEnabled() {
        return !isDetachedId();
    }

    @Override
    public void setEnabled(boolean enabled) {
        createLock();
        long stamp = lock.writeLock();
        try {
            if (enabled && isDetachedId()) {
                data.composition.reattachEntity(this);
            } else if (!enabled && isEnabled()) {
                data.composition.detachEntity(this);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    boolean delete() {
        createLock();
        long stamp = lock.writeLock();
        try {
            if (isDetachedId()) {
                return false;
            }
            return data.composition.deleteEntity(this);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void createLock() {
        if (lock == null) {
            lockUpdater.compareAndSet(this, null, new StampedLock());
        }
    }

    public boolean isComponentArrayFromCache() {
        return (id & COMPONENT_ARRAY_FROM_POOL_BIT) == COMPONENT_ARRAY_FROM_POOL_BIT;
    }

    void flagComponentArrayFromPool() {
        id |= COMPONENT_ARRAY_FROM_POOL_BIT;
    }

    boolean isDetachedId() {
        return (id & DETACHED_ID_BIT) == DETACHED_ID_BIT;
    }

    void flagDetachedId() {
        id |= DETACHED_ID_BIT;
    }

    @Override
    public String toString() {
        return "IntEntity{" +
                "id=" + idToString(id) +
                ", prevId=" + idToString(prevId) +
                ", nextId=" + idToString(nextId) +
                '}';
    }

    public record Data(Composition composition, Object[] components) {
    }
}

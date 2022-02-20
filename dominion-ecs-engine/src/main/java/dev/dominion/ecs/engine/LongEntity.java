/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ConcurrentPool;
import dev.dominion.ecs.engine.system.UncheckedReferenceUpdater;

import java.util.concurrent.locks.StampedLock;

public final class LongEntity implements Entity, ConcurrentPool.Identifiable {
    private static final UncheckedReferenceUpdater<LongEntity, StampedLock> lockUpdater;

    static {
        UncheckedReferenceUpdater<LongEntity, StampedLock> updater = null;
        try {
            updater = new UncheckedReferenceUpdater<>(LongEntity.class, StampedLock.class, "lock");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        lockUpdater = updater;
    }

    private long id;
    private Composition composition;
    private Object singleComponent;
    private Object[] components;
    private boolean isComponentArrayFromCache;
    @SuppressWarnings("unused")
    private volatile StampedLock lock;


    public LongEntity(long id, Composition composition) {
        this.id = id;
        this.composition = composition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    public Object getSingleComponent() {
        return singleComponent;
    }

    public LongEntity setSingleComponent(Object singleComponent) {
        this.singleComponent = singleComponent;
        return this;
    }

    public Object[] getComponents() {
        return components;
    }

    public LongEntity setComponents(Object[] components) {
        this.components = components;
        return this;
    }

    @Override
    public Entity add(Object... components) {
        if (lock == null) {
            lockUpdater.compareAndSet(this, null, new StampedLock());
        }
        long stamp = lock.writeLock();
        try {
            return composition.getRepository().addComponents(this, components);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public Entity remove(Object... components) {
        return null;
    }

    @Override
    public boolean has(Class<?> componentType) {
        return composition.hasComponentType(componentType);
    }

    @Override
    public boolean contains(Object component) {
        int idx;
        return singleComponent != null ?
                singleComponent.equals(component) :
                components != null
                        && (idx = composition.fetchComponentIndex(component.getClass())) > -1
                        && components[idx].equals(component);
    }

    @Override
    public <S extends Enum<S>> void setState(S state) {
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    public boolean isComponentArrayFromCache() {
        return isComponentArrayFromCache;
    }

    void flagComponentArrayFromCache() {
        isComponentArrayFromCache = true;
    }
}

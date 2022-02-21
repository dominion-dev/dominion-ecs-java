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
            updater = new UncheckedReferenceUpdater<>(LongEntity.class, "lock");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        lockUpdater = updater;
    }

    private long id;
    private volatile Data data;
    private boolean isComponentArrayFromCache;
    @SuppressWarnings("unused")
    private volatile StampedLock lock;

    public LongEntity(long id, Composition composition, Object... components) {
        this.id = id;
        data = new Data(composition, components);
    }

    public long getId() {
        return id;
    }

    public long setId(long id) {
        return this.id = id;
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

    LongEntity setData(Data data) {
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
    public Entity remove(Object... components) {
        return null;
    }

    @Override
    public boolean has(Class<?> componentType) {
        return data.composition.hasComponentType(componentType);
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
        return isComponentArrayFromCache;
    }

    void flagComponentArrayFromCache() {
        isComponentArrayFromCache = true;
    }

    public record Data(Composition composition, Object[] components) {
    }
}

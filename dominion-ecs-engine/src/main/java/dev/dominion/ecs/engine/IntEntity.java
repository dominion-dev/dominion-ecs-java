/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.Item;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public final class IntEntity implements Entity, Item {
    private static final AtomicIntegerFieldUpdater<IntEntity> ID_UPDATER = AtomicIntegerFieldUpdater.newUpdater(IntEntity.class, "id");
    private static final AtomicIntegerFieldUpdater<IntEntity> STATE_ID_UPDATER = AtomicIntegerFieldUpdater.newUpdater(
            IntEntity.class,
            "stateId"
    );
    private final ChunkedPool<IntEntity> pool;
    private volatile int id;
    private volatile int stateId;
    private Object[] shelf;

    public IntEntity(int id, ChunkedPool<IntEntity> pool) {
        this.id = id;
        this.stateId = ChunkedPool.IdSchema.DETACHED_BIT;
        this.pool = pool;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public synchronized void setId(int id) {
        if(this.id == id) return;
        if(this.id > 0) {
            pool.getChunk(this.id).incrementRmCount();
        }
        this.id = id;
    }

    @Override
    public void compareAndSetId(int expect, int id) {
        ID_UPDATER.compareAndSet(this, expect, id);
    }

    public int getStateId() {
        return stateId;
    }

    @Override
    public synchronized void setStateId(int stateId) {
        if(this.stateId == stateId) return;
        if(this.stateId > 0) {
            pool.getChunk(this.stateId).incrementRmCount();
        }
        this.stateId = stateId;
    }

    @Override
    public void compareAndSetStateId(int expect, int id) {
        STATE_ID_UPDATER.compareAndSet(this, expect, id);
    }

    public DataComposition getComposition() {
        return (DataComposition) getChunk().getTenant().getOwner();
    }

    public ChunkedPool.LinkedChunk<IntEntity> getChunk() {
        return id < 0 ? null : pool.getChunk(id);
    }

    public ChunkedPool.LinkedChunk<IntEntity> getStateChunk() {
        return stateId < 0 ? null : pool.getChunk(stateId);
    }

    public Object[] getComponentArray() {
        if (getChunk() == null || getArrayLength() == 0) {
            return null;
        }
        return getChunk().getData(id);
    }

    public int getArrayLength() {
        return getChunk().getDataLength();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public synchronized Entity add(Object component) {
        if (!isEnabled()) {
            return this;
        }
        return getComposition().getRepository().addComponent(this, component);
    }

    @Override
    public synchronized boolean remove(Object component) {
        if (!isEnabled()) {
            return false;
        }
        return getComposition().getRepository().removeComponentType(this, component.getClass());
    }

    public synchronized boolean modify(
            CompositionRepository compositions, PreparedComposition.TargetComposition targetComposition,
            Object addedComponent, Object[] addedComponents
    ) {
        if (!isEnabled()) {
            return false;
        }
        compositions.modifyComponents(this, targetComposition, addedComponent, addedComponents);
        return true;
    }

    @Override
    public synchronized boolean removeType(Class<?> componentType) {
        if (!isEnabled()) {
            return false;
        }
        return getComposition().getRepository().removeComponentType(this, componentType);
    }

    @Override
    public boolean has(Class<?> componentType) {
        while (true) {
            final var chunk = getChunk();
            final var result = switch (chunk.getDataLength()) {
                case 0 -> false;
                case 1 -> chunk.getFromDataArray(id).getClass().equals(componentType);
                default -> {
                    DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
                    yield composition.fetchComponentIndex(componentType) > -1;
                }
            };
            if (getChunk() == chunk) {
                return result;
            }
        }
    }

    @Override
    public boolean contains(Object component) {
        while (true) {
            final var chunk = getChunk();
            final var result = switch (chunk.getDataLength()) {
                case 0 -> false;
                case 1 -> chunk.getFromDataArray(id).equals(component);
                default -> {
                    DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
                    int idx;
                    yield (idx = composition.fetchComponentIndex(component.getClass())) > -1 && chunk.getData(id)[idx].equals(component);
                }
            };
            if (getChunk() == chunk) {
                return result;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> componentType) {
        while (true) {
            final var chunk = getChunk();
            final T result = switch (chunk.getDataLength()) {
                case 0 -> null;
                case 1 -> {
                    T fromDataArray = (T) chunk.getFromDataArray(id);
                    yield fromDataArray.getClass().equals(componentType) ? fromDataArray : null;
                }
                default -> {
                    DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
                    int componentIndex = composition.fetchComponentIndex(componentType);
                    yield componentIndex > -1 ? (T) chunk.getFromMultiDataArray(id, componentIndex) : null;
                }
            };
            if (getChunk() == chunk) {
                return result;
            }
        }
    }

    @SuppressWarnings("resource")
    @Override
    public synchronized <S extends Enum<S>> Entity setState(S state) {
        if (!isEnabled()) {
            return this;
        }
        if (state == null && stateId > -1) {
            setStateId(ChunkedPool.IdSchema.DETACHED_BIT);
            return this;
        }
        DataComposition composition = getComposition();
        if (stateId > -1) {
            var tenant = getStateChunk().getTenant();
            if (tenant == composition.getStateTenant(state)) {
                return this;
            }
            setStateId(ChunkedPool.IdSchema.DETACHED_BIT);
        }
        composition.fetchStateTenants(state).registerState(this);
        return this;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted() && shelf == null;
    }

    @Override
    public synchronized Entity setEnabled(boolean enabled) {
        if (enabled && shelf != null) {
            final var chunk = pool.getChunk(-id);
            final var tenant = chunk.getTenant();
            this.id = tenant.nextId();
            tenant.register(this, shelf);
            shelf = null;
        } else if (!enabled && shelf == null) {
            final var chunk = getChunk();
            shelf = chunk.getData(id);
            id = -id;
            chunk.incrementRmCount();
        }
        return this;
    }

    synchronized boolean delete() {
        id = ChunkedPool.IdSchema.DETACHED_BIT;
        stateId = ChunkedPool.IdSchema.DETACHED_BIT;
        return true;
    }

    @Override
    public boolean isDeleted() {
        return (id & ChunkedPool.IdSchema.DETACHED_BIT) == ChunkedPool.IdSchema.DETACHED_BIT;
    }

    @Override
    public String toString() {
        ChunkedPool.IdSchema idSchema = getComposition().getIdSchema();
        return "Entity={" +
                "id=" + idSchema.fetchChunkId(id) + ", " +
                "id=" + idSchema.idToString(id) + "-> " + Arrays.toString(getComponentArray()) + ", " +
                "stateId=" + idSchema.idToString(stateId) + ", " +
                "enabled=" + isEnabled() +
                "}";
    }
}

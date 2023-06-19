/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.Item;

import java.util.Arrays;

public final class IntEntity implements Entity, Item {
    ChunkedPool.LinkedChunk<IntEntity> chunk;
    ChunkedPool.LinkedChunk<IntEntity> stateChunk;
    private int id;
    private int stateId;
    private Object[] shelf;

    public IntEntity(int id) {
        this.id = id;
        this.stateId = ChunkedPool.IdSchema.DETACHED_BIT;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getStateId() {
        return stateId;
    }

    @Override
    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public DataComposition getComposition() {
        return (DataComposition) chunk.getTenant().getOwner();
    }

    public ChunkedPool.LinkedChunk<IntEntity> getChunk() {
        return chunk;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setChunk(ChunkedPool.LinkedChunk<? extends Item> chunk) {
        this.chunk = (ChunkedPool.LinkedChunk<IntEntity>) chunk;
    }

    public ChunkedPool.LinkedChunk<IntEntity> getStateChunk() {
        return stateChunk;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setStateChunk(ChunkedPool.LinkedChunk<? extends Item> chunk) {
        this.stateChunk = (ChunkedPool.LinkedChunk<IntEntity>) chunk;
    }

    public Object[] getComponentArray() {
        if (chunk == null || getArrayLength() == 0) {
            return null;
        }
        return chunk.getData(id);
    }

    public int getArrayLength() {
        return chunk.getDataLength();
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

    public synchronized boolean modify(CompositionRepository compositions, PreparedComposition.TargetComposition targetComposition,
                                       Object addedComponent, Object[] addedComponents) {
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
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return false;
        if (dataLength == 1) {
            return chunk.getFromDataArray(id).getClass().equals(componentType);
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        return composition.fetchComponentIndex(componentType) > -1;
    }

    @Override
    public boolean contains(Object component) {
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return false;
        if (dataLength == 1) {
            return chunk.getFromDataArray(id).equals(component);
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        int idx;
        return (idx = composition.fetchComponentIndex(component.getClass())) > -1
                && chunk.getData(getId())[idx].equals(component);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> componentType) {
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return null;
        if (dataLength == 1) {
            T fromDataArray = (T) chunk.getFromDataArray(id);
            return fromDataArray.getClass().equals(componentType) ? fromDataArray : null;
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        int componentIndex = composition.fetchComponentIndex(componentType);
        return componentIndex > -1 ? (T) chunk.getFromMultiDataArray(id, componentIndex) : null;
    }

    @SuppressWarnings("resource")
    @Override
    public synchronized <S extends Enum<S>> Entity setState(S state) {
        if (!isEnabled()) {
            return this;
        }
        if (state == null && stateChunk != null) {
            ChunkedPool.Tenant<IntEntity> tenant;
            synchronized (tenant = stateChunk.getTenant()) {
                tenant.freeStateId(stateId);
                stateChunk = null;
                return this;
            }
        }
        DataComposition composition = getComposition();
        if (stateChunk != null) {
            var tenant = stateChunk.getTenant();
            if (tenant == composition.getStateTenant(state)) {
                return this;
            }
            synchronized (stateChunk.getTenant()) {
                tenant.freeStateId(stateId);
            }
        }
        ChunkedPool.Tenant<IntEntity> tenant;
        synchronized (tenant = composition.fetchStateTenants(state)) {
            stateChunk = tenant.registerState(this);
        }
        return this;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted() && shelf == null;
    }

    @Override
    public synchronized Entity setEnabled(boolean enabled) {
        if (enabled && !isEnabled()) {
            synchronized (chunk.getTenant()) {
                chunk.unshelve(this, shelf);
                shelf = null;
            }
        } else if (!enabled && isEnabled()) {
            synchronized (chunk.getTenant()) {
                shelf = chunk.shelve(this);
            }
        }
        return this;
    }

    synchronized boolean delete() {
        ChunkedPool.Tenant<IntEntity> tenant;
        synchronized (tenant = chunk.getTenant()) {
            tenant.freeId(id);
            flagDetachedId();
            chunk = null;
            shelf = null;
        }
        if (stateChunk != null) {
            synchronized (tenant = stateChunk.getTenant()) {
                tenant.freeStateId(stateId);
                stateChunk = null;
            }
        }
        return true;
    }

    @Override
    public boolean isDeleted() {
        return (id & ChunkedPool.IdSchema.DETACHED_BIT) == ChunkedPool.IdSchema.DETACHED_BIT;
    }

    void flagDetachedId() {
        setId(id | ChunkedPool.IdSchema.DETACHED_BIT);
    }

    @Override
    public String toString() {
        ChunkedPool.IdSchema idSchema = getComposition().getIdSchema();
        return "Entity={" +
                "id=" + idSchema.idToString(id) + "-> " + Arrays.toString(getComponentArray()) + ", " +
                "stateId=" + idSchema.idToString(stateId) + ", " +
                "enabled=" + isEnabled() +
                "}";
    }
}

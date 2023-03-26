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
    public Entity add(Object component) {
        synchronized (this) {
            if (!isEnabled()) {
                return this;
            }
            return getComposition().getRepository().addComponent(this, component);
        }
    }

    @Override
    public boolean remove(Object component) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            return getComposition().getRepository().removeComponentType(this, component.getClass());
        }
    }

    public boolean modify(CompositionRepository compositions, PreparedComposition.TargetComposition targetComposition,
                          Object addedComponent, Object[] addedComponents) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            compositions.modifyComponents(this, targetComposition, addedComponent, addedComponents);
            return true;
        }
    }

    @Override
    public boolean removeType(Class<?> componentType) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            return getComposition().getRepository().removeComponentType(this, componentType);
        }
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

    @Override
    public Object get(Class<?> componentType) {
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return null;
        if (dataLength == 1) {
            Object fromDataArray = chunk.getFromDataArray(id);
            return fromDataArray.getClass().equals(componentType) ? fromDataArray : null;
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        int componentIndex = composition.fetchComponentIndex(componentType);
        return componentIndex > -1 ? chunk.getFromMultiDataArray(id, componentIndex) : null;
    }

    @SuppressWarnings("resource")
    @Override
    public <S extends Enum<S>> Entity setState(S state) {
        synchronized (this) {
            if (!isEnabled()) {
                return this;
            }
            if (state == null && stateChunk != null) {
                stateChunk.getTenant().freeStateId(stateId);
                stateChunk = null;
                return this;
            }
            DataComposition composition = getComposition();
            if (stateChunk != null) {
                var tenant = stateChunk.getTenant();
                if (tenant == composition.getStateTenant(state)) {
                    return this;
                }
                tenant.freeStateId(stateId);
            }
            stateChunk = composition.fetchStateTenants(state).registerState(this);
            return this;
        }
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted() && shelf == null;
    }

    @Override
    public Entity setEnabled(boolean enabled) {
        if (enabled && !isEnabled()) {
            synchronized (this) {
                chunk.unshelve(this, shelf);
                shelf = null;
            }
        } else if (!enabled && isEnabled()) {
            synchronized (this) {
                shelf = chunk.shelve(this);
            }
        }
        return this;
    }

    boolean delete() {
        synchronized (this) {
            chunk.getTenant().freeId(id);
            flagDetachedId();
            chunk = null;
            if (stateChunk != null) {
                stateChunk.getTenant().freeStateId(stateId);
                stateChunk = null;
            }
            shelf = null;
            return true;
        }
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

/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.Item;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.UncheckedUpdater;

public final class IntEntity implements Entity, Item {
    private static final UncheckedUpdater.Int<IntEntity> idUpdater;

    static {
        UncheckedUpdater.Int<IntEntity> updater = null;
        try {
            updater = new UncheckedUpdater.Int<>(IntEntity.class, "id");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        idUpdater = updater;
    }

    ChunkedPool.LinkedChunk<IntEntity> chunk;
//    private IntEntity prev = null;
//    private IntEntity next = null;

    //    private final Data data;
    @SuppressWarnings("FieldMayBeFinal")
    private volatile int id;

    private Object[] backup;

    public IntEntity(int id, DataComposition composition, String name) {
        this.id = id;
//        data = new Data(composition, name);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int setId(int id) {
        int prev = this.id;
        return idUpdater.compareAndSet(this, prev, id) ? id : prev;
    }

    @Override
    public Item getPrev() {
        return null;
//        return prev;
    }

    @Override
    public void setPrev(Item prev) {
//        this.prev = (IntEntity) prev;
    }

    @Override
    public Item getNext() {
        return null;
//        return next;
    }

    @Override
    public void setNext(Item next) {
//        this.next = (IntEntity) next;
    }

    public DataComposition getComposition() {
        return (DataComposition) chunk.getTenant().getOwner();
        //        return data.composition;
    }

    public IntEntity setComposition(DataComposition composition) {
//        data.composition = composition;
        return this;
    }

    public ChunkedPool.LinkedChunk<IntEntity> getChunk() {
        return chunk;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setChunk(ChunkedPool.LinkedChunk<? extends Item> chunk) {
//        data.chunk = (ChunkedPool.LinkedChunk<IntEntity>) chunk;
        this.chunk = (ChunkedPool.LinkedChunk<IntEntity>) chunk;
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

    public IndexKey getStateRoot() {
//        return data.stateRoot;
        return null;
    }

    public IntEntity setStateRoot(IndexKey stateRoot) {
//        data.stateRoot = stateRoot;
        return this;
    }

    @Override
    public String getName() {
//        return data.name;
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

    public boolean modify(CompositionRepository compositions, PreparedComposition.TargetComposition targetComposition, Object[] addedComponents) {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            compositions.modifyComponents(this, targetComposition, addedComponents);
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
            return chunk.getFromDataArray(getId()).getClass().equals(componentType);
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        return composition.fetchComponentIndex(componentType) > -1;
    }

    @Override
    public boolean contains(Object component) {
        int dataLength;
        if (chunk == null || (dataLength = chunk.getDataLength()) == 0) return false;
        if (dataLength == 1) {
            return chunk.getFromDataArray(getId()).equals(component);
        }
        DataComposition composition = (DataComposition) chunk.getTenant().getOwner();
        int idx;
        return (idx = composition.fetchComponentIndex(component.getClass())) > -1
                && chunk.getData(getId())[idx].equals(component);
    }

    @Override
    public <S extends Enum<S>> Entity setState(S state) {
//        if (!isEnabled()) {
//            return this;
//        }
//        return data.composition.setEntityState(this, state);
        return this;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted() && backup == null;
    }

    @Override
    public Entity setEnabled(boolean enabled) {
        if (enabled && !isEnabled() && !isDeleted()) {
            chunk.renew(this, backup);
            backup = null;
        } else if (!enabled && isEnabled()) {
            backup = chunk.disable(this);
        }
        return this;
    }

    boolean delete() {
        synchronized (this) {
            if (!isEnabled()) {
                return false;
            }
            chunk.getTenant().freeId(id);
            flagDetachedId();
            chunk = null;
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
        return "Entity={id=" + idSchema.idToString(id) + '}';
//        String name = data.name == null ? "Entity" : data.name;
//        return name + "={" +
//                "id=" + idSchema.idToString(id) +
//                ", " + data.composition +
//                ", arrayOffset=" + data.offset +
//                ", stateRootKey=" + data.stateRoot +
//                ", prev.id=" + (prev == null ? null : idSchema.idToString(prev.id)) +
//                ", next.id=" + (next == null ? null : idSchema.idToString(next.id)) +
//                '}';
    }

//    public static class Data {
//        private final String name;
//        private final DataComposition composition;
//        public ChunkedPool.LinkedChunk<IntEntity> chunk;
//        private Object[] components;
//        private int offset;
//        private IndexKey stateRoot;
//
//        public Data(DataComposition composition, String name) {
//            this.composition = composition;
//            this.name = name;
//        }
//
//        public DataComposition composition() {
//            return composition;
//        }
//
//        public String name() {
//            return name;
//        }
//
//        public IndexKey stateRoot() {
//            return stateRoot;
//        }
//    }
}

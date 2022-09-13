/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine;

import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.engine.collections.ChunkedPool;
import dev.dominion.ecs.engine.collections.ChunkedPool.Identifiable;
import dev.dominion.ecs.engine.system.IndexKey;
import dev.dominion.ecs.engine.system.UncheckedUpdater;

public final class IntEntity implements Entity, Identifiable {
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

    @SuppressWarnings("FieldMayBeFinal")
    private volatile int id;
    //    private final int id;
    private IntEntity prev = null;
    private IntEntity next = null;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile Data data;
//    private final Data data;

    public IntEntity(int id, DataComposition composition, String name) {
        this.id = id;
        data = new Data(composition, name);
    }

    public static boolean isLocked(int id) {
        return (id & ChunkedPool.IdSchema.LOCK_BIT) == ChunkedPool.IdSchema.LOCK_BIT;
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
    public Identifiable getPrev() {
        return prev;
    }

    @Override
    public Identifiable setPrev(Identifiable prev) {
        Identifiable old = this.prev;
        this.prev = (IntEntity) prev;
        return old;
    }

    @Override
    public Identifiable getNext() {
        return next;
    }

    @Override
    public Identifiable setNext(Identifiable next) {
        Identifiable old = this.next;
        this.next = (IntEntity) next;
        return old;
    }

    @Override
    public void setArray(Object[] components, int offset) {
        data.components = components;
        data.offset = offset;
    }

    @Override
    public int getOffset() {
        return data.offset;
    }

    public DataComposition getComposition() {
        return data.composition;
    }

    public IntEntity setComposition(DataComposition composition) {
        data.composition = composition;
        return this;
    }

    public Object[] cloneComponentArray() {
        int arrayLength;
        if (getArray() == null || (arrayLength = getArrayLength()) == 0) {
            return null;
        }
        Object[] components = new Object[arrayLength];
        System.arraycopy(getArray(), getArrayOffset(), components, 0, arrayLength);
        return components;
    }

    public Object[] getArray() {
        return data.components;
    }

    public int getArrayOffset() {
        return data.offset;
    }

    public int getArrayLength() {
        return data.composition.length();
    }

    public Data getData() {
        return data;
    }

    public IndexKey getStateRoot() {
        return data.stateRoot;
    }

    public IntEntity setStateRoot(IndexKey stateRoot) {
        data.stateRoot = stateRoot;
        return this;
    }

    @Override
    public String getName() {
        return data.name;
    }

    @Override
    public Entity add(Object component) {
//        lock();
//        try {
        synchronized (this) {
            if (!isEnabled()) {
                return this;
            }
            return data.composition.getRepository().addComponent(this, component);
        }
//        } finally {
//            unlock();
//        }
    }

    @Override
    public boolean remove(Object component) {
//        lock();
//        try {
        synchronized (this) {

            if (!isEnabled()) {
                return false;
            }
            return data.composition.getRepository().removeComponentType(this, component.getClass());
        }
//        } finally {
//            unlock();
//        }
    }

    public boolean modify(CompositionRepository compositions, DataComposition newDataComposition, Object[] newComponentArray) {
//        lock();
//        try {
        synchronized (this) {

            if (!isEnabled()) {
                return false;
            }
            compositions.modifyComponents(this, newDataComposition, newComponentArray);
            return true;
        }
//        } finally {
//            unlock();
//        }
    }

    @Override
    public boolean removeType(Class<?> componentType) {
//        lock();
//        try {
        synchronized (this) {

            if (!isEnabled()) {
                return false;
            }
            return data.composition.getRepository().removeComponentType(this, componentType);
        }
//        } finally {
//            unlock();
//        }
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
    public <S extends Enum<S>> Entity setState(S state) {
        if (!isEnabled()) {
            return this;
        }
        return data.composition.setEntityState(this, state);
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted() && data.offset > -1;
    }

    @Override
    public Entity setEnabled(boolean enabled) {
        if (enabled && !isEnabled() && !isDeleted()) {
            data.composition.reEnableEntity(this);
        } else if (!enabled && isEnabled()) {
            data.offset = -1;
        }
        return this;
    }

    boolean delete() {
//        lock();
//        try {
        synchronized (this) {

            if (!isEnabled()) {
                return false;
            }
            data.composition.detachEntityAndState(this);
            return true;
        }
//        } finally {
//            unlock();
//        }
    }

    @Override
    public boolean isDeleted() {
        return (id & ChunkedPool.IdSchema.DETACHED_BIT) == ChunkedPool.IdSchema.DETACHED_BIT;
    }

    void flagDetachedId() {
        setId(id | ChunkedPool.IdSchema.DETACHED_BIT);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void lock() {
        int prev;
        while (isLocked(prev = id)) {
        }
        boolean set = idUpdater.compareAndSet(this, prev, prev | ChunkedPool.IdSchema.LOCK_BIT);
        if (!set) {
//            System.out.println("rec");
//            unsafe.storeFence();
            lock();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void unlock() {
        int prev;
        while (!idUpdater.compareAndSet(this, prev = id, prev ^ ChunkedPool.IdSchema.LOCK_BIT)) {
        }
    }

    @Override
    public String toString() {
        ChunkedPool.IdSchema idSchema = data.composition.getIdSchema();
        String name = data.name == null ? "Entity" : data.name;
        return name + "={" +
                "id=" + idSchema.idToString(id) +
                ", " + data.composition +
                ", arrayOffset=" + data.offset +
                ", stateRootKey=" + data.stateRoot +
                ", prev.id=" + (prev == null ? null : idSchema.idToString(prev.id)) +
                ", next.id=" + (next == null ? null : idSchema.idToString(next.id)) +
                '}';
    }

//    public record Data(DataComposition composition, Object[] components, String name, IndexKey stateRoot, int offset) {
//        public Data(DataComposition composition, Object[] components, Data other) {
//            this(composition, components, other == null ? null : other.name, other == null ? null : other.stateRoot, other == null ? 0 : other.offset);
//        }
//    }

    public static class Data {
        private final String name;
        private DataComposition composition;
        private Object[] components;
        private int offset;
        private IndexKey stateRoot;

        public Data(DataComposition composition, String name) {
            this.composition = composition;
            this.name = name;
        }

        public DataComposition composition() {
            return composition;
        }

        public Object[] components() {
            return components;
        }

        public String name() {
            return name;
        }

        public IndexKey stateRoot() {
            return stateRoot;
        }

        public int offset() {
            return offset;
        }
    }
}

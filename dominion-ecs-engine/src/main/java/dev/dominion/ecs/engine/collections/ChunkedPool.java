/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.Logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ChunkedPool class is the core of the Dominion project.
 * This custom data structure implements multi-tenant management of a pool of items organized into linked
 * chunks to improve performance and have dynamic capacity.
 *
 * @param <T> the managed type that must implement the {@link Item} interface
 */
public final class ChunkedPool<T extends ChunkedPool.Item> implements AutoCloseable {
    private static final System.Logger LOGGER = Logging.getLogger();
    private final LinkedChunk<T>[] chunks;
    private final List<Tenant<T>> tenants = new ArrayList<>();
    private final IdSchema idSchema;
    private final Logging.Context loggingContext;
    private int chunkIndex = -1;

    @SuppressWarnings("unchecked")
    public ChunkedPool(IdSchema idSchema, Logging.Context loggingContext) {
        this.idSchema = idSchema;
        this.loggingContext = loggingContext;
        if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                            , "Creating " + this
                    )
            );
        }
        chunks = new LinkedChunk[idSchema.chunkCount];
    }

    @Override
    public String toString() {
        return "ChunkedPool={"
                + "chunkCount=" + idSchema.chunkCount
                + ", chunkCapacity=" + idSchema.chunkCapacity
                + '}';
    }

    private LinkedChunk<T> newChunk(Tenant<T> owner, LinkedChunk<T> previousChunk) {
        int id = ++chunkIndex;
        if (id > idSchema.chunkCount - 1) {
            throw new OutOfMemoryError(ChunkedPool.class.getName() + ": cannot create a new memory chunk");
        }
        LinkedChunk<T> newChunk = new LinkedChunk<>(id, idSchema, previousChunk, owner.dataLength, owner, loggingContext);
        if (previousChunk != null) {
            previousChunk.setNext(newChunk);
        }
        chunks[id] = newChunk;
        return newChunk;
    }

    private LinkedChunk<T> getChunk(int id) {
        return chunks[idSchema.fetchChunkId(id)];
    }

    public T getEntry(int id) {
        return getChunk(id).get(id);
    }

    public Tenant<T> newTenant() {
        return newTenant(0, null, null);
    }

    public Tenant<T> newTenant(int dataLength, Object owner, Object subject) {
        Tenant<T> newTenant = new Tenant<>(this, idSchema, dataLength, owner, subject, loggingContext);
        tenants.add(newTenant);
        return newTenant;
    }

    public int size() {
        int sum = 0;
        for (int i = 0; i <= chunkIndex; i++) {
            var chunk = chunks[i];
            sum += chunk.size();
        }
        return sum;
    }

    @Override
    public void close() {
        tenants.forEach(Tenant::close);
    }

    public interface Item {

        int getId();

        void setId(int id);

        void setStateId(int id);

        LinkedChunk<? extends Item> getChunk();

        void setChunk(LinkedChunk<? extends Item> chunk);

        void setStateChunk(LinkedChunk<? extends Item> chunk);
    }

    public interface PoolIteratorNextWith1 {
        Object fetchNext(Object[] dataArray, int next, Item item);

        Object fetchNext(Object[][] multiDataArray, int i1, int next, Item item);
    }

    public interface PoolIteratorNextWith2 {
        Object fetchNext(Object[][] multiDataArray, int i1, int i2, int next, Item item);
    }

    public interface PoolIteratorNextWith3 {
        Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int next, Item item);
    }

    public interface PoolIteratorNextWith4 {
        Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int i4, int next, Item item);
    }

    public interface PoolIteratorNextWith5 {
        Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int i4, int i5, int next, Item item);
    }

    public interface PoolIteratorNextWith6 {
        Object fetchNext(Object[][] multiDataArray, int i1, int i2, int i3, int i4, int i5, int i6, int next, Item item);
    }

    // |--FLAGS--|--CHUNK_ID--|--OBJECT_ID--|
    public record IdSchema(int chunkBit
            , int chunkCount, int chunkIdBitMask, int chunkIdBitMaskShifted
            , int chunkCapacity, int objectIdBitMask
    ) {
        public static final int TOTAL_BIT = 31;
        public static final int MIN_CHUNK_BIT = 8;
        public static final int MAX_CHUNK_BIT = 16;
        public static final int DETACHED_BIT_IDX = 31;
        public static final int DETACHED_BIT = 1 << DETACHED_BIT_IDX;

        public IdSchema(int chunkBit) {
            this(chunkBit
                    , 1 << (TOTAL_BIT - chunkBit)
                    , (1 << (TOTAL_BIT - chunkBit)) - 1
                    , ((1 << (TOTAL_BIT - chunkBit)) - 1) << chunkBit
                    , 1 << Math.min(chunkBit, MAX_CHUNK_BIT)
                    , (1 << chunkBit) - 1
            );
        }

        public String idToString(int id) {
            return "|" + ((id & DETACHED_BIT) >>> DETACHED_BIT_IDX)
                    + ":" + (fetchChunkId(id))
                    + ":" + (fetchObjectId(id))
                    + "|";
        }

        public int createId(int chunkId, int objectId) {
            return (chunkId & chunkIdBitMask) << chunkBit
                    | (objectId & objectIdBitMask);
        }

        public int mergeId(int id, int objectId) {
            return (id & chunkIdBitMaskShifted) | objectId;
        }

        public int fetchChunkId(int id) {
            return (id >>> chunkBit) & chunkIdBitMask;
        }

        public int fetchObjectId(int id) {
            return id & objectIdBitMask;
        }
    }

    public static final class Tenant<T extends Item> implements AutoCloseable {
        private static final AtomicInteger idGenerator = new AtomicInteger();
        private final int id = idGenerator.getAndIncrement();
        private final ChunkedPool<T> pool;
        private final IdSchema idSchema;
        private final IntStack idStack;
        private final LinkedChunk<T> firstChunk;
        private final Logging.Context loggingContext;
        private final int dataLength;
        private final Object owner;
        private final Object subject;
        private LinkedChunk<T> currentChunk;
        private int nextId = IdSchema.DETACHED_BIT;

        private Tenant(ChunkedPool<T> pool, IdSchema idSchema, int dataLength, Object owner, Object subject, Logging.Context loggingContext) {
            this.pool = pool;
            this.idSchema = idSchema;
            this.dataLength = dataLength;
            this.owner = owner;
            this.subject = subject;
            this.loggingContext = loggingContext;
            idStack = new IntStack(IdSchema.DETACHED_BIT, idSchema.chunkCapacity);
            currentChunk = pool.newChunk(this, null);
            firstChunk = currentChunk;
            nextId();
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                                , "Creating " + this
                        )
                );
            }
        }

        @Override
        public String toString() {
            return "Tenant={" +
                    "id=" + id +
                    ", dataLength=" + dataLength +
                    ", nextId=" + idSchema.idToString(nextId) +
                    ", subject=" + subject +
                    '}';
        }

        public int nextId() {
            boolean loggable = Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE);
            if (loggable) {
                LOGGER.log(
                        System.Logger.Level.TRACE, Logging.format(loggingContext.subject()
                                , "Getting nextId from " + currentChunk
                                        + " having current nextId " + idSchema.idToString(nextId)
                        )
                );
            }
            synchronized (this) {
                int returnValue = idStack.pop();
                if (loggable) {
                    LOGGER.log(
                            System.Logger.Level.TRACE, Logging.format(loggingContext.subject()
                                    , "Popping nextId:" + idSchema.idToString(returnValue)
                            )
                    );
                }
                if (returnValue != IdSchema.DETACHED_BIT) {
                    pool.getChunk(returnValue).incrementIndex();
                    return returnValue;
                }
                returnValue = nextId;
                if (currentChunk.index < idSchema.chunkCapacity - 1) {
                    nextId = idSchema.createId(currentChunk.id, currentChunk.incrementIndex());
                    return returnValue;
                }
                currentChunk = pool.newChunk(this, currentChunk);
                nextId = idSchema.createId(currentChunk.id, currentChunk.incrementIndex());
                return returnValue;
            }
        }

        public int freeId(int id) {
            return freeId(id, true, false);
        }

        public int freeStateId(int stateId) {
            return freeId(stateId, true, true);
        }

        public int freeId(int id, boolean check, boolean isState) {
            LinkedChunk<T> chunkById = pool.getChunk(id);
            if (check && (chunkById == null || chunkById.tenant != this)) {
                throw new IllegalArgumentException("Invalid chunkById [" + chunkById + "] retrieved by [" + id + "]");
            }
            boolean loggable = Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE);
            synchronized (this) {
                if (chunkById.isEmpty()) {
                    return id;
                }
                int reusableId = chunkById.remove(id, isState);
                if (loggable) {
                    LOGGER.log(
                            System.Logger.Level.TRACE, Logging.format(loggingContext.subject()
                                    , "Freeing " + (isState ? "stateId" : "id") + "=" + idSchema.idToString(id)
                                            + " > reusableId=" + idSchema.idToString(reusableId)
                                            + " having current " + currentChunk

                            )
                    );
                }
                if (reusableId == IdSchema.DETACHED_BIT) return reusableId;
                if (chunkById != currentChunk) {
                    if (loggable) {
                        LOGGER.log(
                                System.Logger.Level.TRACE, Logging.format(loggingContext.subject()
                                        , "Pushing reusableId: " + idSchema.idToString(reusableId)
                                )
                        );
                    }
                    idStack.push(reusableId);
                } else {
                    nextId = reusableId;
                }
                return reusableId;
            }
        }

        public PoolDataIterator<T> iterator() {
            return dataLength == 1 ?
                    new PoolDataIterator<>(firstChunk, idSchema) :
                    new PoolMultiDataIterator<>(firstChunk, idSchema);
        }

        public PoolDataIterator<T> noItemIterator() {
            return dataLength == 1 ?
                    new PoolDataNoItemIterator<>(firstChunk, idSchema) :
                    new PoolMultiDataNoItemIterator<>(firstChunk, idSchema);
        }

        public PoolDataIterator<T> iteratorWithState(boolean multiData) {
            return multiData ?
                    new PoolMultiDataIteratorWithState<>(firstChunk, idSchema) :
                    new PoolDataIteratorWithState<>(firstChunk, idSchema);
        }

        public PoolDataIterator<T> noItemIteratorWithState(boolean multiData) {
            return multiData ?
                    new PoolMultiDataNoItemIteratorWithState<>(firstChunk, idSchema) :
                    new PoolDataNoItemIteratorWithState<>(firstChunk, idSchema);
        }

        public T register(T entry, Object[] data) {
            return pool.getChunk(entry.getId()).set(entry, data);
        }

        public LinkedChunk<T> registerState(T entry) {
            int stateId = nextId();
            LinkedChunk<T> stateChunk = pool.getChunk(stateId);
            stateChunk.setState(stateId, entry);
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE)) {
                LOGGER.log(
                        System.Logger.Level.TRACE, Logging.format(loggingContext.subject()
                                , "Setting state to " + entry
                        )
                );
            }
            return stateChunk;
        }

        public void migrate(T entry, int newId, int[] indexMapping, int[] addedIndexMapping, Object addedComponent, Object[] addedComponents) {
            LinkedChunk<T> prevChunk = pool.getChunk(entry.getId());
            LinkedChunk<T> newChunk = pool.getChunk(newId);
            newChunk.copy(entry, prevChunk, newId, indexMapping);
            if (addedIndexMapping != null) {
                newChunk.add(newId, addedIndexMapping, addedComponent, addedComponents);
            }
        }

        public int currentChunkSize() {
            return currentChunk.size();
        }

        public int currentChunkLength() {
            return currentChunk.dataLength;
        }

        public int getDataLength() {
            return dataLength;
        }

        public ChunkedPool<T> getPool() {
            return pool;
        }

        public Object getOwner() {
            return owner;
        }

        public Object getSubject() {
            return subject;
        }

        @Override
        public void close() {
            idStack.close();
        }
    }

    // ROOT iterator

    public static class PoolIterator<T extends Item> implements Iterator<T> {
        protected int next;
        protected LinkedChunk<T> currentChunk;
        protected IdSchema idSchema;
        private int begin;

        public PoolIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            this.currentChunk = currentChunk;
            this.idSchema = idSchema;
            next = begin = currentChunk == null ? 0 : currentChunk.size() - 1;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean hasNext() {
            return next > -1
                    || (currentChunk != null && (currentChunk = currentChunk.next) != null && !currentChunk.isEmpty() && (next = begin = currentChunk.size() - 1) == begin);
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T next() {
            return (T) currentChunk.itemArray[next--];
        }
    }

    // SINGLE data iterator

    public static class PoolDataIterator<T extends Item> extends PoolIterator<T> {
        public PoolDataIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        public Object data(int i) {
            return currentChunk.dataArray[next];
        }

        public Object next(PoolIteratorNextWith1 nextWith1, int i1) {
            return null;
        }

        public Object next(PoolIteratorNextWith2 nextWith2, int i1, int i2) {
            return null;
        }

        public Object next(PoolIteratorNextWith3 nextWith3, int i1, int i2, int i3) {
            return null;
        }

        public Object next(PoolIteratorNextWith4 nextWith4, int i1, int i2, int i3, int i4) {
            return null;
        }

        public Object next(PoolIteratorNextWith5 nextWith5, int i1, int i2, int i3, int i4, int i5) {
            return null;
        }

        public Object next(PoolIteratorNextWith6 nextWith6, int i1, int i2, int i3, int i4, int i5, int i6) {
            return null;
        }
    }

    public static class PoolDataEmptyIterator<T extends Item> extends PoolDataIterator<T> {
        public PoolDataEmptyIterator() {
            super(null, null);
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            return null;
        }

        @Override
        public Object data(int i) {
            return null;
        }
    }

    public static class PoolDataIteratorWithState<T extends Item> extends PoolDataIterator<T> {
        public PoolDataIteratorWithState(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        public Object next(PoolIteratorNextWith1 nextWith1, int i1) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            var itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith1.fetchNext(itemChunk.dataArray, itemIdx, next());
        }

        @Override
        public Object data(int i) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            var itemIdx = idSchema.fetchObjectId(item.getId());
            return itemChunk.dataArray[itemIdx];
        }
    }

    public static final class PoolDataNoItemIterator<T extends Item> extends PoolDataIterator<T> {
        public PoolDataNoItemIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public T next() {
            next--;
            return null;
        }
    }

    public static final class PoolDataNoItemIteratorWithState<T extends Item> extends PoolDataIteratorWithState<T> {
        public PoolDataNoItemIteratorWithState(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public T next() {
            next--;
            return null;
        }
    }

    // MULTI data iterator

    public static class PoolMultiDataIterator<T extends Item> extends PoolDataIterator<T> {
        public PoolMultiDataIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public Object data(int i) {
            return currentChunk.multiDataArray[i][next];
        }
    }

    public static class PoolMultiDataIteratorWithState<T extends Item> extends PoolDataIteratorWithState<T> {
        public PoolMultiDataIteratorWithState(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public Object next(PoolIteratorNextWith1 nextWith1, int i1) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            int itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith1.fetchNext(itemChunk.multiDataArray, i1, itemIdx, next());
        }

        @Override
        public Object next(PoolIteratorNextWith2 nextWith2, int i1, int i2) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            int itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith2.fetchNext(itemChunk.multiDataArray, i1, i2, itemIdx, next());
        }

        @Override
        public Object next(PoolIteratorNextWith3 nextWith3, int i1, int i2, int i3) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            int itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith3.fetchNext(itemChunk.multiDataArray, i1, i2, i3, itemIdx, next());
        }

        @Override
        public Object next(PoolIteratorNextWith4 nextWith4, int i1, int i2, int i3, int i4) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            int itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith4.fetchNext(itemChunk.multiDataArray, i1, i2, i3, i4, itemIdx, next());
        }

        @Override
        public Object next(PoolIteratorNextWith5 nextWith5, int i1, int i2, int i3, int i4, int i5) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            int itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith5.fetchNext(itemChunk.multiDataArray, i1, i2, i3, i4, i5, itemIdx, next());
        }

        @Override
        public Object next(PoolIteratorNextWith6 nextWith6, int i1, int i2, int i3, int i4, int i5, int i6) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            int itemIdx = idSchema.fetchObjectId(item.getId());
            return nextWith6.fetchNext(itemChunk.multiDataArray, i1, i2, i3, i4, i5, i6, itemIdx, next());
        }
    }

    public static final class PoolMultiDataNoItemIterator<T extends Item> extends PoolMultiDataIterator<T> {
        public PoolMultiDataNoItemIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public T next() {
            next--;
            return null;
        }
    }

    public static final class PoolMultiDataNoItemIteratorWithState<T extends Item> extends PoolMultiDataIteratorWithState<T> {
        public PoolMultiDataNoItemIteratorWithState(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public T next() {
            next--;
            return null;
        }
    }

    public static final class LinkedChunk<T extends Item> {
        private static final System.Logger LOGGER = Logging.getLogger();
        private final IdSchema idSchema;
        private final Item[] itemArray;
        private final Object[] dataArray;
        private final Object[][] multiDataArray;
        private final LinkedChunk<T> previous;
        private final Tenant<T> tenant;
        private final int id;
        private final int dataLength;
        private int index = -1;
        private LinkedChunk<T> next;
        private int sizeOffset = 0;

        public LinkedChunk(int id, IdSchema idSchema, LinkedChunk<T> previous, int dataLength, Tenant<T> tenant, Logging.Context loggingContext) {
            this.idSchema = idSchema;
            this.dataLength = dataLength;
            itemArray = new Item[idSchema.chunkCapacity];
            dataArray = dataLength == 1 ? new Object[idSchema.chunkCapacity * dataLength] : null;
            multiDataArray = dataLength > 1 ? new Object[dataLength][idSchema.chunkCapacity * dataLength] : null;
            this.previous = previous;
            this.tenant = tenant;
            this.id = id;
            if (Logging.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, Logging.format(loggingContext.subject()
                                , "Creating " + this
                        )
                );
            }
        }

        public int incrementIndex() {
            return ++index;
        }

        public int remove(int id, boolean isState) {
            int removedIndex = idSchema.fetchObjectId(id);
            int lastIndex = --index + sizeOffset;
            if (lastIndex < 0 || lastIndex >= idSchema.chunkCapacity) {
                return IdSchema.DETACHED_BIT;
            }
            Item last = itemArray[lastIndex];
            Item removed = itemArray[removedIndex];
            if (last != null && last != removed) {
                if (!isState) {
                    last.setId(id);
                } else {
                    last.setStateId(id);
                }
                if (dataLength == 1) {
                    dataArray[removedIndex] = dataArray[lastIndex];
                }
                if (dataLength > 1) {
                    for (int i = 0; i < dataLength; i++) {
                        multiDataArray[i][removedIndex] = multiDataArray[i][lastIndex];
                    }
                }
                itemArray[removedIndex] = last;
                itemArray[lastIndex] = null;
            } else {
                itemArray[removedIndex] = null;
            }
            return idSchema.mergeId(id, lastIndex);
        }

        @SuppressWarnings("unchecked")
        public T get(int id) {
            return (T) itemArray[idSchema.fetchObjectId(id)];
        }

        @SuppressWarnings("unchecked")
        public T set(T value, Object[] data) {
            int idx = idSchema.fetchObjectId(value.getId());
            if (dataLength == 1) {
                dataArray[idx] = data[0];
            }
            if (dataLength > 1) {
                for (int i = 0; i < dataLength; i++) {
                    multiDataArray[i][idx] = data[i];
                }
            }
            value.setChunk(this);
            return (T) (itemArray[idx] = value);
        }

        public void setState(int stateId, T value) {
            int idx = idSchema.fetchObjectId(stateId);
            value.setStateId(stateId);
            value.setStateChunk(this);
            itemArray[idx] = value;
        }

        @SuppressWarnings("StatementWithEmptyBody")
        public void copy(T value, LinkedChunk<T> prevChunk, int newId, int[] indexMapping) {
            int prevIdx = idSchema.fetchObjectId(value.getId());
            int newIdx = idSchema.fetchObjectId(newId);
            if (indexMapping.length > 0) {
                if (dataLength == 1) { // copy to new dataArray
                    if (prevChunk.dataLength == 1) { // copy from prev.dataArray
                        dataArray[newIdx] = prevChunk.dataArray[prevIdx];
                    } else { // copy from prev.multiDataArray
                        int i = -1;
                        while (indexMapping[++i] != 0) ;
                        dataArray[newIdx] = prevChunk.multiDataArray[i][prevIdx];
                    }
                } else { // copy to new multiDataArray
                    if (prevChunk.dataLength == 1) { // copy from prev.dataArray
                        if (indexMapping[0] > -1) {
                            multiDataArray[indexMapping[0]][newIdx] = prevChunk.dataArray[prevIdx];
                        }
                    } else {  // copy from prev.multiDataArray
                        int i = -1;
                        while (indexMapping[++i] < 0) ;
                        i--;
                        while (++i < indexMapping.length) {
                            if (indexMapping[i] > -1)
                                multiDataArray[indexMapping[i]][newIdx] = prevChunk.multiDataArray[i][prevIdx];
                        }
                    }
                }
            }
            value.setId(newId);
            value.setChunk(this);
            itemArray[newIdx] = value;
        }

        public void add(int id, int[] addedIndexMapping, Object addedComponent, Object[] addedComponents) {
            int idx = idSchema.fetchObjectId(id);
            if (dataLength == 1) { // add to dataArray
                if (addedComponent != null) dataArray[idx] = addedComponent;
                else for (int i = 0; i < addedIndexMapping.length; i++) {
                    if (addedIndexMapping[i] == 0) {
                        dataArray[idx] = addedComponents[i];
                    }
                }
            } else if (dataLength > 1) { // add to multiDataArray
                if (addedComponent != null) multiDataArray[addedIndexMapping[0]][idx] = addedComponent;
                else for (int i = 0; i < addedIndexMapping.length; i++) {
                    if (addedIndexMapping[i] > -1) {
                        multiDataArray[addedIndexMapping[i]][idx] = addedComponents[i];
                    }
                }
            }
        }

        public Object[] shelve(T value) {
            int id = value.getId();
            Object[] data = getData(id);
            tenant.freeId(id);
            return data;
        }

        public void unshelve(T value, Object[] dataArray) {
            value.setId(tenant.nextId());
            tenant.register(value, dataArray);
        }

        public Object[] getData(int id) {
            int idx = idSchema.fetchObjectId(id);
            Object[] data = new Object[dataLength];
            if (dataLength == 1) {
                data[0] = dataArray[idx];
            }
            if (dataLength > 1) {
                for (int i = 0; i < dataLength; i++) {
                    data[i] = multiDataArray[i][idx];
                }
            }
            return data;
        }

        public Object getFromDataArray(int id) {
            return dataArray[idSchema.fetchObjectId(id)];
        }

        public Object getFromMultiDataArray(int id, int i) {
            return multiDataArray[i][idSchema.fetchObjectId(id)];
        }

        public Tenant<T> getTenant() {
            return tenant;
        }

        public int getDataLength() {
            return dataLength;
        }

        public boolean hasCapacity() {
            return index < idSchema.chunkCapacity - 1;
        }

        public LinkedChunk<T> getPrevious() {
            return previous;
        }

        private void setNext(LinkedChunk<T> next) {
            this.next = next;
            sizeOffset = 1;
        }

        public int size() {
            return index + sizeOffset;
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public String toString() {
            return "LinkedChunk={"
                    + "id=" + id
                    + ", dataLength=" + dataLength
                    + ", capacity=" + idSchema.chunkCapacity
                    + ", size=" + size()
                    + ", previous=" + (previous == null ? null : previous.id)
                    + ", next=" + (next == null ? null : next.id)
                    + ", of " + tenant
                    + '}';
        }
    }
}

/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.LoggingSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class ChunkedPool<T extends ChunkedPool.Item> implements AutoCloseable {
    public static final int ID_STACK_CAPACITY = 1 << 16;
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private final AtomicReferenceArray<LinkedChunk<T>> chunks;
    private final AtomicInteger chunkIndex = new AtomicInteger(-1);
    private final List<Tenant<T>> tenants = new ArrayList<>();
    private final IdSchema idSchema;
    private final LoggingSystem.Context loggingContext;

    public ChunkedPool(IdSchema idSchema, LoggingSystem.Context loggingContext) {
        this.idSchema = idSchema;
        this.loggingContext = loggingContext;
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + this
                    )
            );
        }
        chunks = new AtomicReferenceArray<>(idSchema.chunkCount);
    }

    @Override
    public String toString() {
        return "ChunkedPool={"
                + "chunkCount=" + idSchema.chunkCount
                + ", chunkCapacity=" + idSchema.chunkCapacity
                + '}';
    }

    private LinkedChunk<T> newChunk(Tenant<T> owner, LinkedChunk<T> previousChunk, int currentChunkIndex) {
        int id;
        if (!chunkIndex.compareAndSet(currentChunkIndex, id = currentChunkIndex + 1)) {
            return null;
        }
        if (id > idSchema.chunkCount - 1) {
            throw new OutOfMemoryError(ChunkedPool.class.getName() + ": cannot create a new memory chunk");
        }
        LinkedChunk<T> newChunk = new LinkedChunk<>(id, idSchema, previousChunk, owner.dataLength, owner, loggingContext);
        if (previousChunk != null) {
            previousChunk.setNext(newChunk);
        }
        chunks.set(id, newChunk);
        return newChunk;
    }

    private LinkedChunk<T> getChunk(int id) {
        return chunks.getPlain(idSchema.fetchChunkId(id));
    }

    public T getEntry(int id) {
        return getChunk(id).get(id);
    }

    public Tenant<T> newTenant() {
        return newTenant(0, null);
    }

    public Tenant<T> newTenant(int dataLength, Object owner) {
        Tenant<T> newTenant = new Tenant<>(this, idSchema, dataLength, owner, loggingContext);
        tenants.add(newTenant);
        return newTenant;
    }

    public int size() {
        int sum = 0;
        for (int i = 0; i <= chunkIndex.get(); i++) {
            var chunk = chunks.get(i);
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

        int setId(int id);

        int setStateId(int id);

        LinkedChunk<? extends Item> getChunk();

        void setChunk(LinkedChunk<? extends Item> chunk);

        void setStateChunk(LinkedChunk<? extends Item> chunk);
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
        private final LoggingSystem.Context loggingContext;
        private final int dataLength;
        private final Object owner;
        private volatile LinkedChunk<T> currentChunk;
        private int newId = Integer.MIN_VALUE;

        @SuppressWarnings("StatementWithEmptyBody")
        private Tenant(ChunkedPool<T> pool, IdSchema idSchema, int dataLength, Object owner, LoggingSystem.Context loggingContext) {
            this.pool = pool;
            this.idSchema = idSchema;
            this.dataLength = dataLength;
            this.owner = owner;
            this.loggingContext = loggingContext;
            idStack = new IntStack(ID_STACK_CAPACITY);
            while ((currentChunk = pool.newChunk(this, null, pool.chunkIndex.get())) == null) {
            }
            firstChunk = currentChunk;
            nextId();
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
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
                    ", newId=" + idSchema.idToString(newId) +
                    '}';
        }

        @SuppressWarnings("StatementWithEmptyBody")
        public int nextId() {
            boolean loggable = LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE);
            if (loggable) {
                LOGGER.log(
                        System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                , "Getting nextId from " + currentChunk
                                        + " having newId " + idSchema.idToString(newId)
                        )
                );
            }
            int returnValue = idStack.pop();
            if (loggable) {
                LOGGER.log(
                        System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                , "Popping nextId:" + idSchema.idToString(returnValue)
                        )
                );
            }
            if (returnValue != Integer.MIN_VALUE) {
                return returnValue;
            }
            for (; ; ) {
                returnValue = newId;
                int objectId;
                LinkedChunk<T> chunk = currentChunk;
                int currentChunkIndex = pool.chunkIndex.get();
                // try to get a newId from the current chunk
                if (chunk == null) {
                    continue;
                }
                while ((objectId = chunk.index.get()) < idSchema.chunkCapacity - 1) {
                    if (chunk.index.compareAndSet(objectId, objectId + 1)) {
                        newId = idSchema.createId(chunk.id, ++objectId);
                        return returnValue;
                    }
                }
                // current chunk is over
                currentChunk = null;
                while (currentChunk == null) {
                    // try to create a new one owned by this tenant
                    if ((chunk = pool.newChunk(this, chunk, currentChunkIndex)) != null) {
                        currentChunk = chunk;
                        objectId = chunk.incrementIndex();
                        newId = idSchema.createId(chunk.id, objectId);
                        return returnValue;
                    }
                    currentChunkIndex = pool.chunkIndex.get();
                    // try to get the chunk at the current index created by other threads
                    while ((chunk = pool.chunks.get(currentChunkIndex)) == null) {
                    }
                    // set it as the current chunk if it is already owned by this tenant
                    if (chunk.tenant == this) {
                        currentChunk = chunk;
                    }
                }
            }
        }

        public int freeId(int id) {
            return freeId(id, true, false);
        }

        public int freeStateId(int stateId) {
            return freeId(stateId, true, true);
        }

        public int freeId(int id, boolean check, boolean isState) {
            LinkedChunk<T> chunk = pool.getChunk(id);
            if (check && (chunk == null || chunk.tenant != this)) {
                throw new IllegalArgumentException("Invalid chunk [" + chunk + "] retrieved by [" + id + "]");
            }
            boolean loggable = LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE);
            if (chunk.isEmpty()) {
                idStack.push(id);
                if (loggable) {
                    LOGGER.log(
                            System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                    , "Pushing id: " + idSchema.idToString(id)
                            )
                    );
                }
                return id;
            }
            boolean notCurrentChunk = chunk != currentChunk;
            int reusableId = chunk.remove(id, notCurrentChunk, isState);
            if (loggable) {
                LOGGER.log(
                        System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                , "Freeing " + (isState ? "stateId" : "id") + "=" + idSchema.idToString(id)
                                        + " > reusableId=" + idSchema.idToString(reusableId)
                                        + " having current " + currentChunk

                        )
                );
            }
            if (notCurrentChunk) {
                if (loggable) {
                    LOGGER.log(
                            System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                    , "Pushing reusableId: " + idSchema.idToString(reusableId)
                            )
                    );
                }
                idStack.push(reusableId);
            } else {
                newId = reusableId;
            }
            return reusableId;
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
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE)) {
                LOGGER.log(
                        System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                , "Setting state to " + entry
                        )
                );
            }
            return stateChunk;
        }

        public T migrate(T entry, int newId, int[] indexMapping, int[] addedIndexMapping, Object[] addedComponents) {
            LinkedChunk<T> prevChunk = pool.getChunk(entry.getId());
            LinkedChunk<T> newChunk = pool.getChunk(newId);
            entry = newChunk.copy(entry, prevChunk, newId, indexMapping);
            if (addedIndexMapping != null) {
                newChunk.add(newId, addedIndexMapping, addedComponents);
            }
            return entry;
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

        public IntStack getIdStack() {
            return idStack;
        }

        public ChunkedPool<T> getPool() {
            return pool;
        }

        public Object getOwner() {
            return owner;
        }

        @Override
        public void close() {
            idStack.close();
        }
    }

    // ROOT iterator

    public static class PoolIterator<T extends Item> implements Iterator<T> {
        protected int next = 0;
        protected LinkedChunk<T> currentChunk;
        protected IdSchema idSchema;

        public PoolIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            this.currentChunk = currentChunk;
            this.idSchema = idSchema;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean hasNext() {
            return currentChunk.size() > next
                    || ((next = 0) == 0 && (currentChunk = currentChunk.next) != null);
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T next() {
            return (T) currentChunk.itemArray[next++];
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
        protected LinkedChunk<? extends Item> itemChunk;
        protected int itemIdx;
        private int begin;

        public PoolDataIteratorWithState(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
            next = begin = currentChunk.size() - 1;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public boolean hasNext() {
            return next > -1
                    || ((currentChunk = currentChunk.next) != null && (next = begin = currentChunk.size() - 1) == begin);
        }

        @Override
        public Object data(int i) {
            var item = currentChunk.itemArray[next];
            var itemChunk = item.getChunk();
            var itemIdx = idSchema.fetchObjectId(item.getId());
            return itemChunk.dataArray != null ? itemChunk.dataArray[itemIdx] : itemChunk.multiDataArray[i][itemIdx];
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T next() {
            return (T) currentChunk.itemArray[next--];
        }
    }

    public static final class PoolDataNoItemIterator<T extends Item> extends PoolDataIterator<T> {
        public PoolDataNoItemIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public T next() {
            next++;
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
        public Object data(int i) {
            if (itemChunk == null) {
                var item = currentChunk.itemArray[next];
                itemChunk = item.getChunk();
                itemIdx = idSchema.fetchObjectId(item.getId());
            }
            return itemChunk.multiDataArray[i][itemIdx];
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public T next() {
            itemChunk = null;
            return (T) currentChunk.itemArray[next--];
        }
    }

    public static final class PoolMultiDataNoItemIterator<T extends Item> extends PoolMultiDataIterator<T> {
        public PoolMultiDataNoItemIterator(LinkedChunk<T> currentChunk, IdSchema idSchema) {
            super(currentChunk, idSchema);
        }

        @Override
        public T next() {
            next++;
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
            itemChunk = null;
            return null;
        }
    }

    public static final class LinkedChunk<T extends Item> {
        private static final System.Logger LOGGER = LoggingSystem.getLogger();
        private final IdSchema idSchema;
        private final Item[] itemArray;
        private final Object[] dataArray;
        private final Object[][] multiDataArray;
        private final LinkedChunk<T> previous;
        private final Tenant<T> tenant;
        private final int id;
        private final AtomicInteger index = new AtomicInteger(-1);
        private final int dataLength;
        private LinkedChunk<T> next;
        private int sizeOffset = 0;

        public LinkedChunk(int id, IdSchema idSchema, LinkedChunk<T> previous, int dataLength, Tenant<T> tenant, LoggingSystem.Context loggingContext) {
            this.idSchema = idSchema;
            this.dataLength = dataLength;
            itemArray = new Item[idSchema.chunkCapacity];
            dataArray = dataLength == 1 ? new Object[idSchema.chunkCapacity * dataLength] : null;
            multiDataArray = dataLength > 1 ? new Object[dataLength][idSchema.chunkCapacity * dataLength] : null;
            this.previous = previous;
            this.tenant = tenant;
            this.id = id;
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                                , "Creating " + this
                        )
                );
            }
        }

        public int incrementIndex() {
            return index.incrementAndGet();
        }

        public int remove(int id, boolean doNotUpdateIndex, boolean isState) {
            int capacity = idSchema.chunkCapacity;
            int removedIndex = idSchema.fetchObjectId(id);
            boolean recheck = false;
            for (; ; ) {
                int lastIndex = doNotUpdateIndex || recheck ? index.get() : index.decrementAndGet();
                recheck = false;
                if (lastIndex >= capacity) {
                    index.compareAndSet(capacity, capacity - 1);
                    continue;
                }
                if (lastIndex < 0) {
                    return 0;
                }
                Item last = itemArray[lastIndex];
                Item removed = itemArray[removedIndex];
                if (last != null && last != removed) {
                    synchronized (itemArray[lastIndex]) {
                        if (!isState && last.setId(id) != id) {
                            recheck = true;
                            continue;
                        }
                        if (isState && last.setStateId(id) != id) {
                            recheck = true;
                            continue;
                        }
                        if (dataLength == 1) {
                            dataArray[removedIndex] = dataArray[lastIndex];
                            dataArray[lastIndex] = null;
                        }
                        if (dataLength > 1) {
                            for (int i = 0; i < dataLength; i++) {
                                multiDataArray[i][removedIndex] = multiDataArray[i][lastIndex];
                                multiDataArray[i][lastIndex] = null;
                            }
                        }
                        itemArray[removedIndex] = last;
                        itemArray[lastIndex] = null;
                    }
                } else {
                    itemArray[removedIndex] = null;
                    if (dataLength == 1) {
                        dataArray[removedIndex] = null;
                    }
                    if (dataLength > 1) {
                        for (int i = 0; i < dataLength; i++) {
                            multiDataArray[i][removedIndex] = null;
                        }
                    }
                }
                return idSchema.mergeId(id, lastIndex);
            }
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

        @SuppressWarnings("unchecked")
        public T copy(T value, LinkedChunk<T> prevChunk, int newId, int[] indexMapping) {
            int prevIdx = idSchema.fetchObjectId(value.getId());
            int newIdx = idSchema.fetchObjectId(newId);
            if (indexMapping.length > 0) {
                if (dataLength == 1) { // copy to new dataArray
                    if (prevChunk.dataLength == 1) { // copy from prev.dataArray
                        dataArray[newIdx] = prevChunk.dataArray[prevIdx];
                    } else if (prevChunk.dataLength > 1) { // copy from prev.multiDataArray
                        for (int i = 0; i < indexMapping.length; i++) {
                            if (indexMapping[i] == 0) {
                                dataArray[newIdx] = prevChunk.multiDataArray[i][prevIdx];
                            }
                        }
                    }
                } else if (dataLength > 1) { // copy to new multiDataArray
                    if (prevChunk.dataLength == 1) { // copy from prev.dataArray
                        if (indexMapping[0] > -1) {
                            multiDataArray[indexMapping[0]][newIdx] = prevChunk.dataArray[prevIdx];
                        }
                    } else if (prevChunk.dataLength > 1) {  // copy from prev.multiDataArray
                        for (int i = 0; i < indexMapping.length; i++) {
                            if (indexMapping[i] > -1) {
                                multiDataArray[indexMapping[i]][newIdx] = prevChunk.multiDataArray[i][prevIdx];
                            }
                        }
                    }
                }
            }
            value.setId(newId);
            value.setChunk(this);
            return (T) (itemArray[newIdx] = value);
        }

        public void add(int id, int[] addedIndexMapping, Object[] addedComponents) {
            int idx = idSchema.fetchObjectId(id);
            if (dataLength == 1) { // add to dataArray
                for (int i = 0; i < addedIndexMapping.length; i++) {
                    if (addedIndexMapping[i] == 0) {
                        dataArray[idx] = addedComponents[i];
                    }
                }
            } else if (dataLength > 1) { // add to multiDataArray
                for (int i = 0; i < addedIndexMapping.length; i++) {
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

        public Tenant<T> getTenant() {
            return tenant;
        }

        public int getDataLength() {
            return dataLength;
        }

        public boolean hasCapacity() {
            return index.get() < idSchema.chunkCapacity - 1;
        }

        public LinkedChunk<T> getPrevious() {
            return previous;
        }

        private void setNext(LinkedChunk<T> next) {
            this.next = next;
            sizeOffset = 1;
        }

        public int size() {
            return index.get() + sizeOffset;
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
                    + ", previous=" + (previous == null ? null : previous.id)
                    + ", next=" + (next == null ? null : next.id)
                    + ", of " + tenant
                    + '}';
        }
    }
}

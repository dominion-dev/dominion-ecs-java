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
        return newTenant(0);
    }

    public Tenant<T> newTenant(int dataLength) {
        Tenant<T> newTenant = new Tenant<>(this, idSchema, dataLength, loggingContext);
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

        Item getPrev();

        void setPrev(Item prev);

        Item getNext();

        void setNext(Item next);

        void setArray(Object[] array, int offset);

        int getOffset();

        boolean isEnabled();
    }

    // |--FLAGS--|--CHUNK_ID--|--OBJECT_ID--|
    public record IdSchema(int chunkBit, int chunkCountBit
            , int chunkCount, int chunkIdBitMask, int chunkIdBitMaskShifted
            , int chunkCapacity, int objectIdBitMask
    ) {
        public static final int BIT_LENGTH = 30;
        public static final int MIN_CHUNK_BIT = 10;
        public static final int MIN_CHUNK_COUNT_BIT = 6;
        public static final int MAX_CHUNK_BIT = BIT_LENGTH - MIN_CHUNK_COUNT_BIT;
        public static final int MAX_CHUNK_COUNT_BIT = BIT_LENGTH - MIN_CHUNK_BIT;
        public static final int DETACHED_BIT_IDX = 31;
        public static final int DETACHED_BIT = 1 << DETACHED_BIT_IDX;
        public static final int LOCK_BIT_IDX = 30;
        public static final int LOCK_BIT = 1 << LOCK_BIT_IDX;

        public IdSchema(int chunkBit, int chunkCountBit) {
            this(chunkBit
                    , chunkCountBit
                    , 1 << chunkCountBit
                    , (1 << (BIT_LENGTH - chunkBit)) - 1
                    , ((1 << (BIT_LENGTH - chunkBit)) - 1) << chunkBit
                    , 1 << Math.min(chunkBit, MAX_CHUNK_BIT)
                    , (1 << chunkBit) - 1
            );
        }

        public String idToString(int id) {
            return "|" + ((id & DETACHED_BIT) >>> DETACHED_BIT_IDX)
                    + ":" + ((id & LOCK_BIT) >>> LOCK_BIT_IDX)
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
        private final IdStack stack;
        private final LinkedChunk<T> firstChunk;
        private final LoggingSystem.Context loggingContext;
        private final int dataLength;
        private volatile LinkedChunk<T> currentChunk;
        private int newId = Integer.MIN_VALUE;

        @SuppressWarnings("StatementWithEmptyBody")
        private Tenant(ChunkedPool<T> pool, IdSchema idSchema, int dataLength, LoggingSystem.Context loggingContext) {
            this.pool = pool;
            this.idSchema = idSchema;
            this.dataLength = dataLength;
            this.loggingContext = loggingContext;
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                                , "Creating " + getClass().getSimpleName()
                        )
                );
            }
            stack = new IdStack(ID_STACK_CAPACITY, idSchema, loggingContext);
            while ((currentChunk = pool.newChunk(this, null, pool.chunkIndex.get())) == null) {
            }
            firstChunk = currentChunk;
            nextId();
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
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE)) {
                LOGGER.log(
                        System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                , "Getting nextId from " + currentChunk
                                        + " having newId " + idSchema.idToString(newId)
                        )
                );
            }
            int returnValue = stack.pop();
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
            LinkedChunk<T> chunk = pool.getChunk(id);
            if (chunk == null) {
                return -1;
            }
            if (chunk.isEmpty()) {
                stack.push(id);
                return id;
            }
            boolean notCurrentChunk = chunk != currentChunk;
            int reusableId = chunk.remove(id, notCurrentChunk);
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE)) {
                LOGGER.log(
                        System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                                , "Freeing id=" + idSchema.idToString(id)
                                        + " > reusableId=" + idSchema.idToString(reusableId)
                                        + " having current " + currentChunk

                        )
                );
            }
            if (notCurrentChunk) {
                stack.push(reusableId);
            } else {
                newId = reusableId;
            }
            return reusableId;
        }

        public Iterator<T> iterator() {
            return new PoolIterator<>(firstChunk);
        }

        public T register(int id, T entry, Object[] data) {
            return pool.getChunk(id).set(id, entry, data);
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

        public IdStack getStack() {
            return stack;
        }

        public ChunkedPool<T> getPool() {
            return pool;
        }

        @Override
        public void close() {
            stack.close();
        }
    }

    public static class PoolIterator<T extends Item> implements Iterator<T> {
        int next = 0;
        private LinkedChunk<T> currentChunk;

        public PoolIterator(LinkedChunk<T> currentChunk) {
            this.currentChunk = currentChunk;
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

    public static final class LinkedChunk<T extends Item> {
        private static final System.Logger LOGGER = LoggingSystem.getLogger();
        private final IdSchema idSchema;
        private final Item[] itemArray;
        private final Object[] dataArray;
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
            dataArray = dataLength > 0 ? new Object[idSchema.chunkCapacity * dataLength] : null;
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

        public int remove(int id, boolean doNotUpdateIndex) {
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
                        if (last.setId(id) != id) {
                            recheck = true;
                            continue;
                        }
                        int removedOffset = removed.getOffset();
                        if (dataLength > 0) {
                            System.arraycopy(dataArray, last.getOffset(), dataArray, removedOffset, dataLength);
                        }
                        last.setArray(dataArray, removedOffset);
                        itemArray[removedIndex] = last;
                        removed.setArray(null, -1);
                        itemArray[lastIndex] = null;
                    }
                } else {
                    itemArray[removedIndex] = null;
                    if (removed != null) {
                        int offset = removed.getOffset();
                        for (int i = offset; i < offset + dataLength; i++) {
                            dataArray[i] = null;
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
        public T set(int id, T value, Object[] data) {
            int idx = idSchema.fetchObjectId(id);
            if (dataLength > 0) {
                int offset = idx * dataLength;
                if (data != null) {
                    if (data.length == dataLength) {
                        System.arraycopy(data, 0, dataArray, offset, dataLength);
                    }
                }
                value.setArray(dataArray, offset);
            }
            return (T) (itemArray[idx] = value);
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

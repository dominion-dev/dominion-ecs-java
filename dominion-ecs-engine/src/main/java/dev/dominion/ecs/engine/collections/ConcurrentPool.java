/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class ConcurrentPool<T extends ConcurrentPool.Identifiable> implements AutoCloseable {
    private final LinkedChunk<T>[] chunks;
    private final AtomicInteger chunkIndex = new AtomicInteger(-1);
    private final List<Tenant<T>> tenants = new ArrayList<>();
    private final ChunkSchema chunkSchema;

    @SuppressWarnings("unchecked")
    public ConcurrentPool(ChunkSchema chunkSchema) {
        this.chunkSchema = chunkSchema;
        chunks = new LinkedChunk[chunkSchema.maxNumOfChunks];
    }

    private LinkedChunk<T> newChunk(Tenant<T> owner) {
        int id = chunkIndex.incrementAndGet();
        if (id > chunkSchema.maxNumOfChunks - 1) {
            throw new OutOfMemoryError(ConcurrentPool.class.getName() + ": cannot create a new memory chunk");
        }
        LinkedChunk<T> currentChunk = owner.currentChunk;
        LinkedChunk<T> newChunk =
                new LinkedChunk<>(id, chunkSchema.chunkCapacity, chunkSchema.identifiableIndexBitMask, currentChunk);
        if (currentChunk != null) {
            currentChunk.setNext(newChunk);
        }
        return chunks[id] = newChunk;
    }

    private LinkedChunk<T> getChunk(int id) {
        int chunkId = (id >> chunkSchema.chunkBit) & chunkSchema.chunkIndexBitMask;
        return chunks[chunkId];
    }

    public T getEntry(int id) {
        return getChunk(id).get(id);
    }

    public Tenant<T> newTenant() {
        Tenant<T> newTenant = new Tenant<>(this);
        tenants.add(newTenant);
        return newTenant;
    }

    public int size() {
        return Arrays.stream(chunks)
                .filter(Objects::nonNull)
                .mapToInt(LinkedChunk::size)
//                .peek(System.out::println)
                .sum();
    }

    @Override
    public void close() {
        tenants.forEach(Tenant::close);
    }

    public interface Identifiable {

        int getId();

        int setId(int id);

        int getPrevId();

        int setPrevId(int prevId);

        int getNextId();

        int setNextId(int nextId);
    }

    public record ChunkSchema(int chunkBit
            , int maxNumOfChunks, int chunkIndexBitMask, int chunkIndexBitMaskShifted
            , int chunkCapacity, int identifiableIndexBitMask
    ) {
        private static final int BIT_LENGTH = 30;

        public ChunkSchema(int chunkBit) {
            this(chunkBit
                    , 1 << (BIT_LENGTH - chunkBit)
                    , (1 << (BIT_LENGTH - chunkBit)) - 1
                    , ((1 << (BIT_LENGTH - chunkBit)) - 1) << chunkBit
                    , 1 << chunkBit
                    , (1 << chunkBit) - 1
            );
        }
    }

    public static final class Tenant<T extends Identifiable> implements AutoCloseable {
        private final ConcurrentPool<T> pool;
        private final StampedLock lock = new StampedLock();
        private final ConcurrentIntStack stack;
        private final LinkedChunk<T> firstChunk;
        private LinkedChunk<T> currentChunk;
        private int newId;

        private Tenant(ConcurrentPool<T> pool) {
            this.pool = pool;
            firstChunk = currentChunk = pool.newChunk(this);
            stack = new ConcurrentIntStack(1 << 16);
            nextId();
        }

        public int nextId() {
            int returnValue = stack.pop();
            if (returnValue != Integer.MIN_VALUE) {
                return returnValue;
            }
            long stamp = lock.tryOptimisticRead();
            ChunkSchema chunkSchema = pool.chunkSchema;
            try {
                for (; ; ) {
                    if (stamp == 0L) {
                        stamp = lock.writeLock();
                        continue;
                    }
                    // possibly racy reads
                    returnValue = newId;
                    int chunkIndex;
                    if ((chunkIndex = currentChunk.index.get()) < chunkSchema.chunkCapacity - 1) {
                        boolean incremented = false;
                        while (!incremented && (chunkIndex = currentChunk.index.get()) < chunkSchema.chunkCapacity - 1) {
                            if (currentChunk.index.compareAndSet(chunkIndex, chunkIndex + 1)) {
                                incremented = true;
                                chunkIndex++;
                            }
                        }
                        if (!incremented) {
                            stamp = lock.tryOptimisticRead();
                            continue;
                        }
                    } else {
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if (stamp == 0L) {
                            stamp = lock.writeLock();
                            continue;
                        }
                        // exclusive access
                        chunkIndex = (currentChunk = pool.newChunk(this)).incrementIndex();
                    }
                    newId = (chunkIndex & chunkSchema.identifiableIndexBitMask) |
                            (currentChunk.id & chunkSchema.chunkIndexBitMask) << chunkSchema.chunkBit;
                    return returnValue;
                }
            } finally {
                if (StampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
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
            if (notCurrentChunk) {
                stack.push((id & pool.chunkSchema.chunkIndexBitMaskShifted) | reusableId);
            } else {
                newId = reusableId;
            }
            return reusableId;
        }

        public Iterator<T> iterator() {
            return new PoolIterator<>(firstChunk);
        }

        public T register(int id, T entry) {
            return pool.getChunk(id).set(id, entry);
        }

        public int currentChunkSize() {
            return currentChunk.size();
        }

        public ConcurrentPool<T> getPool() {
            return pool;
        }

        @Override
        public void close() {
            stack.close();
        }
    }

    public static class PoolIterator<T extends Identifiable> implements Iterator<T> {
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

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            return (T) currentChunk.data[next++];
        }
    }

    public static final class LinkedChunk<T extends Identifiable> {
        private final int capacity;
        private final int indexBitMask;
        private final Identifiable[] data;
        private final LinkedChunk<T> previous;
        private final int id;
        private final AtomicInteger index = new AtomicInteger(-1);
        private LinkedChunk<T> next;
        private int sizeOffset;

        public LinkedChunk(int id, int capacity, int indexBitMask, LinkedChunk<T> previous) {
            this.capacity = capacity;
            this.indexBitMask = indexBitMask;
            data = new Identifiable[capacity];
            this.previous = previous;
            this.id = id;
        }

        public int incrementIndex() {
            return index.incrementAndGet();
        }

        public int remove(int id, boolean doNotUpdateIndex) {
            int indexToBeReused = id & indexBitMask;
            for (; ; ) {
                int lastIndex = doNotUpdateIndex ? index.get() : index.decrementAndGet();
                if (lastIndex >= capacity) {
                    index.compareAndSet(capacity, capacity - 1);
                    continue;
                }
                if (lastIndex < 0) {
                    return 0;
                }
                data[indexToBeReused] = data[lastIndex];
                data[lastIndex] = null;
                if (data[indexToBeReused] != null) {
                    data[indexToBeReused].setId(indexToBeReused);
                }
                return lastIndex;
            }
        }

        @SuppressWarnings("unchecked")
        public T get(int id) {
            return (T) data[id & indexBitMask];
        }

        @SuppressWarnings("unchecked")
        public T set(int id, T value) {
            return (T) (data[id & indexBitMask] = value);
        }

        public boolean hasCapacity() {
            return index.get() < capacity - 1;
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
    }
}

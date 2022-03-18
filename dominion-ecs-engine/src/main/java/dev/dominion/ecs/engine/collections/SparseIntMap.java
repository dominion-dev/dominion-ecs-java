/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.HashCode;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class SparseIntMap<V> {
    private final int[] dense;
    private final int[] sparse;
    private final Object[] values;
    private final StampedLock lock = new StampedLock();
    private final int capacity;
    private final AtomicInteger size = new AtomicInteger(0);
    private final AtomicBoolean isKeysHashCodeValid = new AtomicBoolean(false);
    private long keysHashCode;

    private SparseIntMap(int[] dense, int[] sparse, Object[] values) {
        this.dense = dense;
        this.sparse = sparse;
        this.values = values;
        capacity = values.length;
    }

    public SparseIntMap() {
        this(1 << 10);
    }

    public SparseIntMap(int capacity) {
        this(
                new int[capacity],
                new int[capacity],
                new Object[capacity]
        );
    }

    public V put(int key, V value) {
        V current = get(key);
        int i = current == null ? size.getAndIncrement() : sparse[key];
        dense[i] = key;
        sparse[key] = i;
        values[i] = value;
        isKeysHashCodeValid.set(false);
        return current;
    }

    public V get(int key) {
        int i = sparse[key];
        if (i > size.get() || dense[i] != key) return null;
        return valueAt(i);
    }

    public Boolean contains(int key) {
        int i = sparse[key];
        return i <= size.get() && dense[i] == key;
    }

    public V computeIfAbsent(int key, Function<Integer, ? extends V> mappingFunction) {
        V value;
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L)
                    continue;
                // possibly racy reads
                value = get(key);
                if (!lock.validate(stamp))
                    continue;
                if (value != null)
                    break;
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L)
                    continue;
                // exclusive access
                put(key, value = mappingFunction.apply(key));
                break;
            }
            return value;
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    public int size() {
        return size.get();
    }

    public boolean isEmpty() {
        return size.get() == 0;
    }

    @SuppressWarnings("unchecked")
    private V valueAt(int index) {
        return (V) values[index];
    }

    @SuppressWarnings("unchecked")
    public Iterator<V> iterator() {
        return new ObjectIterator<>((V[]) values, size.get());
    }

    public Stream<V> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    public Stream<Integer> keysStream() {
        return IntStream.of(dense)
                .limit(size.get())
                .boxed();
    }

    public void invalidateKeysHashCode() {
        isKeysHashCodeValid.set(false);
    }

    public long sortedKeysHashCode() {
        if (isKeysHashCodeValid.get()) {
            return keysHashCode;
        }
        int length = size();
        int[] keys = new int[length];
        System.arraycopy(dense, 0, keys, 0, length);
        Arrays.sort(keys);
        keysHashCode = HashCode.longHashCode(keys);
        isKeysHashCodeValid.set(true);
        return keysHashCode;
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public V[] values() {
        if (isEmpty()) {
            return null;
        }
        int length = size.get();
        V[] target = (V[]) Array.newInstance(values[0].getClass(), length);
        System.arraycopy(values, 0, target, 0, length);
        return target;
    }

    public int getCapacity() {
        return capacity;
    }

    public static final class ObjectIterator<V> implements Iterator<V> {

        private final V[] data;
        private final int limit;
        int next = 0;

        ObjectIterator(V[] data, int limit) {
            this.data = data;
            this.limit = limit;
        }

        @Override
        public boolean hasNext() {
            return next < limit;
        }

        @Override
        public V next() {
            return data[next++];
        }
    }
}

package dev.dominion.ecs.engine.collections;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ConcurrentIntMap<V> implements SparseIntMap<V> {

    private final int[] dense;
    private final int[] sparse;
    private final Object[] values;
    private final StampedLock lock = new StampedLock();
    private final int capacity;
    private AtomicInteger size = new AtomicInteger(0);

    private ConcurrentIntMap(int[] dense, int[] sparse, Object[] values) {
        this.dense = dense;
        this.sparse = sparse;
        this.values = values;
        capacity = values.length;
    }

    public ConcurrentIntMap() {
        this(1 << 10);
    }

    public ConcurrentIntMap(int capacity) {
        this(
                new int[capacity],
                new int[capacity],
                new Object[capacity]
        );
    }

    @Override
    public V put(int key, V value) {
        V current = get(key);
        int i = current == null ? size.getAndIncrement() : sparse[key];
        dense[i] = key;
        sparse[key] = i;
        values[i] = value;
        return current;
    }

    @Override
    public V get(int key) {
        int i = sparse[key];
        if (i > size.get() || dense[i] != key) return null;
        return valueAt(i);
    }

    @Override
    public Boolean contains(int key) {
        int i = sparse[key];
        return i <= size.get() && dense[i] == key;
    }

    @Override
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
            if (StampedLock.isWriteLockStamp(stamp))
                lock.unlockWrite(stamp);
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @SuppressWarnings("unchecked")
    private V valueAt(int index) {
        return (V) values[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<V> iterator() {
        return new ObjectIterator<>((V[]) values, size.get());
    }

    @Override
    public Stream<V> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @Override
    public Stream<Integer> keysStream() {
        return IntStream.of(dense)
                .limit(size.get())
                .boxed();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public SparseIntMap<V> clone() {
        int[] newDense = new int[capacity];
        int[] newSparse = new int[capacity];
        Object[] newValues = new Object[capacity];
        System.arraycopy(dense, 0, newDense, 0, capacity);
        System.arraycopy(sparse, 0, newSparse, 0, capacity);
        System.arraycopy(values, 0, newValues, 0, capacity);
        ConcurrentIntMap<V> cloned = new ConcurrentIntMap<>(newDense, newSparse, newValues);
        cloned.size = size;
        return cloned;
    }
}

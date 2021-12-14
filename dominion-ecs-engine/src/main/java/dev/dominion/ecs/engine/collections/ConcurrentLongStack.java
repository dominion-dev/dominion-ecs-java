package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;

public final class ConcurrentLongStack implements AutoCloseable {
    private static final int LONG_BYTES = 8;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    public final AtomicInteger index = new AtomicInteger(-LONG_BYTES);
    private final long address;
    private final int capacity;

    public ConcurrentLongStack(int capacity) {
        this.capacity = capacity;
        address = unsafe.allocateMemory(capacity);
    }

    public long pop() {
        int i = index.get();
        if (i < 0) {
            return Long.MIN_VALUE;
        }
        long returnValue = unsafe.getLong(address + i);
        return index.compareAndSet(i, i - LONG_BYTES) ? returnValue : Long.MIN_VALUE;
    }

    public boolean push(long value) {
        long offset = index.addAndGet(LONG_BYTES);
        if (offset < capacity) {
            unsafe.putLong(address + offset, value);
            return true;
        }
        index.addAndGet(-LONG_BYTES);
        return false;
    }

    @Override
    public void close() {
        unsafe.freeMemory(address);
    }
}

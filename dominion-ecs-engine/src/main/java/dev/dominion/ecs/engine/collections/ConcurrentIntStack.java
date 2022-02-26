/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;

public final class ConcurrentIntStack implements AutoCloseable {
    private static final int INT_BYTES = 4;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final AtomicInteger index = new AtomicInteger(-INT_BYTES);
    private final long address;
    private final int capacity;

    public ConcurrentIntStack(int capacity) {
        this.capacity = capacity;
        address = unsafe.allocateMemory(capacity);
    }

    public int pop() {
        int i = index.get();
        if (i < 0) {
            return Integer.MIN_VALUE;
        }
        int returnValue = unsafe.getInt(address + i);
        return index.compareAndSet(i, i - INT_BYTES) ? returnValue : Integer.MIN_VALUE;
    }

    public boolean push(int value) {
        long offset = index.addAndGet(INT_BYTES);
        if (offset < capacity) {
            unsafe.putInt(address + offset, value);
            return true;
        }
        index.addAndGet(-INT_BYTES);
        return false;
    }

    public int size() {
        return (index.get() >> 2) + 1;
    }

    @Override
    public void close() {
        unsafe.freeMemory(address);
    }
}

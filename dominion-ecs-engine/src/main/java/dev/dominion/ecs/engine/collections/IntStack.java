/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class IntStack implements AutoCloseable {
    private static final int INT_BYTES = 4;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final AtomicInteger index = new AtomicInteger(-INT_BYTES);
    private final StampedLock lock = new StampedLock();
    private final int nullInt;
    private long address;
    private int capacity;

    public IntStack(int nullInt, int initialCapacity) {
        this.nullInt = nullInt;
        this.capacity = initialCapacity;
        address = unsafe.allocateMemory(initialCapacity);
    }

    public int pop() {
        int i = index.get();
        if (i < 0) {
            return nullInt;
        }
        int returnValue = unsafe.getInt(address + i);
        returnValue = index.compareAndSet(i, i - INT_BYTES) ? returnValue : Integer.MIN_VALUE;
        return returnValue;
    }

    public boolean push(int id) {
        long offset = index.addAndGet(INT_BYTES);
        if (offset >= capacity) {
            long l = lock.writeLock();
            try {
                int currentCapacity;
                if (offset >= (currentCapacity = capacity)) {
                    int newCapacity = currentCapacity + (currentCapacity >>> 1);
                    long newAddress = unsafe.allocateMemory(newCapacity);
                    unsafe.copyMemory(address, newAddress, currentCapacity);
                    unsafe.freeMemory(address);
                    capacity = newCapacity;
                    address = newAddress;
                }
            } finally {
                lock.unlock(l);
            }
        }
        unsafe.putInt(address + offset, id);
        return true;
    }

    public int size() {
        return (index.get() >> 2) + 1;
    }

    @Override
    public void close() {
        unsafe.freeMemory(address);
    }

    @Override
    public String toString() {
        return "IntStack={"
                + "capacity=" + capacity + "|off-heap"
                + '}';
    }
}

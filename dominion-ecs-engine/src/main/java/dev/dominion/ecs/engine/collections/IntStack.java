/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.StampedLock;

public final class IntStack implements AutoCloseable {
    public static final int NULL_VALUE = 0b10000000100000001000000010000000;
    private static final int INT_BYTES = 4;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private static final AtomicIntegerFieldUpdater<IntStack> INDEX_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(IntStack.class, "index");
    private volatile int index = -INT_BYTES;
    private static final AtomicIntegerFieldUpdater<IntStack> CTRL =
            AtomicIntegerFieldUpdater.newUpdater(IntStack.class, "ctrl");
    private final StampedLock lock = new StampedLock();
    private volatile int ctrl;
    private volatile long address;
    private volatile int capacity;

    // todo: remove nullInt
    public IntStack(int nullInt, int initialCapacity) {
        this.capacity = initialCapacity;
        address = unsafe.allocateMemory(initialCapacity);
        unsafe.setMemory(address, initialCapacity, (byte) (1 << 7));
    }

    public int pop() {
        final var offset = index;
        if (offset < 0) {
            return NULL_VALUE;
        }
        // try to read value, if value equals NULL_VALUE, it means that push is calling, offset has added but value added not yet
        int returnValue = unsafe.getInt(address + offset);
        if (returnValue != NULL_VALUE && unsafe.compareAndSwapInt(null, address + offset, returnValue, NULL_VALUE)) {
            INDEX_UPDATER.addAndGet(this, -INT_BYTES);
            return returnValue;
        }
        return NULL_VALUE;
    }

    public boolean push(int id) {
        final var offset = INDEX_UPDATER.addAndGet(this, INT_BYTES);
        if (offset >= capacity) {
            long l = lock.writeLock();
            try {
                int currentCapacity;
                if (offset >= (currentCapacity = capacity)) {
                    for (; ; ) {
                        if (CTRL.compareAndSet(this, 0, -1)) {
                            int newCapacity = currentCapacity + (currentCapacity >>> 1);
                            long newAddress = unsafe.allocateMemory(newCapacity);
                            long oldAddress = address;
                            unsafe.setMemory(newAddress, newCapacity, (byte) (1 << 7));
                            unsafe.copyMemory(oldAddress, newAddress, currentCapacity);
                            capacity = newCapacity;
                            address = newAddress;
                            unsafe.freeMemory(oldAddress);
                            ctrl = 0;
                            break;
                        } else {
                            Thread.yield();
                        }
                    }
                }
            } finally {
                lock.unlock(l);
            }
        }
        for (; ; ) {
            final var c = ctrl;
            if (c == -1) {
                Thread.yield();
            } else if (CTRL.compareAndSet(this, c, c + 1)) {
                unsafe.putInt(address + offset, id);
                CTRL.decrementAndGet(this);
                return true;
            }
        }
    }

    public int size() {
        return (index >> 2) + 1;
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

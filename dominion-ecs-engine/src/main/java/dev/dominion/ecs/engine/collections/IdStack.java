/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.LoggingSystem;
import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public final class IdStack implements AutoCloseable {
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private static final int INT_BYTES = 4;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final ChunkedPool.IdSchema idSchema;
    private final AtomicInteger index = new AtomicInteger(-INT_BYTES);
    private final LoggingSystem.Context loggingContext;
    private final StampedLock lock = new StampedLock();
    private long address;
    private int capacity;

    public IdStack(int initialCapacity, ChunkedPool.IdSchema idSchema, LoggingSystem.Context loggingContext) {
        this.capacity = initialCapacity;
        this.idSchema = idSchema;
        this.loggingContext = loggingContext;
        address = unsafe.allocateMemory(initialCapacity);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
            LOGGER.log(
                    System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                            , "Creating " + this
                    )
            );
        }
    }

    public int pop() {
        int i = index.get();
        if (i < 0) {
            return Integer.MIN_VALUE;
        }
        int returnValue = unsafe.getInt(address + i);
        returnValue = index.compareAndSet(i, i - INT_BYTES) ? returnValue : Integer.MIN_VALUE;
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE)) {
            LOGGER.log(
                    System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                            , "Popping id=" + idSchema.idToString(returnValue)
                    )
            );
        }
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
                    capacity = newCapacity;
                    address = newAddress;
                }
            } finally {
                lock.unlock(l);
            }
        }
        unsafe.putInt(address + offset, id);
        if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.TRACE)) {
            LOGGER.log(
                    System.Logger.Level.TRACE, LoggingSystem.format(loggingContext.subject()
                            , "Pushing id=" + idSchema.idToString(id)
                    )
            );
        }
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
        return "IdStack={"
                + "capacity=" + capacity + "|off-heap"
                + '}';
    }
}

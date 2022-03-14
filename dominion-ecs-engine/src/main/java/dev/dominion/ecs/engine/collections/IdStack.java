/*
 * Copyright (c) 2021 Enrico Stara
 * This code is licensed under the MIT license. See the LICENSE file in the project root for license terms.
 */

package dev.dominion.ecs.engine.collections;

import dev.dominion.ecs.engine.system.LoggingSystem;
import dev.dominion.ecs.engine.system.UnsafeFactory;
import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdStack implements AutoCloseable {
    private static final System.Logger LOGGER = LoggingSystem.getLogger();
    private static final int INT_BYTES = 4;
    private static final Unsafe unsafe = UnsafeFactory.INSTANCE;
    private final AtomicInteger index = new AtomicInteger(-INT_BYTES);
    private final long address;
    private final int capacity;
    private final LoggingSystem.Context loggingContext;

    public IdStack(int capacity, LoggingSystem.Context loggingContext) {
        this.capacity = capacity;
        this.loggingContext = loggingContext;
        address = unsafe.allocateMemory(capacity);
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
        return index.compareAndSet(i, i - INT_BYTES) ? returnValue : Integer.MIN_VALUE;
    }

    public boolean push(int id) {
        long offset = index.addAndGet(INT_BYTES);
        if (offset < capacity) {
            unsafe.putInt(address + offset, id);
            if (LoggingSystem.isLoggable(loggingContext.levelIndex(), System.Logger.Level.DEBUG)) {
                LOGGER.log(
                        System.Logger.Level.DEBUG, LoggingSystem.format(loggingContext.subject()
                                , "Pushing id=" + id
                        )
                );
            }
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

    @Override
    public String toString() {
        return "IdStack={"
                + "capacity=" + capacity + "|off-heap"
                + '}';
    }
}
